package eg.edu.guc.csen.keywordtranslator;

import java.io.File;
import java.io.IOException;

public class KeywordTranslations extends TranslationsBase {

    private static final KeywordTranslations instance = new KeywordTranslations("keywords.json");

    public static KeywordTranslations getInstance() {
        return instance;
    }

    public KeywordTranslations(String resourcePath) {
        super(resourcePath);
    }

    public KeywordTranslations(File jsonFile) throws IOException {
        super(jsonFile);
    }

}
