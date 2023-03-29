package eg.edu.guc.csen.keywordtranslator;

/**
 * KeywordTranslator
 */
public class KeywordTranslator {

    public static String translateKeyword(String keyword, String fromLanguage, String toLanguage) {
        if (fromLanguage.equals(toLanguage)) {
            return keyword;
        }
        if (fromLanguage.equals("en")) {
            return KeywordTranslations.translateFromEnglish(keyword, toLanguage);
        }
        if (toLanguage.equals("en")) {
            return KeywordTranslations.translateToEnglish(keyword, fromLanguage);
        }
        String english = KeywordTranslations.translateToEnglish(keyword, fromLanguage);
        return KeywordTranslations.translateFromEnglish(english, toLanguage);
    }
}