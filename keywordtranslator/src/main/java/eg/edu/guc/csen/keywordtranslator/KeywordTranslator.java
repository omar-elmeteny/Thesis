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
            return KeywordTranslations.getInstance().translateFromEnglish(keyword, toLanguage);
        }
        if (toLanguage.equals("en")) {
            return KeywordTranslations.getInstance().translateToEnglish(keyword, fromLanguage);
        }
        String english = KeywordTranslations.getInstance().translateToEnglish(keyword, fromLanguage);
        return KeywordTranslations.getInstance().translateFromEnglish(english, toLanguage);
    }
}