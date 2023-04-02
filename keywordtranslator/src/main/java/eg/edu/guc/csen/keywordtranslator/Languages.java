package eg.edu.guc.csen.keywordtranslator;



import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Languages {
    private final static Languages instance = new Languages("languages.json");
    private static Languages getInstance() {
        return instance;
    }

    private ArrayList<Language> languages;

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
                Language lang = new Language(key, name, nativeName);
                languages.add(lang);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Language> getLanguages() {
        return getInstance().languages;
    }
}

