package eg.edu.guc.csen.localizationruntimehelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.apache.commons.io.input.BOMInputStream;

import eg.edu.guc.csen.keywordtranslator.Translations;

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
            Translations translations = getTranslations(clazz);
            return (RuntimeException)translations.getExceptionTranslations().translateException(e, language);
        } catch (ClassNotFoundException e1) {
            return e;
        }
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
