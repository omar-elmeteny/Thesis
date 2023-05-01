package eg.edu.guc.csen.localizationruntimehelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.HashMap;

import org.json.JSONObject;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class IdentifiersHelper {

    private static final HashMap<ClassLoader, HashMap<String, String>> identifiersMap = new HashMap<ClassLoader, HashMap<String, String>>();

    public static HashMap<String, String> getIdentifiersDictionary(Class<?> clazz) {
        synchronized (identifiersMap) {
            ClassLoader classLoader = clazz.getClassLoader();
            if (identifiersMap.containsKey(classLoader)) {
                return identifiersMap.get(classLoader);
            }
            HashMap<String, String> identifiers = new HashMap<>();
            // Loop all classes in a class loader

            try {
                Enumeration<URL> resources = classLoader.getResources("");

                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();

                    try {
                        Path resourcePath = Paths.get(resource.toURI());

                        if (Files.isDirectory(resourcePath)) {
                            Files.walkFileTree(resourcePath, new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                        throws IOException {
                                    if (file.getFileName().toString().endsWith(".class")) {
                                        String clazzPath = resourcePath.relativize(file).toString()
                                            .replace('\\', '/');
                                        identifiers.putAll(getClassIdentifiers(classLoader, clazzPath));
                                    }

                                    return FileVisitResult.CONTINUE;
                                }
                            });
                        }
                    } catch (URISyntaxException e) {
                        // handle exception
                    }
                }
            } catch (IOException e) {
                // handle exception
            }

            identifiersMap.put(classLoader, identifiers);
            return identifiers;
        }
    }

    private static HashMap<String, String> getClassIdentifiers(ClassLoader loader, String streamName) {
        HashMap<String, String> identifiers = new HashMap<String, String>();
        try {
            try (InputStream inputStream = loader.getResourceAsStream(streamName)) {
                ClassReader classReader = new ClassReader(inputStream);
                // get attribute with type IdentifiersDictionary
                String idenfiersDictionayJson = readIdentifiersDictionaryAttribute(classReader);
                if (idenfiersDictionayJson != null) {
                    // read the contents of the resource file
                    JSONObject jsonObject = new JSONObject(idenfiersDictionayJson);
                    for (String key : jsonObject.keySet()) {
                        identifiers.put(key, jsonObject.getString(key));
                    }
                }
            }
        } catch (IOException e) {
            // ignore exception
        }
        return identifiers;
    }

    private static String readIdentifiersDictionaryAttribute(ClassReader classReader) {
        IdentifierDictionaryVisitor visitor = new IdentifierDictionaryVisitor();
        classReader.accept(visitor, new Attribute[] { new IdentifiersDictionaryAttribute() }, 0);
        return visitor.identifiersDictionary;
    }

    private static class IdentifierDictionaryVisitor extends ClassVisitor {
        private String identifiersDictionary;

        public IdentifierDictionaryVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visitAttribute(Attribute attr) {
            if (attr instanceof IdentifiersDictionaryAttribute) {
                identifiersDictionary = ((IdentifiersDictionaryAttribute) attr).identifiersDictionary;
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

        @SuppressWarnings("deprecation")
        @Override
        protected Attribute read(ClassReader classReader, int offset, int length, char[] charBuffer, int codeOffset,
                Label[] labels) {

            byte[] c = new byte[length - 2];
            System.arraycopy(classReader.b, offset + 2, c, 0, length - 2);
            String identifiersDictionary = new String(c, StandardCharsets.UTF_8);
            return new IdentifiersDictionaryAttribute(identifiersDictionary);
        }
    }
}
