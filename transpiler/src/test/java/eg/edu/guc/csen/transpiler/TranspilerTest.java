package eg.edu.guc.csen.transpiler;

import org.junit.Test;

import eg.edu.guc.csen.translator.Translations;

public class TranspilerTest {

    @Test
    public void testEnglishToArabicTranspilation() {
        String javaCode = """
        package test2;

        public class Test {
            
            public static Integer test3() {
                return 0;
            }
            
            public static int test2() {
                return 0;
            }
            
            public static String test() {
                return "";
            }
        
            public static void main(String[] args) {
                System.out.println("Hello World!");
            }
        
        }
        """;
        TranspilerOptions options = new TranspilerOptions();
        options.setOutputEncoding("UTF-8");
        options.setSourceEncoding("UTF-8");
        options.setSourceLanguage("en");
        options.setTargetLanguage("ar");
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
        options.setTranslations(translations);
        String arabicCode = Transpiler.transpile(options, javaCode);
        System.out.println(arabicCode);
    }
    
}
