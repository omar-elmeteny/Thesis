package eg.edu.guc.csen.keywordtranslator;

import java.util.HashSet;

class Keywords {
    public static final HashSet<String> keywords = initializeKeywords();

    private static HashSet<String> initializeKeywords() {
        HashSet<String> keywords = new HashSet<>();
        keywords.add("abstract");
        keywords.add("assert");
        keywords.add("boolean");
        keywords.add("break");
        keywords.add("byte");
        keywords.add("case");
        keywords.add("catch");
        keywords.add("char");
        keywords.add("class");
        keywords.add("const");
        keywords.add("continue");
        keywords.add("default");
        keywords.add("do");
        keywords.add("double");
        keywords.add("else");
        keywords.add("enum");
        keywords.add("exports");
        keywords.add("extends");
        keywords.add("final");
        keywords.add("finally");
        keywords.add("float");
        keywords.add("for");
        keywords.add("if");
        keywords.add("goto");
        keywords.add("implements");
        keywords.add("import");
        keywords.add("instanceof");
        keywords.add("int");
        keywords.add("interface");
        keywords.add("long");
        keywords.add("module");
        keywords.add("native");
        keywords.add("new");
        keywords.add("open");
        keywords.add("opens");
        keywords.add("package");
        keywords.add("private");
        keywords.add("protected");
        keywords.add("provides");
        keywords.add("public");
        keywords.add("requires");
        keywords.add("return");
        keywords.add("short");
        keywords.add("static");
        keywords.add("strictfp");
        keywords.add("super");
        keywords.add("switch");
        keywords.add("synchronized");
        keywords.add("this");
        keywords.add("throw");
        keywords.add("throws");
        keywords.add("to");
        keywords.add("transient");
        keywords.add("transitive");
        keywords.add("try");
        keywords.add("uses");
        keywords.add("void");
        keywords.add("volatile");
        keywords.add("while");
        keywords.add("with");
        keywords.add("true");
        keywords.add("false");
        keywords.add("null");
        return keywords;
    }

}
