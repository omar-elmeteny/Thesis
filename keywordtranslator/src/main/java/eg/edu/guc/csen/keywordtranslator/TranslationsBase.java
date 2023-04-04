package eg.edu.guc.csen.keywordtranslator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.input.BOMInputStream;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class TranslationsBase {

    // key language (eg. "de" ) -> English keyword/identifier (e.g. "class") ->
    // Language translated keyword/identifier (e.g. "klasse")
    private final HashMap<String, HashMap<String, String>> englishToLanguageTranslations = new HashMap<>();
    // key language (eg. "de" ) -> Language translated keyword/identifier (e.g.
    // "klasse") -> English keyword (e.g. "class")
    private final HashMap<String, HashMap<String, String>> languageToEnglishTranslations = new HashMap<>();

    protected TranslationsBase(String resourcePath) {
        try {
            String jsonString = ResourceHelper.readResourceFile(resourcePath);
            loadFromJsonString(jsonString);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    protected TranslationsBase(File jsonFile) throws IOException {
        if (!jsonFile.exists()) {
            return;
        }
        try (FileInputStream fileInputStream = new FileInputStream(jsonFile)) {
            try (BOMInputStream bomInputStream = new BOMInputStream(fileInputStream)) {
                String jsonString = new String(bomInputStream.readAllBytes(), StandardCharsets.UTF_8);
                loadFromJsonString(jsonString);
            }
        }
    }

    private void loadFromJsonString(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);

        for (String item : jsonObject.keySet()) {
            JSONObject languageMap = jsonObject.getJSONObject(item);

            for (String language : languageMap.keySet()) {
                String translated = languageMap.getString(language);

                addTranslation(item, language, translated);
            }
        }
    }

    public void addTranslation(String word, String language, String translated) {
        if (!this.languageToEnglishTranslations.containsKey(language)) {
            this.languageToEnglishTranslations.put(language, new HashMap<>());
        }
        if (!this.englishToLanguageTranslations.containsKey(language)) {
            this.englishToLanguageTranslations.put(language, new HashMap<>());
        }
        this.englishToLanguageTranslations.get(language).put(word, translated);
        this.languageToEnglishTranslations.get(language).put(translated, word);
    }

    public String translateFromEnglish(String word, String language) {
        if (englishToLanguageTranslations.containsKey(language)) {
            HashMap<String, String> hashMap = englishToLanguageTranslations.get(language);
            if (hashMap.containsKey(word)) {
                return hashMap.get(word);
            }
        }
        return word;
    }

    public String translateToEnglish(String word, String language) {
        if (languageToEnglishTranslations.containsKey(language)) {
            HashMap<String, String> hashMap = languageToEnglishTranslations.get(language);
            if (hashMap.containsKey(word)) {
                return hashMap.get(word);
            }
        }
        return word;
    }

    public HashMap<String, String> getLanguageTranslations(String language) {
        if (languageToEnglishTranslations.containsKey(language)) {
            HashMap<String, String> hashMap = languageToEnglishTranslations.get(language);
            return hashMap;
        }
        return new HashMap<>();
    }

    public boolean hasTranslation(String word, String language) {
        if (!this.englishToLanguageTranslations.containsKey(language)) {
            return false;
        }
        return this.englishToLanguageTranslations.get(language).containsKey(word);
    }

    public void save(File jsonFile) throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject();

        HashMap<String, JSONObject> itemMaps = new HashMap<>();
        for (Map.Entry<String, HashMap<String, String>> languageMap : this.englishToLanguageTranslations.entrySet()) {
            String language = languageMap.getKey();
            HashMap<String, String> words = languageMap.getValue();

            for (Map.Entry<String, String> translationEntry: words.entrySet()) {
                String word = translationEntry.getKey();
                String translated = translationEntry.getValue();

                JSONObject wordObject;
                if (!itemMaps.containsKey(word)) {
                    wordObject = new JSONObject();
                    itemMaps.put(word, wordObject);
                    jsonObject.put(word, wordObject);
                } else {
                    wordObject = itemMaps.get(word);
                }
                wordObject.put(language, translated);
            }
        }

        // get utf BOM
        
        byte[] utf8Bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        Files.write(jsonFile.toPath(), utf8Bom, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        Files.write(jsonFile.toPath(), jsonObject.toString(4).getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND, StandardOpenOption.WRITE);
    }
}
