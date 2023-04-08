package eg.edu.guc.csen.keywordtranslator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONObject;

public class KeywordTranslations extends TranslationsBase {

    // private static final KeywordTranslations instance = new KeywordTranslations("keywords.json");

    // public static KeywordTranslations getInstance() {
    //     return instance;
    // }

    public KeywordTranslations() {
        super();
    }

    public KeywordTranslations(String resourcePath) {
        super(resourcePath);
    }

    public KeywordTranslations(JSONObject jsonObject) {
        super(jsonObject);
    }

    public KeywordTranslations(File jsonFile) throws IOException {
        super(jsonFile);
    }

    @Override
    public ArrayList<KeyValuePair> getLanguageTranslations(String language) {
        ArrayList<KeyValuePair> result = super.getLanguageTranslations(language);
        for (String keyword : Keywords.keywords) {
            KeyValuePair pair = new KeyValuePair(keyword, keyword);
            ArrayListUtil.insertIfNotExists(result, pair);
        }
        return result;
    }
}
