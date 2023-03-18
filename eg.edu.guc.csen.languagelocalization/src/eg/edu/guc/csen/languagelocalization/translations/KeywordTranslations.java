package eg.edu.guc.csen.languagelocalization.translations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

public class KeywordTranslations {
	private HashMap<String, HashMap<String, String>> translations;

    KeywordTranslations(String filePath) {
        this.translations = new HashMap<>();
        
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get(filePath)));
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
        if (!this.translations.containsKey(keyword)) {
            this.translations.put(keyword, new HashMap<>());
        }
        this.translations.get(keyword).put(language, translated);
    }
    
    public Set<Map.Entry<String, String>> getTranslationsByLanguage(String language) {
        HashMap<String, String> languageTranslations = new HashMap<>();

        for (Map.Entry<String, HashMap<String, String>> entry : this.translations.entrySet()) {
            String keyword = entry.getKey();
            HashMap<String, String> translations = entry.getValue();

            if (translations.containsKey(language)) {
                String translated = translations.get(language);
                languageTranslations.put(keyword, translated);
            } else {
            	languageTranslations.put(keyword, keyword);
            }
        }

        return languageTranslations.entrySet();
    }
}
