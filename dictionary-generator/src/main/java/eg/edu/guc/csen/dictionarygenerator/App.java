package eg.edu.guc.csen.dictionarygenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.text.StringEscapeUtils;

import eg.edu.guc.csen.keywordtranslator.IdentifierTranslations;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws IOException {
        String[] languages = new String[] { "ar", "de", "es", "fr", "it" };
        String[] packagePaths = new String[] { "java.base/java/lang", "java.base/java/util", "java.base/java/io", "java.base/java/math" };

        File dictionaryFile = new File(args.length == 0 ? "identifiers.json" : args[0]);
        IdentifierTranslations dictionary = new IdentifierTranslations(dictionaryFile);
        String javaBaseLocation = System.getProperty("java.home");
        String srcLocation = Path.of(javaBaseLocation, "lib", "src.zip").toString();

        String regex = "(" + String.join("|", packagePaths)
                .replaceAll("/", "\\\\/")
                .replaceAll("\\.", "\\\\.") + ")"
                + "\\/[A-Z][A-Za-z0-9]*\\.java";

        ArrayList<String> builtInPackageIdentifiers = new ArrayList<>();
        builtInPackageIdentifiers.add("java");
        builtInPackageIdentifiers.add("io");
        builtInPackageIdentifiers.add("util");
        builtInPackageIdentifiers.add("math");
        builtInPackageIdentifiers.add("lang");
        translateIdentifiers(builtInPackageIdentifiers, dictionary, languages);
        dictionary.save(dictionaryFile);

        try (ZipFile zipFile = new ZipFile(srcLocation)) {
            var entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.getName().matches(regex)) {
                    ArrayList<String> identifiers = new ArrayList<>();

                    String className = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);
                    className = className.substring(0, className.lastIndexOf('.'));
                    String packageName = entry.getName().substring(10, entry.getName().lastIndexOf('/')).replace('/',
                            '.');
                    try {
                        Class<?> cls = Class.forName(packageName + "." + className);
                        identifiers.add(className);

                        Method[] methods = cls.getDeclaredMethods();
                        // Print the methods
                        for (Method method : methods) {
                            if ((method.getModifiers() & Modifier.PUBLIC) != 0) {
                                if (!identifiers.contains(method.getName())) {
                                    identifiers.add(method.getName());
                                }
                            }
                        }

                        Field[] fields = cls.getFields();
                        for (Field field : fields) {
                            if ((field.getModifiers() & Modifier.PUBLIC) != 0) {
                                if (!identifiers.contains(field.getName())) {
                                    identifiers.add(field.getName());
                                }
                            }
                        }

                        translateIdentifiers(identifiers, dictionary, languages);
                        dictionary.save(dictionaryFile);

                    } catch (ClassNotFoundException e) {
                        continue;
                    }

                }
            }
        }
    }

    private static void translateIdentifiers(List<String> identifiers, IdentifierTranslations translations, String[] targetLanguages) {
        for (String language : targetLanguages) {
            ArrayList<String> wordsToTranslate = new ArrayList<String>();
            for (String identifier : identifiers) {
                if (!translations.hasTranslationFromEnglish(identifier, language)) {
                    addIdentifierToTranslate(identifier, wordsToTranslate);
                }
            }
            if (wordsToTranslate.size() > 0) {
                List<String> translationResult = GoogleTranslate.translate(wordsToTranslate, language);
                for (int i = 0; i < wordsToTranslate.size(); i++) {
                    translations.addTranslation(wordsToTranslate.get(i).replace(" ", ""), language, processTranslation(wordsToTranslate.get(i), language, translationResult.get(i)));
                }
            }
        }
    }

    private static void addIdentifierToTranslate(String identifier, ArrayList<String> wordsToTranslate) {
        // convert camel case or pascal case to space separated words
        String words = String.join(" ", identifier.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"));
        wordsToTranslate.add(words);
    }

    private static String processTranslation(String identifier, String language, String translation) {
        translation = StringEscapeUtils.unescapeHtml4(translation).replaceAll("'", "");
        if (language.equals("ar")) {
            return translation.replace(' ', '_');
        } else {
            if (Character.isUpperCase(identifier.charAt(0))) {
                // convert translation to pascal case
                return convertToPascalCase(translation);
            } else {
                return convertToCamelCase(translation);
            }
        }
    }

    public static String convertToPascalCase(String sentence) {
        StringBuilder pascalCaseBuilder = new StringBuilder();
        boolean shouldCapitalizeNext = true;

        for (char ch : sentence.toCharArray()) {
            if (ch == ' ') {
                shouldCapitalizeNext = true;
            } else {
                if (shouldCapitalizeNext) {
                    pascalCaseBuilder.append(Character.toUpperCase(ch));
                    shouldCapitalizeNext = false;
                } else {
                    pascalCaseBuilder.append(ch);
                }
            }
        }

        return pascalCaseBuilder.toString();
    }

    public static String convertToCamelCase(String sentence) {
        StringBuilder pascalCaseBuilder = new StringBuilder();
        boolean shouldCapitalizeNext = false;

        for (char ch : sentence.toCharArray()) {
            if (ch == ' ') {
                shouldCapitalizeNext = true;
            } else {
                if (shouldCapitalizeNext) {
                    pascalCaseBuilder.append(Character.toUpperCase(ch));
                    shouldCapitalizeNext = false;
                } else {
                    pascalCaseBuilder.append(ch);
                }
            }
        }

        return pascalCaseBuilder.toString();
    }
}
