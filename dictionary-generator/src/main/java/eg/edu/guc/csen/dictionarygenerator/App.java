package eg.edu.guc.csen.dictionarygenerator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.text.StringEscapeUtils;

import eg.edu.guc.csen.translator.IdentifierTranslations;
import eg.edu.guc.csen.translator.Identifiers;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws IOException {
        String[] languages = new String[] { "ar", "de", "es", "fr", "it" };
        
        File dictionaryFile = new File(args.length == 0 ? "identifiers.json" : args[0]);
        IdentifierTranslations dictionary = new IdentifierTranslations(dictionaryFile);
        ArrayList<String> commonIdentifiers = Identifiers.getCommonIdentifiers();
        translateIdentifiers(commonIdentifiers, dictionary, languages);
        dictionary.save(dictionaryFile);
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
