package eg.edu.guc.csen.transpilerplugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;

import eg.edu.guc.csen.keywordtranslator.IdentifierTranslator;
import eg.edu.guc.csen.localizedtranspiler.Transpiler;
import eg.edu.guc.csen.localizedtranspiler.TranspilerException;
import eg.edu.guc.csen.localizedtranspiler.TranspilerOptions;

@Mojo(name = "transpile", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true, threadSafe = true)
public class TranspilerMojo extends AbstractMojo {

    /*
     * --------------------------------------------------------------------
     * The following are Maven specific parameters, rather than specific
     * options that the transpiler tool can use.
     */

    /**
     * Provides an explicit list of all the guc files that should be included in
     * the generate phase of the plugin. Note that the plugin is smart enough to
     * realize that imported guc files should be included but not acted upon
     * directly by the transpiler Tool.
     * <p>
     * A set of Ant-like inclusion patterns used to select files from the source
     * directory for processing. By default, the pattern
     * <code>**&#47;*.guc</code> is used to select guc files.
     * </p>
     */
    @Parameter
    protected Set<String> includes = new HashSet<String>();
    /**
     * A set of Ant-like exclusion patterns used to prevent certain files from
     * being processed. By default, this set is empty such that no files are
     * excluded.
     */
    @Parameter
    protected Set<String> excludes = new HashSet<String>();

    /**
     * specify guc file encoding; e.g., euc-jp
     */
    @Parameter(property = "project.build.sourceEncoding")
    protected String inputEncoding;

    /**
     * Specify the source language; defaults to "ar"
     */
    @Parameter(defaultValue = "ar")
    protected String sourceLanguage;

    /**
     * specify output file encoding; defaults to source encoding
     */
    @Parameter(property = "project.build.sourceEncoding")
    protected String outputEncoding;

    /**
     * The directory where the guc files are located.
     */
    @Parameter(defaultValue = "${basedir}/src/main/guc")
    private File sourceDirectory;

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    /**
     * Specify output directory where the Java files are generated.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/guc")
    private File outputDirectory;

    public File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Log log = getLog();
        outputEncoding = validateEncoding(outputEncoding);

        if (log.isDebugEnabled()) {
            for (String e : excludes) {
                log.debug("GUC TRANSPILER: Exclude: " + e);
            }

            for (String e : includes) {
                log.debug("GUC TRANSPILER: Include: " + e);
            }

            log.debug("GUC TRANSPILER: Output: " + outputDirectory);
        }

        if (!sourceDirectory.isDirectory()) {
            log.info("No GUC files to compile in " + sourceDirectory.getAbsolutePath());
            return;
        }

        // Ensure that the output directory path is all in tact so that
        // GUC TRANSPILER can just write into it.
        //
        File outputDir = getOutputDirectory();

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        Set<File> gucFiles;
        try {
            gucFiles = getGucFiles(sourceDirectory);
        } catch (Exception e) {
            log.error(e);
            throw new MojoExecutionException(
                    "Fatal error occured while evaluating the names of the guc files to analyze", e);
        }

        TranspilerOptions options = new TranspilerOptions();
        options.setOutputEncoding(outputEncoding);
        options.setSourceEncoding(inputEncoding);
        options.setTargetLanguage("en");
        options.setSourceLanguage(sourceLanguage);
        Set<File> generatedFiles = new HashSet<File>();
        for (File gucFile : gucFiles) {
            String javaFileName = IdentifierTranslator.translateIdentifier(getFileNameWithoutExtension(gucFile),
                    sourceLanguage, "en") + ".java";
            String relativePath = getFilePathRelativeToSourceDirectory(gucFile);
            String[] split = relativePath.split("\\//|\\\\");

            File outDir = outputDirectory;
            for (int i = 0; i < split.length; i++) {
                split[i] = IdentifierTranslator.translateIdentifier(split[i], sourceLanguage, "en");
                outDir = new File(outDir, split[i]);
            }
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            File outputFile = new File(outDir, javaFileName);
            if (!outputFile.exists() || outputFile.lastModified() <= gucFile.lastModified()) {
                log.info("GUC TRANSPILER: " + gucFile.getAbsolutePath() + " -> " + outputFile.getAbsolutePath());
                try {
                    Transpiler.transpile(gucFile, outputFile, options);
                } catch (TranspilerException e) {
                    throw new MojoExecutionException("Error while transpiling " + gucFile.getAbsolutePath(), e);
                }
            }
            generatedFiles.add(outputFile);
        }

        Set<File> filesInOutput;
        try {
            filesInOutput = getFilesInOutputFolder(outputDirectory);
            for (File file : filesInOutput) {
                if (!generatedFiles.contains(file)) {
                    log.info("GUC TRANSPILER: Deleting " + file.getAbsolutePath());
                    Files.delete(file.toPath());;
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error while cleaning up", e);
        }

    }

    private String getFilePathRelativeToSourceDirectory(File file) {
        // get the path of the file relative to the source directory
        // example : if the file is /src/main/guc/com/example/Hello.guc
        String filePath = file.getParentFile().getAbsolutePath();
        // filePath should be /src/main/guc/com/example

        // and the source directory is /src/main/guc
        String sourceDirectoryPath = sourceDirectory.getAbsolutePath();
        if (filePath.equals(sourceDirectoryPath)) {
            return "";
        }
        // return should be com/example
        return filePath.substring(sourceDirectoryPath.length() + 1);
    }

    private static String getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        int pos = fileName.lastIndexOf(".");
        if (pos > 0) {
            return fileName.substring(0, pos);
        }
        return fileName;
    }

    private Set<File> getGucFiles(File sourceDirectory) throws InclusionScanException {
        // Which files under the source set should we be looking for as guc files
        SourceMapping mapping = new SuffixMapping("guc", Collections.<String>emptySet());

        // What are the sets of includes (defaulted or otherwise).
        Set<String> includes = getIncludesPatterns();

        SourceInclusionScanner scan = new SimpleSourceInclusionScanner(includes, excludes);
        scan.addSourceMapping(mapping);

        return scan.getIncludedSources(sourceDirectory, null);
    }

    public static Set<File> getFilesInOutputFolder(File outputDirectory){
        Set<File> javaFiles = new HashSet<>();
        listJavaFilesRecursively(outputDirectory, javaFiles);
        return javaFiles;
    }

    private static void listJavaFilesRecursively(File folder, Set<File> javaFiles) {
        File[] files = folder.listFiles();
        
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                listJavaFilesRecursively(file, javaFiles);
            } else if (file.getName().endsWith(".java")) {
                javaFiles.add(file);
            }
        }
    }

    public Set<String> getIncludesPatterns() {
        if (includes == null || includes.isEmpty()) {
            return Collections.singleton("**/*.guc");
        }
        return includes;
    }

    /**
     * Validates the given encoding.
     *
     * @return the validated encoding. If {@code null} was provided, returns the
     *         platform default encoding.
     */
    private String validateEncoding(String encoding) {
        return (encoding == null) ? Charset.defaultCharset().name() : Charset.forName(encoding.trim()).name();
    }
}
