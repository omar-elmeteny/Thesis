package eg.edu.guc.csen.keywordtranslator;

import com.ibm.icu.text.Transliterator;

public class IdentifierTranslator {
    public static String translateIdentifier(String identifier, String sourceLanguage, String targetLanguage) {
        IdentifierTranslations instance = IdentifierTranslations.getInstance();
        if(instance.hasTranslationToEnglish(identifier, sourceLanguage)) {
            String englishTranslation = instance.translateToEnglish(identifier, sourceLanguage);
            if(targetLanguage.equals("en")) {
                return englishTranslation;
            }
            if(instance.hasTranslationFromEnglish(englishTranslation, targetLanguage)) {
                return instance.translateFromEnglish(englishTranslation, targetLanguage);
            }
        }
        if(sourceLanguage.equals("ar")) {
            Transliterator transliterator = Transliterator.getInstance("Arabic-Latin");
            identifier = transliterator.transliterate(identifier);
        }
        return replaceNonAlphaNumeric(identifier);
    }

    private static String replaceNonAlphaNumeric(String input) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = (char)input.codePointAt(i);
            if (!shouldReplaceCharacter(c)) {
                output.append(c);
            } else {
                output.append(String.format("_x%04X_", (int)c));
            }
        }

        return output.toString();
    }

    private static boolean shouldReplaceCharacter(char c) {
        if (c == '$' || c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))
            return false;
        return true;
    }
}
