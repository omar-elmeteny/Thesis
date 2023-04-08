package eg.edu.guc.csen.keywordtranslator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TranslatorsTests {
    
    @Test
    public void testShouldNotAddExistingTranslation() {
        Translations translations = new Translations();

        boolean result = translations.canAddTranslation("abstract", "ar", "assert");
        assertFalse("Should not be able to use an existing keyword as translation for another keyword", result);
    }

    @Test
    public void testCanAddTranslations() {
        Translations translations = new Translations();

        boolean result = translations.canAddTranslation("abstract", "ar", "abstractar");
        assertTrue("Should be able to translate keyword", result);
    }

    @Test
    public void testShouldNotAddDuplicateTranslations() {
        Translations translations = new Translations();

        boolean result = translations.canAddTranslation("abstract", "ar", "abstractar");
        assertTrue("Should be able to translate keyword", result);
        translations.getKeywordTranslations().addTranslation("abstract", "ar", "abstractar");
        result = translations.canAddTranslation("assert", "ar", "abstractar");

        assertFalse("Should not be able to add duplicate translations", result);
    }
}
