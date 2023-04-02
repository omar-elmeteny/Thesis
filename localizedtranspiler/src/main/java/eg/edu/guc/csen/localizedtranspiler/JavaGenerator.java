package eg.edu.guc.csen.localizedtranspiler;

import java.util.Hashtable;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import eg.edu.guc.csen.keywordtranslator.KeywordTranslator;

public class JavaGenerator extends Java9ParserBaseVisitor<StringBuilder> {

    private StringBuilder builder = new StringBuilder();
    private final String targetLanguage;
    private String sourceLanguage;
    private int lastTokenStop = -1;

    private static Hashtable<Integer, String> keywords = initializeKeywords();

    public JavaGenerator(String sourceLanguage, String targetLanguage) {
        super();
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }

    private static Hashtable<Integer, String> initializeKeywords() {
        Hashtable<Integer, String> keywords = new Hashtable<>();
        keywords.put(Java9Lexer.ABSTRACT, "abstract");
        keywords.put(Java9Lexer.ASSERT, "assert");
        keywords.put(Java9Lexer.BOOLEAN, "boolean");
        keywords.put(Java9Lexer.BREAK, "break");
        keywords.put(Java9Lexer.BYTE, "byte");
        keywords.put(Java9Lexer.CASE, "case");
        keywords.put(Java9Lexer.CATCH, "catch");
        keywords.put(Java9Lexer.CHAR, "char");
        keywords.put(Java9Lexer.CLASS, "class");
        keywords.put(Java9Lexer.CONST, "const");
        keywords.put(Java9Lexer.CONTINUE, "continue");
        keywords.put(Java9Lexer.DEFAULT, "default");
        keywords.put(Java9Lexer.DO, "do");
        keywords.put(Java9Lexer.DOUBLE, "double");
        keywords.put(Java9Lexer.ELSE, "else");
        keywords.put(Java9Lexer.ENUM, "enum");
        keywords.put(Java9Lexer.EXPORTS, "exports");
        keywords.put(Java9Lexer.EXTENDS, "extends");
        keywords.put(Java9Lexer.FINAL, "final");
        keywords.put(Java9Lexer.FINALLY, "finally");
        keywords.put(Java9Lexer.FLOAT, "float");
        keywords.put(Java9Lexer.FOR, "for");
        keywords.put(Java9Lexer.IF, "if");
        keywords.put(Java9Lexer.GOTO, "goto");
        keywords.put(Java9Lexer.IMPLEMENTS, "implements");
        keywords.put(Java9Lexer.IMPORT, "import");
        keywords.put(Java9Lexer.INSTANCEOF, "instanceof");
        keywords.put(Java9Lexer.INT, "int");
        keywords.put(Java9Lexer.INTERFACE, "interface");
        keywords.put(Java9Lexer.LONG, "long");
        keywords.put(Java9Lexer.MODULE, "module");
        keywords.put(Java9Lexer.NATIVE, "native");
        keywords.put(Java9Lexer.NEW, "new");
        keywords.put(Java9Lexer.OPEN, "open");
        keywords.put(Java9Lexer.OPERNS, "opens");
        keywords.put(Java9Lexer.PACKAGE, "package");
        keywords.put(Java9Lexer.PRIVATE, "private");
        keywords.put(Java9Lexer.PROTECTED, "protected");
        keywords.put(Java9Lexer.PROVIDES, "provides");
        keywords.put(Java9Lexer.PUBLIC, "public");
        keywords.put(Java9Lexer.REQUIRES, "requires");
        keywords.put(Java9Lexer.RETURN, "return");
        keywords.put(Java9Lexer.SHORT, "short");
        keywords.put(Java9Lexer.STATIC, "static");
        keywords.put(Java9Lexer.STRICTFP, "strictfp");
        keywords.put(Java9Lexer.SUPER, "super");
        keywords.put(Java9Lexer.SWITCH, "switch");
        keywords.put(Java9Lexer.SYNCHRONIZED, "synchronized");
        keywords.put(Java9Lexer.THIS, "this");
        keywords.put(Java9Lexer.THROW, "throw");
        keywords.put(Java9Lexer.THROWS, "throws");
        keywords.put(Java9Lexer.TO, "to");
        keywords.put(Java9Lexer.TRANSIENT, "transient");
        keywords.put(Java9Lexer.TRANSITIVE, "transitive");
        keywords.put(Java9Lexer.TRY, "try");
        keywords.put(Java9Lexer.USES, "uses");
        keywords.put(Java9Lexer.VOID, "void");
        keywords.put(Java9Lexer.VOLATILE, "volatile");
        keywords.put(Java9Lexer.WHILE, "while");
        keywords.put(Java9Lexer.WITH, "with");
        keywords.put(Java9Lexer.TRUE, "true");
        keywords.put(Java9Lexer.FALSE, "false");
        keywords.put(Java9Lexer.NULL, "null");
        return keywords;
        
    }

    public JavaGenerator() {
        super();
        this.targetLanguage = "en";
        this.sourceLanguage = "en";
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    @Override
    public StringBuilder visitTerminal(TerminalNode node) {
        var token = node.getSymbol();
        if (token.getType() == Java9Lexer.EOF) {
            return builder;
        }
        var start = token.getStartIndex();
        var stream = token.getInputStream();
        appendStreamText(stream, lastTokenStop + 1, start);

        int tokenType = token.getType();
        if (keywords.containsKey(tokenType)) {
            appendKeyword(keywords.get(tokenType));
        } else {
            builder.append(token.getText());
        }

        lastTokenStop = token.getStopIndex();

        return builder;
    }

    private void appendStreamText(CharStream stream, int startIndex, int stopIndex) {
        int length = stopIndex - startIndex;
        if (length <= 0) {
            return;
        }
        String s = stream.getText(new Interval(startIndex, stopIndex - 1));
        builder.append(s);
    }

    private void appendKeyword(String keyword) {
        builder.append(KeywordTranslator.translateKeyword(keyword, "en", targetLanguage));
    }
}