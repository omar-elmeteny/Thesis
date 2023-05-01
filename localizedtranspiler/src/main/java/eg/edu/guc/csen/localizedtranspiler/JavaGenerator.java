package eg.edu.guc.csen.localizedtranspiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import eg.edu.guc.csen.keywordtranslator.Translations;
import eg.edu.guc.csen.localizedtranspiler.Java9Parser.AnnotationContext;
import eg.edu.guc.csen.localizedtranspiler.Java9Parser.BlockStatementsContext;
import eg.edu.guc.csen.localizedtranspiler.Java9Parser.CatchClauseContext;
import eg.edu.guc.csen.localizedtranspiler.Java9Parser.ClassTypeContext;
import eg.edu.guc.csen.localizedtranspiler.Java9Parser.ConstructorDeclarationContext;
import eg.edu.guc.csen.localizedtranspiler.Java9Parser.ExceptionTypeContext;
import eg.edu.guc.csen.localizedtranspiler.Java9Parser.IdentifierContext;
import eg.edu.guc.csen.localizedtranspiler.Java9Parser.LastFormalParameterContext;
import eg.edu.guc.csen.localizedtranspiler.Java9Parser.MethodDeclarationContext;
import eg.edu.guc.csen.localizedtranspiler.Java9Parser.Throws_Context;
import eg.edu.guc.csen.localizedtranspiler.Java9Parser.VariableDeclaratorIdContext;

public class JavaGenerator extends Java9ParserBaseVisitor<StringBuilder> {

    private StringBuilder builder = new StringBuilder();
    private final String targetLanguage;
    private String sourceLanguage;
    private int lastTokenStop = -1;

    private static HashMap<Integer, String> keywords = initializeKeywords();
    private final Translations translations;
    private final HashMap<String, String> translatedIdentifiers = new HashMap<String, String>();

    public HashMap<String, String> getTranslatedIdentifiers() {
        return translatedIdentifiers;
    }

    public JavaGenerator(String sourceLanguage, String targetLanguage, Translations translations) {
        super();
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.translations = translations;
    }

