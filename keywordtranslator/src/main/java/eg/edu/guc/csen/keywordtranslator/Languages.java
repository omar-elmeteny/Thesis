package eg.edu.guc.csen.keywordtranslator;



import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Languages {
    private final static Languages instance = new Languages("languages.json");
    private static Languages getInstance() {
        return instance;
    }

    private final ArrayList<Language> languages;
    private final HashMap<String, Language> languagesMap;

    private Languages(String filePath) {
        languages = new ArrayList<>();

        try {
            String jsonStr = ResourceHelper.readResourceFile(filePath);
            JSONObject jsonObject = new JSONObject(jsonStr);

            JSONArray langArray = jsonObject.getJSONArray("languages");

            for (int i = 0; i < langArray.length(); i++) {
                JSONObject langJson = langArray.getJSONObject(i);
                String key = langJson.getString("key");
                String name = langJson.getString("name");
                String nativeName = langJson.getString("nativeName");
                String script = langJson.getString("script");
                boolean rtl = langJson.getBoolean("rtl");
                String atTranslation = langJson.getString("at");
                Language lang = new Language(key, name, nativeName, script, rtl, atTranslation);
                languages.add(lang);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        languagesMap = new HashMap<>();
        for (Language lang : languages) {
            languagesMap.put(lang.getKey(), lang);
        }
    }

    public static ArrayList<Language> getLanguages() {
        return new ArrayList<>(getInstance().languages);
    }

    public static Language getLanguage(String key) {
        return getInstance().languagesMap.get(key);
    }
}

