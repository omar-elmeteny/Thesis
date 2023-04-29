package eg.edu.guc.csen.transpilerplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

@Mojo(name = "sourcemap-postprocessor", defaultPhase = LifecyclePhase.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true, threadSafe = false)
public class SourceMapMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}")
    private File targetDirectory;

    public File getTargetDirectory() {
        return targetDirectory;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            processFolder(targetDirectory);
        } catch (IOException e) {
            throw new MojoExecutionException("Error while processing folder: ", e);
        }
    }

    private void processFolder(File folder) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                processFolder(file);
            } else if (file.getName().endsWith(".class")) {
                processFile(file);
            }
        }
    }

    private void processFile(File file) throws IOException {
        getLog().info("Processing file: " + file.getAbsolutePath());
        File sourcemapFile = new File(file.getAbsolutePath().replaceAll("\\.class", ".java.smap"));

        File identifiersFile = new File(file.getAbsolutePath().replaceAll("\\.class", ".java.identifiers"));
        if (!sourcemapFile.exists() && !identifiersFile.exists()) {
            getLog().info("No sourcemap file or identifiers file found for file: "
                    + file.getAbsolutePath());
            return;
        }

        getLog().info("Found sourcemap file: " + sourcemapFile.getAbsolutePath());
        ClassReader classReader;
        // Load the original class file
        try (InputStream inputStream = new FileInputStream(file)) {
            classReader = new ClassReader(inputStream);
        }

        // Create a custom attribute to hold the debug information
        // read ths sourcemapFile
        String debugInfo = sourcemapFile.exists() ? Files.readString(sourcemapFile.toPath()) : null;
        String identifiersDictionary = identifiersFile.exists() ? Files.readString(identifiersFile.toPath()) : null;
        // Create a ClassWriter to write the modified class file
        ClassWriter classWriter = new ClassWriter(0);
        // Add the debug attribute to the class
        // classWriter.visitAttribute(debugAttribute);
        // classWriter.visitSource(debugInfo, debugInfo);
        SourceClassVisitor visitor = new SourceClassVisitor(classWriter, debugInfo, getLog());
        if (identifiersDictionary != null) {
            IdentifiersDictionaryAttribute attribute = new IdentifiersDictionaryAttribute(identifiersDictionary);
            visitor.visitAttribute(attribute);
        }
        // Copy the original class into the ClassWriter
        classReader.accept(visitor, ClassReader.EXPAND_FRAMES);

        // Write the modified class file to disk
        byte[] modifiedClassBytes = classWriter.toByteArray();
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(modifiedClassBytes);
        }
        getLog().info("Processed file: " + file.getAbsolutePath());
    }

    private static class SourceClassVisitor extends ClassVisitor {

        private final Log log;
        private final String debugInfo;
        private static final Pattern pattern = Pattern.compile("\\+ 1 ([^\r\n]+)\r?\n([^\r\n]+)\r?\n",
                Pattern.MULTILINE);
        private final String sourceFile;

        protected SourceClassVisitor(ClassWriter writer, String debugInfo, Log log) {
            super(Opcodes.ASM9, writer);
            this.debugInfo = debugInfo;
            this.log = log;
            Matcher m = pattern.matcher(debugInfo);
            if (m.find()) {
                log.info("Found match: " + m.group(1) + " " + m.group(2));
                sourceFile = m.group(2);
            } else {
                log.error("Found no match match for: " + debugInfo);
                sourceFile = null;
            }
        }

        @Override
        public void visitSource(String source, String debug) {
            log.info("Visiting source: " + source + " debug: " + debug);
            if (sourceFile == null) {
                // log.info("Setting debug to: " + debugInfo);
                super.visitSource(source, debugInfo);
            } else {
                log.info("Setting source to: " + sourceFile);
                // log.info("Setting debug to: " + debugInfo);
                super.visitSource(sourceFile, debugInfo);
            }
        }
    }

    private static class IdentifiersDictionaryAttribute extends Attribute {

        private final String identifiersDictionary;

        public IdentifiersDictionaryAttribute(String identifiersDictionary) {
            super("IdentifiersDictionary");
            this.identifiersDictionary = identifiersDictionary;
        }

        public IdentifiersDictionaryAttribute() {
            super("IdentifiersDictionary");
            this.identifiersDictionary = null;
        }

        @Override
        protected ByteVector write(ClassWriter classWriter, byte[] code, int codeLength, int maxStack, int maxLocals) {
            ByteVector byteVector = new ByteVector();
            byteVector.putUTF8(identifiersDictionary);
            return byteVector;
        }
    }

}