    private static HashMap<Integer, String> initializeKeywords() {
        HashMap<Integer, String> keywords = new HashMap<>();
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

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    private boolean isInsideMain = false;
    ArrayList<String> methodThrownExceptions = new ArrayList<>();

    @Override
    public StringBuilder visitMethodDeclaration(MethodDeclarationContext ctx) {
        if (this.sourceLanguage.equals("en")) {
            return super.visitMethodDeclaration(ctx);
        }
        methodThrownExceptions = getThrownExceptions(ctx);
        isInsideMain = isMainMethod(ctx);
        StringBuilder result = super.visitMethodDeclaration(ctx);
        isInsideMain = false;
        methodThrownExceptions = null;
        return result;
        
    }

    @Override
    public StringBuilder visitConstructorDeclaration(ConstructorDeclarationContext ctx) {
        methodThrownExceptions = getThrownExceptions(ctx);
        StringBuilder result = super.visitConstructorDeclaration(ctx);
        methodThrownExceptions = null;
        return result;
    }

    private ArrayList<String> getThrownExceptions(ConstructorDeclarationContext ctx) {
        if (ctx.throws_() == null) {
            return null;
        }
        return getThrownExceptions(ctx.throws_());
    }

    private ArrayList<String> getThrownExceptions(Throws_Context ctx) {
        if (ctx.exceptionTypeList() == null || ctx.exceptionTypeList().exceptionType() == null) {
            return null;
        }
        ArrayList<String> result = new ArrayList<>();
        for (ExceptionTypeContext exceptionTypeContext : ctx.exceptionTypeList().exceptionType()) {
            if (exceptionTypeContext.typeVariable() != null) {
                result.add(exceptionTypeContext.typeVariable().identifier().getText());
            } else {
                result.add(getClassFullName(exceptionTypeContext.classType()));
            }
        }
        return result;
    }

    private String getClassFullName(ClassTypeContext ctx) {
        TypeNameVisitor visitor = new TypeNameVisitor();
        return visitor.visitClassType(ctx).toString();
    }
    
    private boolean visitingCatchClause = false;

   @Override
    public StringBuilder visitCatchClause(CatchClauseContext ctx) {
        if(this.sourceLanguage.equals("en")) {
            return super.visitCatchClause(ctx);
        }
        visitingCatchClause = true;
        super.visitCatchClause(ctx);
        visitingCatchClause = false;
        return builder;
    }

    private boolean visitingCatchClauseVariableDeclatorId = false;
    @Override
    public StringBuilder visitVariableDeclaratorId(VariableDeclaratorIdContext ctx) {
        if (visitingCatchClause) {
            visitingCatchClauseVariableDeclatorId = true;
            var result = super.visitVariableDeclaratorId(ctx);
            visitingCatchClauseVariableDeclatorId = false;
            return result;
        }
        return super.visitVariableDeclaratorId(ctx);
    }

    private String exceptionVariableName = null;
    private String generatedExceptionVariableName = null;
    private boolean shouldRenameExceptionVariable = false;
    @Override
    public StringBuilder visitIdentifier(IdentifierContext ctx) {
        if (visitingCatchClauseVariableDeclatorId) {
            exceptionVariableName = ctx.getText();
            generatedExceptionVariableName = getExceptionVariableName();            
            shouldRenameExceptionVariable = true;
        }
        return super.visitIdentifier(ctx);
    }

    private ArrayList<String> getThrownExceptions(MethodDeclarationContext ctx) {
        if (ctx.methodHeader() != null && ctx.methodHeader().throws_() != null) {
            return getThrownExceptions(ctx.methodHeader().throws_());
        }
        return null;
    }

    private String getStringTypeName() {
        return this.translations.translateIdentifier("String", "en", sourceLanguage);
    }

    private boolean isPublicMethod(MethodDeclarationContext ctx) {
        return ctx.methodModifier().stream().anyMatch(modifier -> modifier.PUBLIC() != null);
    }

    private boolean isStaticMethod(MethodDeclarationContext ctx) {
        return ctx.methodModifier().stream().anyMatch(modifier -> modifier.STATIC() != null);
    }

    private boolean isMainMethod(MethodDeclarationContext ctx) {
        if (!isPublicMethod(ctx)) {
            return false;
        }
        if (!isStaticMethod(ctx)) {
            return false;
        }
        if (!ctx.methodHeader().methodDeclarator().identifier().getText().equals("main")) {
            return false;
        }

        if (ctx.methodHeader().result().VOID() == null) {
            return false;
        }
        List<ParseTree> parameters = ctx.methodHeader().methodDeclarator().formalParameterList().children;
        if (parameters == null || parameters.size() != 1
                || !(parameters.get(0) instanceof LastFormalParameterContext)) {
            return false;
        }
        LastFormalParameterContext parameter = (LastFormalParameterContext) parameters.get(0);
        String type = parameter.formalParameter().unannType().getText();
        if (!type.equals(getStringTypeName() + "[]") && !type.equals("String[]")) {
            return false;
        }
        return true;
    }

    private int exceptionVariableIndex = 0;
    private String getExceptionVariableName() {
        return "______e" + exceptionVariableIndex++;
    }

    @Override
    public StringBuilder visitBlockStatements(BlockStatementsContext ctx) {
        ArrayList<String> thrownExceptions = methodThrownExceptions;
        if (thrownExceptions != null  && thrownExceptions.size() > 0) {
            builder.append("try {");
            methodThrownExceptions = null;
            for (String exceptionType : thrownExceptions) {
                builder.append("if ((Object)Class.class == Object.class) throw new ");
                builder.append(exceptionType);
                builder.append("();");
            }
        }
        if (generatedExceptionVariableName != null && exceptionVariableName != null) {
            builder.append("var ");
            appendIdentifier(exceptionVariableName);
            builder.append(" = eg.edu.guc.csen.localizationruntimehelper.ExceptionHelper.getLocalizedCheckedException(");
            builder.append(generatedExceptionVariableName);
            builder.append(", \"");
            builder.append(this.sourceLanguage);
            builder.append("\");");
        }
        if (this.sourceLanguage.equals("en")) {
            return super.visitBlockStatements(ctx);
        }
        if (isInsideMain) {
            builder.append("eg.edu.guc.csen.localizationruntimehelper.Startup.initializeApplication();");
            isInsideMain = false;
        }
        builder.append("try {");
        super.visitBlockStatements(ctx);
        builder.append(
                "} catch (RuntimeException ");
        String exceptionVariableName = getExceptionVariableName();
        builder.append(exceptionVariableName);
        builder.append(") { throw eg.edu.guc.csen.localizationruntimehelper.ExceptionHelper.getLocalizedException(");
        builder.append(exceptionVariableName);
        builder.append(", \"");
        builder.append(this.sourceLanguage);
        builder.append("\"); }");
        if (thrownExceptions != null && thrownExceptions.size() > 0) {
            exceptionVariableName = getExceptionVariableName();
            builder.append("} catch (");
            for (int i = 0; i < thrownExceptions.size(); i++) {
                if (i > 0) {
                    builder.append(" | ");
                }
                builder.append(thrownExceptions.get(i));
            }
            builder.append(" ");
            builder.append(exceptionVariableName);
            builder.append(") { throw eg.edu.guc.csen.localizationruntimehelper.ExceptionHelper.getLocalizedCheckedException(");
            builder.append(exceptionVariableName);
            builder.append(", \"");
            builder.append(this.sourceLanguage);
            builder.append("\"); }");
        }
        return builder;
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
        } else if (tokenType == Java9Lexer.Identifier) {
            if (shouldRenameExceptionVariable && node.getText().equals(exceptionVariableName)) {
                appendIdentifier(generatedExceptionVariableName);
                shouldRenameExceptionVariable = false;
            } else {
                appendIdentifier(token.getText());
            }
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
        builder.append(translations.translateKeyword(keyword, "en", targetLanguage));
    }

    private void appendIdentifier(String identifier) {
        String translateIdentifier = translateIdentifier(identifier);
        builder.append(translateIdentifier);

    }

    private String translateIdentifier(String identifier) {
        String translateIdentifier = translations.translateIdentifier(identifier, sourceLanguage, targetLanguage);
        translatedIdentifiers.put(translateIdentifier, identifier);
        return translateIdentifier;
    }

    private class TypeNameVisitor extends Java9ParserBaseVisitor<StringBuilder> {
        StringBuilder innerBuilder = new StringBuilder();

        public TypeNameVisitor() {
            super();
        }

        @Override
        public StringBuilder visitAnnotation(AnnotationContext ctx) {
            return innerBuilder;
        }

        @Override
        public StringBuilder visitTerminal(TerminalNode node) {
            var token = node.getSymbol();
            if (token.getType() == Java9Lexer.EOF) {
                return builder;
            }
            int tokenType = token.getType();

            if (tokenType == Java9Lexer.Identifier) {
                innerBuilder.append(translateIdentifier(token.getText()));

            } else {
                innerBuilder.append(token.getText());
            }
            return innerBuilder;
        }

    }

}
