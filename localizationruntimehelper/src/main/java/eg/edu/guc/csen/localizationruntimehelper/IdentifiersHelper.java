package eg.edu.guc.csen.localizationruntimehelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.json.JSONObject;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class IdentifiersHelper {

    private static final HashMap<Class<?>, HashMap<String, String>> identifiersMap = new HashMap<Class<?>, HashMap<String, String>>();

    public static HashMap<String, String> getIdentifiersDictionary(Class<?> clazz) {
        synchronized (identifiersMap) {
            if (identifiersMap.containsKey(clazz)) {
                return identifiersMap.get(clazz);
            }
            HashMap<String, String> identifiers = new HashMap<String, String>();
            ClassLoader classLoader = clazz.getClassLoader();
            String className = clazz.getName();
            String streamName = className.replace('.', '/') + ".class";

            try {
                try (InputStream inputStream = classLoader.getResourceAsStream(streamName)) {
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
            identifiersMap.put(clazz, identifiers);
            return identifiers;
        }
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
