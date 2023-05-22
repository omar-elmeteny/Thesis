package eg.edu.guc.csen.translator;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void testTranslateIdentifier() {
        Translations translations = new Translations();
        translations.updateFromJSONString("""
            {
                "defaultLanguage": "ar",
                "keywords": {
                    "synchronized": {"ar": "متزامن"},
                    "do": {"ar": "افعل"},
                    "float": {"ar": "عائم"},
                    "while": {"ar": "بينما"},
                    "protected": {"ar": "محمي"},
                    "continue": {"ar": "استمر"},
                    "else": {"ar": "وإلا"},
                    "catch": {"ar": "امسك"},
                    "export": {"ar": "صدر"},
                    "if": {"ar": "إذا"},
                    "case": {"ar": "حالة"},
                    "transitive": {"ar": "متعاقب"},
                    "new": {"ar": "جديد"},
                    "package": {"ar": "حزمة"},
                    "static": {"ar": "محايد"},
                    "void": {"ar": "لاشئ"},
                    "byte": {"ar": "بايت"},
                    "double": {"ar": "مزدوج"},
                    "var": {"ar": "متغير"},
                    "finally": {"ar": "أخيرا"},
                    "module": {"ar": "وحدة"},
                    "this": {"ar": "هذا"},
                    "strictfp": {"ar": "عائم_صارم"},
                    "throws": {"ar": "يرمي"},
                    "enum": {"ar": "تعدادي"},
                    "extends": {"ar": "يمد"},
                    "null": {"ar": "عدم"},
                    "transient": {"ar": "عابر"},
                    "final": {"ar": "نهائي"},
                    "true": {"ar": "حقيقي"},
                    "opens": {"ar": "يفتح"},
                    "try": {"ar": "حاول"},
                    "requires": {"ar": "يتطلب"},
                    "implements": {"ar": "يحقق"},
                    "private": {"ar": "خاص"},
                    "const": {"ar": "ثابت"},
                    "import": {"ar": "استورد"},
                    "exports": {"ar": "يصدر"},
                    "for": {"ar": "لكل"},
                    "interface": {"ar": "واجهة"},
                    "long": {"ar": "طويل"},
                    "switch": {"ar": "حول"},
                    "default": {"ar": "افتراضي"},
                    "goto": {"ar": "اذهب"},
                    "public": {"ar": "عام"},
                    "native": {"ar": "محلي"},
                    "assert": {"ar": "يجزم"},
                    "provides": {"ar": "يوفر"},
                    "class": {"ar": "نوع"},
                    "break": {"ar": "اخرج"},
                    "false": {"ar": "خطأ"},
                    "volatile": {"ar": "متقلب"},
                    "abstract": {"ar": "مجرد"},
                    "int": {"ar": "صحيح"},
                    "instanceof": {"ar": "نموذج_من"},
                    "super": {"ar": "أعلى"},
                    "with": {"ar": "مع"},
                    "boolean": {"ar": "منطقي"},
                    "throw": {"ar": "ارمي"},
                    "char": {"ar": "حرف"},
                    "short": {"ar": "قصير"},
                    "uses": {"ar": "يستخدم"},
                    "to": {"ar": "إلى"},
                    "open": {"ar": "افتح"},
                    "return": {"ar": "ارجع"}
                },
                "identifiers": {
                    "nextFloat": {"ar": "العائم_التالي"},
                    "HashMap": {"ar": "خريطة_تجزئة"},
                    "ArrayList": {"ar": "قائمة_مصفوفة"},
                    "String": {"ar": "سلسلة"},
                    "Math": {"ar": "رياضيات"},
                    "java": {"ar": "جافا"},
                    "Random": {"ar": "عشوائي"},
                    "nextShort": {"ar": "القصير_التالي"},
                    "Boolean": {"ar": "المنطقي"},
                    "net": {"ar": "شبكة"},
                    "Short": {"ar": "القصير"},
                    "HashSet": {"ar": "مجموعة_تجزئة"},
                    "add": {"ar": "أضف"},
                    "Character": {"ar": "الحرف"},
                    "in": {"ar": "داخل"},
                    "io": {"ar": "io"},
                    "format": {"ar": "تنسيق"},
                    "Double": {"ar": "المزدوج"},
                    "LinkedList": {"ar": "قائمة_مرتبطة"},
                    "Float": {"ar": "العائم"},
                    "contains": {"ar": "يتضمن"},
                    "size": {"ar": "مقاس"},
                    "util": {"ar": "أدوات"},
                    "Byte": {"ar": "البايت"},
                    "Long": {"ar": "الطويل"},
                    "math": {"ar": "رياضيات"},
                    "next": {"ar": "التالي"},
                    "Scanner": {"ar": "ماسح"},
                    "nextByte": {"ar": "البايت_التالي"},
                    "System": {"ar": "نظام"},
                    "remove": {"ar": "أزل"},
                    "substring": {"ar": "سلسلة_فرعية"},
                    "out": {"ar": "خارج"},
                    "printf": {"ar": "اطبع_منسق"},
                    "println": {"ar": "اطبع_سطر"},
                    "get": {"ar": "تحصيل"},
                    "nextBoolean": {"ar": "المنطقي_التالي"},
                    "lang": {"ar": "لغة"},
                    "set": {"ar": "تعيين"},
                    "err": {"ar": "خطأ"},
                    "length": {"ar": "طول"},
                    "nextInt": {"ar": "الصحيح_التالي"},
                    "nextLong": {"ar": "الطويل_التالي"},
                    "nextDouble": {"ar": "المزدوج_التالي"},
                    "Integer": {"ar": "الصحيح"},
                    "print": {"ar": "إطبع"},
                    "swing": {"ar": "يتأرجح"},
                    "charAt": {"ar": "حرف_عند"},
                    "awt": {"ar": "awt"}
                }
            }
        """);
        String arabic = translations.translateIdentifier("String", "en", "ar");
        assertEquals("سلسلة", arabic);
    }
}
