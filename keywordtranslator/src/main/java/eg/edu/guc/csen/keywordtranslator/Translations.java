package eg.edu.guc.csen.keywordtranslator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.input.BOMInputStream;
import org.json.JSONObject;

public class Translations {

    private final KeywordTranslations keywordTranslations;
    private final IdentifierTranslations identifierTranslations;
    private String defaultLanguage = "ar";

    public Translations() {
        keywordTranslations = new KeywordTranslations();
        identifierTranslations = new IdentifierTranslations();
    }

    public Translations(File jsonFile) throws IOException {
        super();
        if (!jsonFile.exists()) {
            keywordTranslations = new KeywordTranslations();
            identifierTranslations = new IdentifierTranslations();
            return;
        }
        try (FileInputStream fileInputStream = new FileInputStream(jsonFile)) {
            try (BOMInputStream bomInputStream = new BOMInputStream(fileInputStream)) {
                String jsonString = new String(bomInputStream.readAllBytes(), StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.has("keywords")) {
                    keywordTranslations = new KeywordTranslations(jsonObject.getJSONObject("keywords"));
                } else {
                    keywordTranslations = new KeywordTranslations();
                }
                if (jsonObject.has("identifiers")) {
                    identifierTranslations = new IdentifierTranslations(jsonObject.getJSONObject("identifiers"));
                } else {
                    identifierTranslations = new IdentifierTranslations();
                }
                if (jsonObject.has("defaultLanguage")) {
                    defaultLanguage = jsonObject.getString("defaultLanguage");
                }
            }
        }
    }

    public void save(File jsonFile) throws IOException {
        JSONObject jsonObject = toJSON();
        Files.write(jsonFile.toPath(), jsonObject.toString(4).getBytes(StandardCharsets.UTF_8));
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("keywords", keywordTranslations.toJSON());
        jsonObject.put("identifiers", identifierTranslations.toJSON());
        jsonObject.put("defaultLanguage", defaultLanguage);
        return jsonObject;
    }

    public KeywordTranslations getKeywordTranslations() {
        return keywordTranslations;
    }

    public IdentifierTranslations getIdentifierTranslations() {
        return identifierTranslations;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }
}
