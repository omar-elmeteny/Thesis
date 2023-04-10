package eg.edu.guc.csen.keywordtranslator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONObject;

public class IdentifierTranslations extends TranslationsBase {

    private static IdentifierTranslations defaults;

    public synchronized static IdentifierTranslations getDefaults() {
        if (defaults == null) {
            defaults = new IdentifierTranslations("identifiers.json");
        }
        return defaults;
    }

    public IdentifierTranslations() {
        super();
    }

    public IdentifierTranslations(File jsonFile) throws IOException {
        super(jsonFile);
    }

    public IdentifierTranslations(JSONObject jsonObject) {
        super(jsonObject);
    }

    public IdentifierTranslations(String resourcePath) {
        super(resourcePath);
    }

    @Override
    public ArrayList<KeyValuePair> getLanguageTranslations(String language) {
        ArrayList<KeyValuePair> result = super.getLanguageTranslations(language);
        for (String identifier : Identifiers.commonIdentifiers) {
            KeyValuePair pair = new KeyValuePair(identifier, identifier);
            ArrayListUtil.insertIfNotExists(result, pair);
        }
        return result;
    }
}
