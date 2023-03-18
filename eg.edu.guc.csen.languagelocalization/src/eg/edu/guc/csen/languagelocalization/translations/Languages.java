package eg.edu.guc.csen.languagelocalization.translations;



import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Languages {
    private ArrayList<Language> languages;

    Languages(String filename) {
        languages = new ArrayList<>();

        try {
            String jsonStr = Files.readString(new File(filename).toPath());
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

    public ArrayList<Language> getLanguages() {
        return languages;
    }
}

