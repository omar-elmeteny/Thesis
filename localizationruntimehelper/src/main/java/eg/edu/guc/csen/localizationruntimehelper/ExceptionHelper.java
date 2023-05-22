package eg.edu.guc.csen.localizationruntimehelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.commons.io.input.BOMInputStream;

import eg.edu.guc.csen.translator.Languages;
import eg.edu.guc.csen.translator.Translations;

public class ExceptionHelper {

    private static HashMap<ClassLoader, Translations> translationsMap = new HashMap<ClassLoader, Translations>();

    public static RuntimeException getLocalizedException(RuntimeException e, String language) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int index = -1;
        for (int i = 0; i < stackTrace.length; i++) {
            if (stackTrace[i].getClassName().equals(ExceptionHelper.class.getName())
                    && stackTrace[i].getMethodName().equals("getLocalizedException")
            ) {
                index = i;
                break;
            }
        }
        if (index == -1 || stackTrace.length <= index + 1) {
            return e;
        }
        StackTraceElement caller = stackTrace[index + 1];
        try {
            Class<?> clazz = Class.forName(caller.getClassName());
            HashMap<String, String> idenfitiersDictionary = IdentifiersHelper.getIdentifiersDictionary(clazz);
            Translations translations = getTranslations(clazz);
            return (RuntimeException)translations.getExceptionTranslations().translateThrowable(e, language, idenfitiersDictionary);
        } catch (ClassNotFoundException e1) {
            return e;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T getLocalizedCheckedThrowable(T e, String language) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int index = -1;
        for (int i = 0; i < stackTrace.length; i++) {
            if (stackTrace[i].getClassName().equals(ExceptionHelper.class.getName())
                    && stackTrace[i].getMethodName().equals("getLocalizedCheckedThrowable")
            ) {
                index = i;
                break;
            }
        }
        if (index == -1 || stackTrace.length <= index + 1) {
            return e;
        }
        StackTraceElement caller = stackTrace[index + 1];
        try {
            Class<?> clazz = Class.forName(caller.getClassName());
            HashMap<String, String> idenfitiersDictionary = IdentifiersHelper.getIdentifiersDictionary(clazz);
            Translations translations = getTranslations(clazz);
            return (T) translations.getExceptionTranslations().translateThrowable(e, language, idenfitiersDictionary);
        } catch (ClassNotFoundException e1) {
            return e;
        }
    }

    public static void printStackTrace(Throwable e, String language) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int index = -1;
        for (int i = 0; i < stackTrace.length; i++) {
            if (stackTrace[i].getClassName().equals(ExceptionHelper.class.getName())
                    && stackTrace[i].getMethodName().equals("printStackTrace")
            ) {
                index = i;
                break;
            }
        }
        if (index == -1 || stackTrace.length <= index + 1) {
            e.printStackTrace();
            return;
        }
        StackTraceElement caller = stackTrace[index + 1];
        try {
            Class<?> clazz = Class.forName(caller.getClassName());
            HashMap<String, String> idenfitiersDictionary = IdentifiersHelper.getIdentifiersDictionary(clazz);
            Translations translations = getTranslations(clazz);
        
            String className = e.getClass().getName();
            String[] split = className.split("\\.");
            for (int i = 0; i < split.length; i++) {
                split[i] = translateIdentifier(split[i], idenfitiersDictionary, translations, language);
            }

            String message = e.getLocalizedMessage();
            className = String.join(".", split);
            if (message != null && message.length() > 0) {
                System.err.println(className + ": " + message);
            } else {
                System.err.println(className);
            }
            String atTranlated = Languages.getLanguage(language).getAtTranslation();
            for (StackTraceElement el : e.getStackTrace()) {
                System.err.println("\t" + atTranlated + " " + el.toString());
            }
        } catch (ClassNotFoundException e1) {
            e.printStackTrace();
            return;
        }
    }

    private static String translateIdentifier(String identifier, HashMap<String, String> idenfitiersDictionary, Translations translations, String language ) {
        if (idenfitiersDictionary.containsKey(identifier)) {
            return idenfitiersDictionary.get(identifier);
        }
        if (translations.getIdentifierTranslations().hasTranslationFromEnglish(identifier, language)) {
            return translations.getIdentifierTranslations().translateFromEnglish(identifier, language);
        }
        return identifier;
    }

    private static Translations getTranslations(Class<?> clazz) {
        synchronized (translationsMap) {
            if (translationsMap.containsKey(clazz.getClassLoader())) {
                return translationsMap.get(clazz.getClassLoader());
            }
            Translations translations = new Translations();
            ClassLoader classLoader = clazz.getClassLoader();
            try {
                try (InputStream inputStream = classLoader.getResourceAsStream("translations.guct")) {
                    try (BOMInputStream bomInputStream = new BOMInputStream(inputStream)) {
                        // read the contents of the resource file
                        String fileContents = new String(bomInputStream.readAllBytes(), StandardCharsets.UTF_8);
                        translations.updateFromJSONString(fileContents);
                    }
                }
            } catch (IOException e) {
                // ignore exception
            }
            translationsMap.put(classLoader, translations);
            return translations;
        }
    }

}
