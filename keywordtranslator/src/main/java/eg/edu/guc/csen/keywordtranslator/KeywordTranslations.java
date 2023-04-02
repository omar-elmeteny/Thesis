package eg.edu.guc.csen.keywordtranslator;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

public class KeywordTranslations {

    private static final KeywordTranslations instance = new KeywordTranslations("keywords.json");

    public static KeywordTranslations getInstance() {
        return instance;
    }

    // key language (eg. "de" ) -> English keyword (e.g. "class") -> Language translated keyword (e.g. "klasse")
	private HashMap<String, HashMap<String, String>> englishToLanguageTranslations;
    // key language (eg. "de" ) -> Language translated keyword (e.g. "klasse") -> English keyword (e.g. "class")
    private HashMap<String, HashMap<String, String>> languageToEnglishTranslations;

    private KeywordTranslations(String filePath) {
        this.englishToLanguageTranslations = new HashMap<>();
        this.languageToEnglishTranslations = new HashMap<>();
        
        try {
            String jsonString = ResourceHelper.readResourceFile(filePath);
            JSONObject jsonObject = new JSONObject(jsonString);

            for (String keyword : jsonObject.keySet()) {
                JSONObject languageMap = jsonObject.getJSONObject(keyword);

                for (String language : languageMap.keySet()) {
                    String translated = languageMap.getString(language);

                    addTranslation(keyword, language, translated);
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void addTranslation(String keyword, String language, String translated) {
        if (!this.languageToEnglishTranslations.containsKey(language)) {
            this.languageToEnglishTranslations.put(language, new HashMap<>());
        }
        if (!this.englishToLanguageTranslations.containsKey(language)) {
            this.englishToLanguageTranslations.put(language, new HashMap<>());
        }
        this.englishToLanguageTranslations.get(language).put(keyword, translated);
        this.languageToEnglishTranslations.get(language).put(translated, keyword);
    }
    
    public static String translateFromEnglish(String keyword, String language) {
        if (instance.englishToLanguageTranslations.containsKey(language)) {
            HashMap<String, String> hashMap = instance.englishToLanguageTranslations.get(language);
            if (hashMap.containsKey(keyword)) {
                return hashMap.get(keyword);
            }
        }
        return keyword;
    }

    public static String translateToEnglish(String keyword, String language) {
        if (instance.languageToEnglishTranslations.containsKey(language)) {
            HashMap<String, String> hashMap = instance.languageToEnglishTranslations.get(language);
            if (hashMap.containsKey(keyword)) {
                return hashMap.get(keyword);
            }
        }
        return keyword;
    }

    public static HashMap<String, String> getLanguageTranslations(String language) {
        if (instance.languageToEnglishTranslations.containsKey(language)) {
            HashMap<String, String> hashMap = instance.languageToEnglishTranslations.get(language);
            return hashMap;
        }
        return new HashMap<>();
    }
    
}
