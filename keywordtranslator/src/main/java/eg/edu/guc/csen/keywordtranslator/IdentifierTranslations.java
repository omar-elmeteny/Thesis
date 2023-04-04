package eg.edu.guc.csen.keywordtranslator;

import java.io.File;
import java.io.IOException;

public class IdentifierTranslations extends TranslationsBase {

    private static final IdentifierTranslations instance = new IdentifierTranslations("identifiers.json");

    public static IdentifierTranslations getInstance() {
        return instance;
    }

    public IdentifierTranslations(File jsonFile) throws IOException {
        super(jsonFile);
    }

    public IdentifierTranslations(String resourcePath) {
        super(resourcePath);
    }
}
