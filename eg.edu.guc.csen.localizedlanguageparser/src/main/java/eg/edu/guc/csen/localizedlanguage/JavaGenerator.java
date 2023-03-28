package eg.edu.guc.csen.localizedlanguage;

import org.antlr.v4.runtime.tree.TerminalNode;

import eg.edu.guc.csen.antlr4.Java9ParserBaseVisitor;
import eg.edu.guc.csen.antlr4.Java9Parser.*;

public class JavaGenerator extends Java9ParserBaseVisitor<StringBuilder> {

    private StringBuilder builder = new StringBuilder();
    private int indentLevel = 0;
    private static final String INDENT = "    ";
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private final String targetLanguage;
    private String sourceLanguage;

    public JavaGenerator(String sourceLanguage, String targetLanguage) {
        super();
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }

    public JavaGenerator() {
        super();
        this.targetLanguage = "en";
        this.sourceLanguage = "en";
    }

    @Override
    public StringBuilder visitCompilationUnit(CompilationUnitContext ctx) {
        super.visitCompilationUnit(ctx);
        return builder;
    }

    @Override
    public StringBuilder visitOrdinaryCompilation(OrdinaryCompilationContext ctx) {
        if (ctx.packageDeclaration() != null) {
            visit(ctx.packageDeclaration());
        }
        if (ctx.importDeclaration() != null) {
            for (var child : ctx.importDeclaration()) {
                visit(child);
            }
        }
        if (ctx.typeDeclaration() != null) {
            for (var child : ctx.typeDeclaration()) {
                visit(child);
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitSingleTypeImportDeclaration(SingleTypeImportDeclarationContext ctx) {
        appendKeyword("import");
        space();
        visit(ctx.typeName());
        builder.append(';');
        newLine();
        return builder;
    }

    @Override
    public StringBuilder visitTypeImportOnDemandDeclaration(TypeImportOnDemandDeclarationContext ctx) {
        appendKeyword("import");
        space();
        visit(ctx.packageOrTypeName());
        builder.append(".*;");
        newLine();
        return builder;
    }

    @Override
    public StringBuilder visitSingleStaticImportDeclaration(SingleStaticImportDeclarationContext ctx) {
        appendKeyword("import");
        space();
        appendKeyword("static");
        space();
        visit(ctx.typeName());
        builder.append('.');
        visit(ctx.identifier());
        builder.append(';');
        newLine();
        return builder;
    }

    @Override
    public StringBuilder visitStaticImportOnDemandDeclaration(StaticImportOnDemandDeclarationContext ctx) {
        appendKeyword("import");
        space();
        appendKeyword("static");
        space();
        visit(ctx.typeName());
        builder.append(".*;");
        newLine();
        return builder;
    }

    @Override
    public StringBuilder visitPackageDeclaration(PackageDeclarationContext ctx) {
        if (ctx.packageModifier() != null) {
            for (var child : ctx.packageModifier()) {
                visit(child);
            }
        }
        appendKeyword("package");
        space();
        visit(ctx.packageName());
        builder.append(';');
        newLine();
        return builder;
    }

    @Override
    public StringBuilder visitNormalClassDeclaration(NormalClassDeclarationContext ctx) {
        if (ctx.classModifier() != null) {
            for (var child : ctx.classModifier()) {
                visit(child);
                space();
            }
        }
        appendKeyword("class");
        space();
        visit(ctx.identifier());

        // TODO: Implement typeParameters
        if (ctx.typeParameters() != null) {
            visit(ctx.typeParameters());
        }
        // TODO: Implement superClass
        if (ctx.superclass() != null) {
            visit(ctx.superclass());
        }
        // TODO: Implement superInterfaces
        if (ctx.superinterfaces() != null) {
            visit(ctx.superinterfaces());
        }
        visit(ctx.classBody());
        return builder;
    }

    @Override
    public StringBuilder visitLiteral(LiteralContext ctx) {
        if (ctx.booleanLiteral() != null) {
            String booleanLiteral = ctx.booleanLiteral().getText();
            String keyword = KeywordTranslator.translateKeyword(booleanLiteral, sourceLanguage, "en");
            appendKeyword(keyword);
        } else if (ctx.nullLiteral() != null) {
            appendKeyword("null");
        } else {
            builder.append(ctx.getText());
        }
        return builder;
    }

    @Override
    public StringBuilder visitNormalInterfaceDeclaration(NormalInterfaceDeclarationContext ctx) {
        if (ctx.interfaceModifier() != null) {
            for (var child : ctx.interfaceModifier()) {
                visit(child);
                space();
            }
        }
        appendKeyword("interface");
        space();
        visit(ctx.identifier());
        if (ctx.typeParameters() != null) {
            visit(ctx.typeParameters());
        }
        if (ctx.extendsInterfaces() != null) {
            visit(ctx.extendsInterfaces());
        }
        visit(ctx.interfaceBody());
        return builder;
    }

    @Override
    public StringBuilder visitExtendsInterfaces(ExtendsInterfacesContext ctx) {
        space();
        appendKeyword("extends");
        space();
        visit(ctx.interfaceTypeList());
        return builder;
    }

    @Override
    public StringBuilder visitInterfaceBody(InterfaceBodyContext ctx) {
        startBlock();
        if (ctx.interfaceMemberDeclaration() != null) {
            for (var child : ctx.interfaceMemberDeclaration()) {
                visit(child);
            }
        }
        endBlock();
        return builder;
    }

    @Override
    public StringBuilder visitAnnotationTypeDeclaration(AnnotationTypeDeclarationContext ctx) {
        if (ctx.interfaceModifier() != null) {
            for (var child : ctx.interfaceModifier()) {
                visit(child);
                space();
            }
        }
        builder.append('@');
        appendKeyword("interface");
        space();
        visit(ctx.identifier());
        visit(ctx.annotationTypeBody());
        return builder;
    }

    @Override
    public StringBuilder visitAnnotationTypeBody(AnnotationTypeBodyContext ctx) {
        startBlock();
        if (ctx.annotationTypeMemberDeclaration() != null) {
            for (var child : ctx.annotationTypeMemberDeclaration()) {
                visit(child);
            }
        }
        endBlock();
        return builder;
    }

    @Override
    public StringBuilder visitInterfaceModifier(InterfaceModifierContext ctx) {
        if (ctx.annotation() != null) {
            visit(ctx.annotation());
        }
        if (ctx.PUBLIC() != null) {
            appendKeyword("public");
        }
        if (ctx.PRIVATE() != null) {
            appendKeyword("private");
        }
        if (ctx.ABSTRACT() != null) {
            appendKeyword("abstract");
        }
        if (ctx.PROTECTED() != null) {
            appendKeyword("protected");
        }
        if (ctx.STATIC() != null) {
            appendKeyword("static");
        }
        if (ctx.STRICTFP() != null) {
            appendKeyword("strictfp");
        }
        return builder;
    }

    @Override
    public StringBuilder visitClassModifier(ClassModifierContext ctx) {
        if (ctx.annotation() != null) {
            visit(ctx.annotation());
        }
        if (ctx.PUBLIC() != null) {
            appendKeyword("public");
        }
        if (ctx.PRIVATE() != null) {
            appendKeyword("private");
        }
        if (ctx.ABSTRACT() != null) {
            appendKeyword("abstract");
        }
        if (ctx.PROTECTED() != null) {
            appendKeyword("protected");
        }
        if (ctx.STATIC() != null) {
            appendKeyword("static");
        }
        if (ctx.FINAL() != null) {
            appendKeyword("final");
        }
        if (ctx.STRICTFP() != null) {
            appendKeyword("strictfp");
        }

        return builder;
    }

    @Override
    public StringBuilder visitIdentifier(IdentifierContext ctx) {
        builder.append(ctx.Identifier().getText());
        return builder;
    }

    @Override
    public StringBuilder visitClassBody(ClassBodyContext ctx) {
        startBlock();
        for (var child : ctx.children) {
            visit(child);
        }
        endBlock();
        return builder;
    }

    @Override
    public StringBuilder visitMethodDeclaration(MethodDeclarationContext ctx) {
        if (ctx.methodModifier() != null) {
            for (var child : ctx.methodModifier()) {
                visit(child);
                space();
            }
        }
        visit(ctx.methodHeader());
        visit(ctx.methodBody());
        return builder;
    }

    @Override
    public StringBuilder visitMethodHeader(MethodHeaderContext ctx) {
        if (ctx.typeParameters() != null) {
            visit(ctx.typeParameters());
            if (ctx.annotation() != null) {
                for (var child : ctx.annotation()) {
                    visit(child);
                    space();
                }
            }
        }

        visit(ctx.result());
        space();
        visit(ctx.methodDeclarator());
        if (ctx.throws_() != null) {
            visit(ctx.throws_());
        }
        return builder;
    }

    @Override
    public StringBuilder visitThrows_(Throws_Context ctx) {
        space();
        appendKeyword("throws");
        space();
        visit(ctx.exceptionTypeList());
        return builder;
    }

    @Override
    public StringBuilder visitInterfaceTypeList(InterfaceTypeListContext ctx) {
        if (ctx.interfaceType() != null) {
            boolean addComma = false;
            for (var child : ctx.interfaceType()) {
                if (addComma) {
                    builder.append(", ");
                }
                visit(child);
                addComma = true;
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitExceptionTypeList(ExceptionTypeListContext ctx) {
        if (ctx.exceptionType() != null) {
            boolean addComma = false;
            for (var child : ctx.exceptionType()) {
                if (addComma) {
                    builder.append(", ");
                }
                visit(child);
                addComma = true;
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitClassType(ClassTypeContext ctx) {
        if (ctx.classOrInterfaceType() != null) {
            visit(ctx.classOrInterfaceType());
            builder.append(".");
        }
        if (ctx.annotation() != null) {
            for (var child : ctx.annotation()) {
                visit(child);
                space();
            }
        }
        visit(ctx.identifier());
        if (ctx.typeArguments() != null) {
            visit(ctx.typeArguments());
        }
        return builder;
    }

    @Override
    public StringBuilder visitClassType_lf_classOrInterfaceType(ClassType_lf_classOrInterfaceTypeContext ctx) {
        builder.append(".");
        if (ctx.annotation() != null) {
            for (var child : ctx.annotation()) {
                visit(child);
                space();
            }
        }
        visit(ctx.identifier());
        if (ctx.typeArguments() != null) {
            visit(ctx.typeArguments());
        }
        return builder;
    }

    @Override
    public StringBuilder visitClassType_lfno_classOrInterfaceType(ClassType_lfno_classOrInterfaceTypeContext ctx) {
        if (ctx.annotation() != null) {
            for (var child : ctx.annotation()) {
                visit(child);
                space();
            }
        }
        visit(ctx.identifier());
        if (ctx.typeArguments() != null) {
            visit(ctx.typeArguments());
        }
        return builder;
    }

    @Override
    public StringBuilder visitPrimitiveType(PrimitiveTypeContext ctx) {
        if (ctx.annotation() != null) {
            for (var child : ctx.annotation()) {
                visit(child);
                space();
            }
        }
        if (ctx.numericType() != null) {
            visit(ctx.numericType());
        }
        if (ctx.BOOLEAN() != null) {
            appendKeyword("boolean");
        }
        return builder;
    }

    @Override
    public StringBuilder visitTypeParameter(TypeParameterContext ctx) {
        if (ctx.typeParameterModifier() != null) {
            for (var child : ctx.typeParameterModifier()) {
                visit(child);
                space();
            }
        }
        visit(ctx.identifier());
        if (ctx.typeBound() != null) {
            visit(ctx.typeBound());
        }
        return builder;
    }

    @Override
    public StringBuilder visitTypeBound(TypeBoundContext ctx) {
        space();
        appendKeyword("extends");
        space();
        if (ctx.typeVariable() != null) {
            visit(ctx.typeVariable());
        } else if (ctx.classOrInterfaceType() != null) {
            visit(ctx.classOrInterfaceType());
            if (ctx.additionalBound() != null) {
                for (var child : ctx.additionalBound()) {
                    visit(child);
                    space();
                }
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitAdditionalBound(AdditionalBoundContext ctx) {
        for (var child : ctx.children) {
            if (child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                builder.append(tn.getSymbol().getText());
            } else if (child instanceof InterfaceTypeContext) {
                visit(child);
                space();
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitTypeArgument(TypeArgumentContext ctx) {
        for (var child : ctx.children) {
            if (child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                builder.append(tn.getSymbol().getText());
            } else if (child instanceof TypeArgumentListContext) {
                visit(child);
                space();
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitTypeArgumentList(TypeArgumentListContext ctx) {
        if (ctx.typeArgument() != null) {
            boolean addComma = false;
            for (var child : ctx.typeArgument()) {
                if (addComma) {
                    builder.append(", ");
                }
                visit(child);
                addComma = true;
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitWildcard(WildcardContext ctx) {
        if (ctx.annotation() != null) {
            for (var child : ctx.annotation()) {
                visit(child);
                space();
            }
        }
        builder.append("?");
        if (ctx.wildcardBounds() != null) {
            visit(ctx.wildcardBounds());
        }
        return builder;
    }

    @Override
    public StringBuilder visitWildcardBounds(WildcardBoundsContext ctx) {
        for (var child : ctx.children) {
            if (child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                appendKeyword(tn.getSymbol().getText());
            } else if (child instanceof ReferenceTypeContext) {
                visit(child);
                space();
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitModuleName(ModuleNameContext ctx) {
        for (var child : ctx.children) {
            if (child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                builder.append(tn.getSymbol().getText());
            } else if (child instanceof IdentifierContext) {
                visit((IdentifierContext) child);
                space();
            } else if (child instanceof ModuleNameContext) {
                visit((ModuleNameContext) child);
                space();
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitModularCompilation(ModularCompilationContext ctx) {
        if (ctx.importDeclaration() != null) {
            for (var child : ctx.importDeclaration()) {
                visit(child);
                space();
            }
        }
        visit(ctx.moduleDeclaration());
        return builder;
    }

    @Override
    public StringBuilder visitTypeDeclaration(TypeDeclarationContext ctx) {
        for (var child : ctx.children) {
            if (child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                builder.append(tn.getSymbol().getText());
            } else if (child instanceof ClassDeclarationContext) {
                visit((ClassDeclarationContext) child);
                space();
            } else if (child instanceof InterfaceDeclarationContext) {
                visit((InterfaceDeclarationContext) child);
                space();
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitModuleDeclaration(ModuleDeclarationContext ctx) {
        if (ctx.annotation() != null) {
            for (var child : ctx.annotation()) {
                visit(child);
                space();
            }
        }
        if (ctx.OPEN() != null) {
            appendKeyword("open");
            space();
        }
        appendKeyword("module");
        space();
        visit(ctx.moduleName());
        builder.append("{");
        newLine();
        if (ctx.moduleDirective() != null) {
            for (var child : ctx.moduleDirective()) {
                visit(child);
                space();
            }
        }
        newLine();
        builder.append("}");
        return builder;
    }

    @Override
    public StringBuilder visitModuleDirective(ModuleDirectiveContext ctx) {
        if (ctx.REQUIRES() != null) {
            space();
            appendKeyword("reguires");
            space();
            if (ctx.requiresModifier() != null) {
                for (var child : ctx.requiresModifier()) {
                    visit(child);
                    space();
                }
            }
            for (var child : ctx.moduleName()) {
                visit(child);
                space();
            }
        } else if (ctx.EXPORTS() != null || ctx.OPERNS() != null) {
            space();
            if (ctx.EXPORTS() != null) {
                appendKeyword("exports");
            } else if (ctx.OPERNS() != null) {
                appendKeyword("opens");
            }
            space();
            visit(ctx.packageName());
            if (ctx.moduleName() != null) {
                boolean addComma = false;
                for (var child : ctx.moduleName()) {
                    if (addComma) {
                        builder.append(", ");
                    }
                    visit(child);
                    addComma = true;
                }
            }
        } else if (ctx.USES() != null) {
            space();
            appendKeyword("uses");
            space();
            for (var child : ctx.typeName()) {
                visit(child);
                space();
            }
        } else if (ctx.PROVIDES() != null) {
            space();
            appendKeyword("provides");
            space();
            for (var child : ctx.typeName()) {
                visit(child);
                space();
            }
            appendKeyword("with");
            space();
            if (ctx.typeName() != null) {
                boolean addComma = false;
                for (var child : ctx.typeName()) {
                    if (addComma) {
                        builder.append(", ");
                    }
                    visit(child);
                    addComma = true;
                }
            }
        }
        builder.append(";");
        return builder;
    }

    @Override
    public StringBuilder visitRequiresModifier(RequiresModifierContext ctx) {
        for (var child : ctx.children) {
            if (child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                appendKeyword(tn.getSymbol().getText());
                space();
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitTypeParameters(TypeParametersContext ctx) {
        for (var child : ctx.children) {
            if (child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                builder.append(tn.getSymbol().getText());
            } else if (child instanceof TypeParameterListContext) {
                visit(child);
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitTypeParameterList(TypeParameterListContext ctx) {
        if (ctx.typeParameter() != null) {
            boolean addComma = false;
            for (var child : ctx.typeParameter()) {
                if (addComma) {
                    builder.append(", ");
                }
                visit(child);
                addComma = true;
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitSuperclass(SuperclassContext ctx) {
        space();
        appendKeyword("super");
        space();
        visit(ctx.classType());
        return builder;
    }

    @Override
    public StringBuilder visitSuperinterfaces(SuperinterfacesContext ctx) {
        space();
        appendKeyword("implements");
        space();
        visit(ctx.interfaceTypeList());
        return builder;
    }

    @Override
    public StringBuilder visitClassMemberDeclaration(ClassMemberDeclarationContext ctx) {
        for (var child : ctx.children) {
            if (child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                builder.append(tn.getSymbol().getText());
            } else {
                visit(child);
                space();
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitFieldDeclaration(FieldDeclarationContext ctx) {
        if (ctx.fieldModifier() != null) {
            for (var child : ctx.fieldModifier()) {
                visit(child);
                space();
            }
        }
        visit(ctx.unannType());
        visit(ctx.variableDeclaratorList());
        builder.append(";");
        return super.visitFieldDeclaration(ctx);
    }

    @Override
    public StringBuilder visitFieldModifier(FieldModifierContext ctx) {
        if (ctx.annotation() != null) {
            visit(ctx.annotation());
        } else if (ctx.FINAL() != null) {
            appendKeyword("final");
            space();
        } else if (ctx.PRIVATE() != null) {
            appendKeyword("private");
            space();
        } else if (ctx.PROTECTED() != null) {
            appendKeyword("protected");
            space();
        } else if (ctx.STATIC() != null) {
            appendKeyword("static");
            space();
        } else if (ctx.TRANSIENT() != null) {
            appendKeyword("transient");
            space();
        } else if (ctx.VOLATILE() != null) {
            appendKeyword("volatile");
            space();
        }
        return builder;
    }

    @Override
    public StringBuilder visitVariableDeclaratorList(VariableDeclaratorListContext ctx) {
        if (ctx.variableDeclarator() != null) {
            boolean addComma = false;
            for (var child : ctx.variableDeclarator()) {
                if (addComma) {
                    builder.append(", ");
                }
                visit(child);
                addComma = true;
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitVariableDeclarator(VariableDeclaratorContext ctx) {
        visit(ctx.variableDeclaratorId());
        if (ctx.ASSIGN() != null) {
            visit(ctx.variableInitializer());
        }
        return builder;
    }

    @Override
    public StringBuilder visitVariableDeclaratorId(VariableDeclaratorIdContext ctx) {
        visit(ctx.identifier());
        if (ctx.dims() != null) {
            visit(ctx.dims());
        }
        return builder;
    }

    @Override
    public StringBuilder visitReceiverParameter(ReceiverParameterContext ctx) {
        if (ctx.annotation() != null) {
            for (var child : ctx.annotation()) {
                visit(child);
                space();
            }
        }
        visit(ctx.unannType());
        if (ctx.identifier() != null) {
            visit(ctx.identifier());
            builder.append(".");
        }
        appendKeyword("this");
        return builder;
    }

    @Override
    public StringBuilder visitStaticInitializer(StaticInitializerContext ctx) {
        appendKeyword("static");
        visit(ctx.block());
        return super.visitStaticInitializer(ctx);
    }

    @Override
    public StringBuilder visitConstructorDeclaration(ConstructorDeclarationContext ctx) {
        if (ctx.constructorModifier() != null) {
            for (var child : ctx.constructorModifier()) {
                visit(child);
                space();
            }
        }
        visit(ctx.constructorDeclarator());
        if (ctx.throws_() != null) {
            visit(ctx.throws_());
            space();
        }
        visit(ctx.constructorBody());
        return builder;
    }

    @Override
    public StringBuilder visitConstructorModifier(ConstructorModifierContext ctx) {
        if (ctx.annotation() != null) {
            visit(ctx.annotation());
        } else if (ctx.PRIVATE() != null) {
            appendKeyword("private");
        } else if (ctx.PROTECTED() != null) {
            appendKeyword("protected");
        } else if (ctx.PUBLIC() != null) {
            appendKeyword("public");
        }
        return builder;
    }

    @Override
    public StringBuilder visitConstructorDeclarator(ConstructorDeclaratorContext ctx) {
        if (ctx.typeParameters() != null) {
            visit(ctx.typeParameters());
        }
        visit(ctx.simpleTypeName());
        builder.append("(");
        if (ctx.formalParameterList() != null) {
            visit(ctx.formalParameterList());
        }
        builder.append(")");
        return builder;
    }

    @Override
    public StringBuilder visitConstructorBody(ConstructorBodyContext ctx) {
        builder.append("{");
        newLine();
        if (ctx.explicitConstructorInvocation() != null) {
            visit(ctx.explicitConstructorInvocation());
        }
        if (ctx.blockStatements() != null) {
            visit(ctx.blockStatements());
        }
        newLine();
        builder.append("}");
        return builder;
    }

    @Override
    public StringBuilder visitExplicitConstructorInvocation(ExplicitConstructorInvocationContext ctx) {
        if (ctx.typeArguments() != null) {
            visit(ctx.typeArguments());
            if (ctx.SUPER() != null) {
                appendKeyword("super");
            } else if (ctx.THIS() != null) {
                appendKeyword("this");
            }
            builder.append("(");
            if (ctx.argumentList() != null) {
                visit(ctx.argumentList());
            }
            builder.append(")");
            builder.append(";");
        } else if (ctx.expressionName() != null) {
            visit(ctx.expressionName());
            builder.append(".");
            appendKeyword("super");
            builder.append("(");
            if (ctx.argumentList() != null) {
                visit(ctx.argumentList());
            }
            builder.append(")");
            builder.append(";");
        } else if (ctx.primary() != null) {
            visit(ctx.primary());
            builder.append(".");
            appendKeyword("super");
            builder.append("(");
            if (ctx.argumentList() != null) {
                visit(ctx.argumentList());
            }
            builder.append(")");
            builder.append(";");
        }
        return super.visitExplicitConstructorInvocation(ctx);
    }

    @Override
    public StringBuilder visitEnumDeclaration(EnumDeclarationContext ctx) {
        if (ctx.classModifier() != null) {
            for (var child : ctx.children) {
                visit(child);
                space();
            }
        }
        appendKeyword("enum");
        visit(ctx.identifier());
        space();
        if (ctx.superinterfaces() != null) {
            visit(ctx.superinterfaces());
            space();
        }
        visit(ctx.enumBody());
        return super.visitEnumDeclaration(ctx);
    }

    @Override
    public StringBuilder visitEnumBody(EnumBodyContext ctx) {
        builder.append("{");
        newLine();
        if (ctx.enumConstantList() != null) {
            visit(ctx.enumConstantList());
            space();
        }
        if (ctx.COMMA() != null) {
            builder.append(",");
            space();
        }
        if (ctx.enumBodyDeclarations() != null) {
            visit(ctx.enumBodyDeclarations());
        }
        newLine();
        builder.append("}");
        return super.visitEnumBody(ctx);
    }

    @Override
    public StringBuilder visitEnumConstantList(EnumConstantListContext ctx) {
        if (ctx.enumConstant() != null) {
            boolean addComma = false;
            for (var child : ctx.enumConstant()) {
                if (addComma) {
                    builder.append(", ");
                }
                visit(child);
                addComma = true;
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitEnumConstant(EnumConstantContext ctx) {
        if (ctx.enumConstantModifier() != null) {
            for (var child : ctx.children) {
                visit(child);
                space();
            }
        }
        visit(ctx.identifier());
        space();
        if (ctx.argumentList() != null) {
            builder.append("(");
            visit(ctx.argumentList());
            builder.append(")");
        }
        if (ctx.classBody() != null) {
            visit(ctx.classBody());
        }
        return builder;
    }

    @Override
    public StringBuilder visitEnumBodyDeclarations(EnumBodyDeclarationsContext ctx) {
        builder.append(";");
        if (ctx.classBodyDeclaration() != null) {
            for (var child : ctx.classBodyDeclaration()) {
                visit(child);
            }
            space();
        }
        return builder;
    }

    @Override
    public StringBuilder visitInterfaceMethodModifier(InterfaceMethodModifierContext ctx) {
        if (ctx.annotation() != null) {
            visit(ctx.annotation());
            space();
        } else if (ctx.ABSTRACT() != null) {
            appendKeyword("abstract");
            space();
        } else if (ctx.PRIVATE() != null) {
            appendKeyword("private");
            space();
        } else if (ctx.DEFAULT() != null) {
            appendKeyword("default");
            space();
        } else if (ctx.PUBLIC() != null) {
            appendKeyword("public");
            space();
        } else if (ctx.STATIC() != null) {
            appendKeyword("static");
            space();
        } else if (ctx.STRICTFP() != null) {
            appendKeyword("strictfp");
            space();
        }
        return builder;
    }

    @Override
    public StringBuilder visitInterfaceMemberDeclaration(InterfaceMemberDeclarationContext ctx) {
        if (ctx.constantDeclaration() != null) {
            visit(ctx.constantDeclaration());
            space();
        } else if (ctx.interfaceMethodDeclaration() != null) {
            visit(ctx.interfaceMethodDeclaration());
            space();
        } else if (ctx.classDeclaration() != null) {
            visit(ctx.classDeclaration());
            space();
        } else if (ctx.interfaceDeclaration() != null) {
            visit(ctx.interfaceDeclaration());
            space();
        } else if (ctx.SEMI() != null) {
            builder.append(";");
        }
        return builder;
    }

    @Override
    public StringBuilder visitConstantDeclaration(ConstantDeclarationContext ctx) {
        if (ctx.constantModifier() != null) {
            for (var child : ctx.children) {
                visit(child);
                space();
            }
        }
        visit(ctx.unannType());
        space();
        visit(ctx.variableDeclaratorList());
        builder.append(";");
        return builder;
    }

    @Override
    public StringBuilder visitConstantModifier(ConstantModifierContext ctx) {
        if (ctx.annotation() != null) {
            visit(ctx.annotation());
            space();
        } else if (ctx.FINAL() != null) {
            appendKeyword("final");
            space();
        } else if (ctx.PUBLIC() != null) {
            appendKeyword("public");
            space();
        } else if (ctx.STATIC() != null) {
            appendKeyword("static");
            space();
        }
        return builder;
    }

    @Override
    public StringBuilder visitInterfaceMethodDeclaration(InterfaceMethodDeclarationContext ctx) {
        if (ctx.interfaceMethodModifier() != null) {
            for (var child : ctx.interfaceMethodModifier()) {
                visit(child);
                space();
            }
        }
        visit(ctx.methodHeader());
        space();
        visit(ctx.methodBody());
        return builder;
    }

    @Override
    public StringBuilder visitAnnotationTypeMemberDeclaration(AnnotationTypeMemberDeclarationContext ctx) {
        if (ctx.annotationTypeElementDeclaration() != null) {
            visit(ctx.annotationTypeElementDeclaration());
            space();
        } else if (ctx.classDeclaration() != null) {
            visit(ctx.classDeclaration());
            space();
        } else if (ctx.constantDeclaration() != null) {
            visit(ctx.constantDeclaration());
            space();
        } else if (ctx.interfaceDeclaration() != null) {
            visit(ctx.interfaceDeclaration());
            space();
        } else if (ctx.SEMI() != null) {
            builder.append(";");
        }
        return builder;
    }

    @Override
    public StringBuilder visitAnnotationTypeElementDeclaration(AnnotationTypeElementDeclarationContext ctx) {
        if (ctx.annotationTypeElementModifier() != null) {
            for (var child : ctx.annotationTypeElementModifier()) {
                visit(child);
                space();
            }
        }
        visit(ctx.unannType());
        space();
        visit(ctx.identifier());
        space();
        builder.append("()");
        if (ctx.dims() != null) {
            visit(ctx.dims());
            space();
        }
        if (ctx.defaultValue() != null) {
            visit(ctx.defaultValue());
        }
        builder.append(";");
        return builder;
    }

    @Override
    public StringBuilder visitAnnotationTypeElementModifier(AnnotationTypeElementModifierContext ctx) {
        if (ctx.annotation() != null) {
            visit(ctx.annotation());
        } else if (ctx.ABSTRACT() != null) {
            appendKeyword("abstract");
            space();
        } else if (ctx.PUBLIC() != null) {
            appendKeyword("public");
            space();
        }
        return builder;
    }

    @Override
    public StringBuilder visitDefaultValue(DefaultValueContext ctx) {
        space();
        appendKeyword("default");
        space();
        visit(ctx.elementValue());
        space();
        return builder;
    }

    @Override
    public StringBuilder visitElementValuePairList(ElementValuePairListContext ctx) {
        if (ctx.elementValuePair() != null) {
            boolean addComma = false;
            for (var child : ctx.elementValuePair()) {
                if (addComma) {
                    builder.append(", ");
                }
                visit(child);
                addComma = true;
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitElementValuePair(ElementValuePairContext ctx) {
        visit(ctx.identifier());
        space();
        builder.append("=");
        space();
        visit(ctx.elementValue());
        return builder;
    }

    @Override
    public StringBuilder visitElementValueArrayInitializer(ElementValueArrayInitializerContext ctx) {
        builder.append("{");
        if (ctx.elementValueList() != null) {
            visit(ctx.elementValueList());
        }
        if (ctx.COMMA() != null) {
            builder.append(",");
            space();
        }
        builder.append("}");
        return builder;
    }

    @Override
    public StringBuilder visitElementValueList(ElementValueListContext ctx) {
        if (ctx.elementValue() != null) {
            boolean addComma = false;
            for (var child : ctx.elementValue()) {
                if (addComma) {
                    builder.append(", ");
                }
                visit(child);
                addComma = true;
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitArrayInitializer(ArrayInitializerContext ctx) {
        builder.append("{");
        if (ctx.variableInitializerList() != null) {
            visit(ctx.variableInitializerList());
        }
        if (ctx.COMMA() != null) {
            builder.append(",");
            space();
        }
        builder.append("}");
        return builder;
    }

    @Override
    public StringBuilder visitVariableInitializerList(VariableInitializerListContext ctx) {
        if (ctx.variableInitializer() != null) {
            boolean addComma = false;
            for (var child : ctx.variableInitializer()) {
                if (addComma) {
                    builder.append(", ");
                }
                visit(child);
                addComma = true;
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitBlockStatements(BlockStatementsContext ctx) {
        for (var child : ctx.blockStatement()) {
            visit(child);
            space();
        }
        return builder;
    }

    @Override
    public StringBuilder visitLocalVariableDeclarationStatement(LocalVariableDeclarationStatementContext ctx) {
        visit(ctx.localVariableDeclaration());
        builder.append(";");
        return builder;
    }

    @Override
    public StringBuilder visitLocalVariableDeclaration(LocalVariableDeclarationContext ctx) {
        if (ctx.variableModifier() != null) {
            for (var child : ctx.variableModifier()) {
                visit(child);
                space();
            }
        }
        visit(ctx.unannType());
        space();
        visit(ctx.variableDeclaratorList());
        return builder;
    }

    @Override
    public StringBuilder visitEmptyStatement_(EmptyStatement_Context ctx) {
        builder.append(";");
        return builder;
    }

    @Override
    public StringBuilder visitLabeledStatement(LabeledStatementContext ctx) {
        visit(ctx.identifier());
        space();
        builder.append(":");
        space();
        visit(ctx.statement());
        return builder;
    }

    @Override
    public StringBuilder visitLabeledStatementNoShortIf(LabeledStatementNoShortIfContext ctx) {
        visit(ctx.identifier());
        space();
        builder.append(":");
        space();
        visit(ctx.statementNoShortIf());
        return builder;
    }

    @Override
    public StringBuilder visitExpressionStatement(ExpressionStatementContext ctx) {
        visit(ctx.statementExpression());
        builder.append(";");
        return builder;
    }

    @Override
    public StringBuilder visitIfThenStatement(IfThenStatementContext ctx) {
        appendKeyword("if");
        builder.append("(");
        visit(ctx.expression());
        builder.append(")");
        space();
        visit(ctx.statement());
        return builder;
    }

    @Override
    public StringBuilder visitIfThenElseStatement(IfThenElseStatementContext ctx) {
        appendKeyword("if");
        builder.append("(");
        visit(ctx.expression());
        builder.append(")");
        space();
        visit(ctx.statementNoShortIf());
        space();
        appendKeyword("else");
        space();
        visit(ctx.statement());
        return builder;
    }

    @Override
    public StringBuilder visitIfThenElseStatementNoShortIf(IfThenElseStatementNoShortIfContext ctx) {
        appendKeyword("if");
        builder.append("(");
        visit(ctx.expression());
        builder.append(")");
        space();
        for (var child : ctx.children) {
            if (child instanceof StatementNoShortIfContext) {
                visit(child);
                space();
                break;
            }
        }
        space();
        appendKeyword("else");
        space();
        for (var child : ctx.children) {
            if (child instanceof StatementNoShortIfContext) {
                visit(child);
                space();
                break;
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitAssertStatement(AssertStatementContext ctx) {
        for (var child : ctx.children) {
            if (child instanceof ExpressionContext) {
                visit(child);
                space();
            } else if (child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                appendKeyword(tn.getSymbol().getText());
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitSwitchStatement(SwitchStatementContext ctx) {
        appendKeyword("switch");
        builder.append("(");
        visit(ctx.expression());
        builder.append(")");
        space();
        visit(ctx.switchBlock());
        return builder;
    }

    @Override
    public StringBuilder visitSwitchBlock(SwitchBlockContext ctx) {
        space();
        builder.append("{");
        newLine();
        if (ctx.switchBlockStatementGroup() != null) {
            for (var child : ctx.switchBlockStatementGroup()) {
                visit(child);
                space();
            }
        }
        if (ctx.switchLabel() != null) {
            for (var child : ctx.switchLabel()) {
                visit(child);
                space();
            }
        }
        newLine();
        builder.append("}");
        newLine();
        return builder;
    }

    @Override
    public StringBuilder visitSwitchBlockStatementGroup(SwitchBlockStatementGroupContext ctx) {
        visit(ctx.switchLabels());
        space();
        visit(ctx.blockStatements());
        return builder;
    }

    @Override
    public StringBuilder visitSwitchLabels(SwitchLabelsContext ctx) {
        for (var child : ctx.switchLabel()) {
            visit(child);
            space();
        }
        return builder;
    }

    @Override
    public StringBuilder visitSwitchLabel(SwitchLabelContext ctx) {
        if (ctx.CASE() != null) {
            appendKeyword("case");
            space();
            if (ctx.constantExpression() != null) {
                visit(ctx.constantExpression());
            } else if (ctx.enumConstantName() != null) {
                visit(ctx.enumConstantName());
            }
        } else if (ctx.DEFAULT() != null) {
            appendKeyword("default");
        }
        builder.append(":");
        space();
        return builder;
    }

    @Override
    public StringBuilder visitWhileStatement(WhileStatementContext ctx) {
        appendKeyword("while");
        builder.append("(");
        visit(ctx.expression());
        builder.append(")");
        space();
        newLine();
        visit(ctx.statement());
        newLine();
        return builder;
    }

    @Override
    public StringBuilder visitWhileStatementNoShortIf(WhileStatementNoShortIfContext ctx) {
        appendKeyword("while");
        builder.append("(");
        visit(ctx.expression());
        builder.append(")");
        space();
        newLine();
        visit(ctx.statementNoShortIf());
        newLine();
        return builder;
    }

    @Override
    public StringBuilder visitDoStatement(DoStatementContext ctx) {
        appendKeyword("do");
        space();
        newLine();
        visit(ctx.statement());
        newLine();
        appendKeyword("while");
        builder.append("(");
        visit(ctx.expression());
        builder.append(")");
        builder.append(";");
        return builder;
    }

    @Override
    public StringBuilder visitBasicForStatement(BasicForStatementContext ctx) {
        appendKeyword("for");
        builder.append("(");
        if (ctx.forInit() != null) {
            visit(ctx.forInit());
        }
        builder.append(";");
        if (ctx.expression() != null) {
            visit(ctx.expression());
        }
        builder.append(";");
        if (ctx.forUpdate() != null) {
            visit(ctx.forUpdate());
        }
        builder.append(")");
        space();
        visit(ctx.statement());
        return builder;
    }

    @Override
    public StringBuilder visitBasicForStatementNoShortIf(BasicForStatementNoShortIfContext ctx) {
        appendKeyword("for");
        builder.append("(");
        if (ctx.forInit() != null) {
            visit(ctx.forInit());
        }
        builder.append(";");
        if (ctx.expression() != null) {
            visit(ctx.expression());
        }
        builder.append(";");
        if (ctx.forUpdate() != null) {
            visit(ctx.forUpdate());
        }
        builder.append(")");
        space();
        visit(ctx.statementNoShortIf());
        return builder;
    }

    @Override
    public StringBuilder visitStatementExpressionList(StatementExpressionListContext ctx) {
        if (ctx.statementExpression() != null) {
            boolean addComma = false;
            for (var child : ctx.statementExpression()) {
                if (addComma) {
                    builder.append(", ");
                }
                visit(child);
                addComma = true;
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitEnhancedForStatement(EnhancedForStatementContext ctx) {
        appendKeyword("for");
        builder.append("(");
        if (ctx.variableModifier() != null) {
            for (var child : ctx.variableModifier()) {
                visit(child);
                space();
            }
        }
        visit(ctx.unannType());
        visit(ctx.variableDeclaratorId());
        builder.append(":");
        if (ctx.expression() != null) {
            visit(ctx.expression());
        }
        builder.append(")");
        space();
        visit(ctx.statement());
        return builder;
    }

    @Override
    public StringBuilder visitEnhancedForStatementNoShortIf(EnhancedForStatementNoShortIfContext ctx) {
        appendKeyword("for");
        builder.append("(");
        if (ctx.variableModifier() != null) {
            for (var child : ctx.variableModifier()) {
                visit(child);
                space();
            }
        }
        visit(ctx.unannType());
        visit(ctx.variableDeclaratorId());
        builder.append(":");
        if (ctx.expression() != null) {
            visit(ctx.expression());
        }
        builder.append(")");
        space();
        visit(ctx.statementNoShortIf());
        return builder;
    }

    @Override
    public StringBuilder visitBreakStatement(BreakStatementContext ctx) {
        appendKeyword("break");
        space();
        if (ctx.identifier() != null) {
            visit(ctx.identifier());
        }
        builder.append(";");
        return builder;
    }

    @Override
    public StringBuilder visitContinueStatement(ContinueStatementContext ctx) {
        appendKeyword("continue");
        space();
        if (ctx.identifier() != null) {
            visit(ctx.identifier());
        }
        builder.append(";");
        return builder;
    }

    @Override
    public StringBuilder visitReturnStatement(ReturnStatementContext ctx) {
        appendKeyword("return");
        space();
        if (ctx.expression() != null) {
            visit(ctx.expression());
        }
        builder.append(";");
        return builder;
    }

    @Override
    public StringBuilder visitThrowStatement(ThrowStatementContext ctx) {
        appendKeyword("throw");
        space();
        visit(ctx.expression());
        builder.append(";");
        return builder;
    }

    @Override
    public StringBuilder visitSynchronizedStatement(SynchronizedStatementContext ctx) {
        appendKeyword("synchronized");
        space();
        builder.append("(");
        visit(ctx.expression());
        builder.append(")");
        space();
        visit(ctx.block());
        builder.append(";");
        return builder;
    }

    @Override
    public StringBuilder visitTryStatement(TryStatementContext ctx) {
        if (ctx.TRY() != null) {
            appendKeyword("try");
            space();
            visit(ctx.block());
            space();
            if (ctx.catches() != null) {
                visit(ctx.catches());
            }
            if (ctx.finally_() != null) {
                visit(ctx.finally_());
            }
        } else if (ctx.tryWithResourcesStatement() != null) {
            visit(ctx.tryWithResourcesStatement());
        }
        return builder;
    }

    @Override
    public StringBuilder visitCatches(CatchesContext ctx) {
        for (var child : ctx.catchClause()) {
            visit(child);
            space();
        }
        return builder;
    }

    @Override
    public StringBuilder visitCatchClause(CatchClauseContext ctx) {
        appendKeyword("catch");
        builder.append("(");
        visit(ctx.catchFormalParameter());
        builder.append(")");
        visit(ctx.block());
        return builder;
    }

    @Override
    public StringBuilder visitCatchFormalParameter(CatchFormalParameterContext ctx) {
        if (ctx.variableModifier() != null) {
            for (var child : ctx.variableModifier()) {
                visit(child);
                space();
            }
        }
        visit(ctx.catchType());
        visit(ctx.variableDeclaratorId());
        return super.visitCatchFormalParameter(ctx);
    }

    @Override
    public StringBuilder visitCatchType(CatchTypeContext ctx) {
        visit(ctx.unannClassType());
        if (ctx.classType() != null) {
            for (var child : ctx.classType()) {
                builder.append(" | ");
                visit(child);
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitFinally_(Finally_Context ctx) {
        appendKeyword("finally");
        visit(ctx.block());
        return builder;
    }

    @Override
    public StringBuilder visitTryWithResourcesStatement(TryWithResourcesStatementContext ctx) {
        appendKeyword("try");
        space();
        visit(ctx.resourceSpecification());
        space();
        visit(ctx.block());
        space();
        if (ctx.catches() != null) {
            visit(ctx.catches());
            space();
        }
        if (ctx.finally_() != null) {
            visit(ctx.finally_());
        }
        return builder;
    }

    @Override
    public StringBuilder visitResourceSpecification(ResourceSpecificationContext ctx) {
        builder.append("(");
        if (ctx.resourceList() != null) {
            visit(ctx.resourceList());
        }
        if (ctx.SEMI() != null) {
            builder.append(";");
        }
        builder.append(")");
        return builder;
    }

    @Override
    public StringBuilder visitResourceList(ResourceListContext ctx) {
        if (ctx.resource() != null) {
            boolean addSemiColon = false;
            for (var child : ctx.resource()) {
                visit(child);
                if (addSemiColon) {
                    builder.append(";");
                }
                space();
                addSemiColon = true;
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitResource(ResourceContext ctx) {
        if (ctx.unannType() != null) {
            for (var child : ctx.variableModifier()) {
                visit(child);
                space();
            }
            visit(ctx.unannType());
            visit(ctx.variableDeclaratorId());
            builder.append("=");
            visit(ctx.expression());
        } else if (ctx.variableAccess() != null) {
            visit(ctx.variableAccess());
        }
        return builder;
    }

    @Override
    public StringBuilder visitPrimary(PrimaryContext ctx) {
        if (ctx.primaryNoNewArray_lfno_primary() != null) {
            visit(ctx.primaryNoNewArray_lfno_primary());
        } else if (ctx.arrayCreationExpression() != null) {
            visit(ctx.arrayCreationExpression());
        }
        if (ctx.primaryNoNewArray_lf_primary() != null) {
            for (var child : ctx.primaryNoNewArray_lf_primary()) {
                visit(child);
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitPrimaryNoNewArray(PrimaryNoNewArrayContext ctx) {
        if (ctx.literal() != null) {
            visit(ctx.literal());
        } else if (ctx.classInstanceCreationExpression() != null) {
            visit(ctx.classInstanceCreationExpression());
        } else if (ctx.fieldAccess() != null) {
            visit(ctx.fieldAccess());
        } else if (ctx.methodInvocation() != null) {
            visit(ctx.methodInvocation());
        } else if (ctx.arrayAccess() != null) {
            visit(ctx.arrayAccess());
        } else if (ctx.methodReference() != null) {
            visit(ctx.methodReference());
        } else if (ctx.THIS() != null) {
            appendKeyword("this");
        } else if (ctx.classLiteral() != null) {
            visit(ctx.classLiteral());
        } else if (ctx.typeName() != null) {
            visit(ctx.typeName());
            builder.append(".");
            appendKeyword("this");
        }
        return builder;
    }

    @Override
    public StringBuilder visitPrimaryNoNewArray_lfno_arrayAccess(PrimaryNoNewArray_lfno_arrayAccessContext ctx) {
        if (ctx.literal() != null) {
            visit(ctx.literal());
        } else if (ctx.typeName() != null) {
            visit(ctx.typeName());
            int size = 0;
            if (ctx.LBRACK() != null && ctx.RBRACK() != null) {
                size = ctx.LBRACK().size();
                for (int i = 0; i < size; i++) {
                    builder.append("[");
                    builder.append("]");
                }
            }
            builder.append(".");
            if (size == 0) {
                appendKeyword("this");
            } else {
                appendKeyword("class");
            }
        } else if (ctx.VOID() != null) {
            appendKeyword("void");
            builder.append(".");
            appendKeyword("class");
        } else if (ctx.THIS() != null) {
            appendKeyword("this");
        } else if (ctx.classInstanceCreationExpression() != null) {
            visit(ctx.classInstanceCreationExpression());
        } else if (ctx.fieldAccess() != null) {
            visit(ctx.fieldAccess());
        } else if (ctx.methodInvocation() != null) {
            visit(ctx.methodInvocation());
        } else if (ctx.methodReference() != null) {
            visit(ctx.methodReference());
        } else if (ctx.expression() != null) {
            builder.append("(");
            visit(ctx.expression());
            builder.append(")");
        }
        return builder;
    }

    @Override
    public StringBuilder visitPrimaryNoNewArray_lfno_primary(PrimaryNoNewArray_lfno_primaryContext ctx) {
        if (ctx.literal() != null) {
            visit(ctx.literal());
        } else if (ctx.classInstanceCreationExpression_lfno_primary() != null) {
            visit(ctx.classInstanceCreationExpression_lfno_primary());
        } else if (ctx.fieldAccess_lfno_primary() != null) {
            visit(ctx.fieldAccess_lfno_primary());
        } else if (ctx.methodInvocation_lfno_primary() != null) {
            visit(ctx.methodInvocation_lfno_primary());
        } else if (ctx.arrayAccess_lfno_primary() != null) {
            visit(ctx.arrayAccess_lfno_primary());
        } else if (ctx.methodReference_lfno_primary() != null) {
            visit(ctx.methodReference_lfno_primary());
        } else if (ctx.THIS() != null) {
            appendKeyword("this");
        } else if (ctx.typeName() != null) {
            visit(ctx.typeName());
            int size = 0;
            if (ctx.LBRACK() != null && ctx.RBRACK() != null) {
                size = ctx.LBRACK().size();
                for (int i = 0; i < size; i++) {
                    builder.append("[");
                    builder.append("]");
                }
            }
            builder.append(".");
            if (size == 0) {
                appendKeyword("this");
            } else {
                appendKeyword("class");
            }
        } else if (ctx.VOID() != null) {
            appendKeyword("void");
            builder.append(".");
            appendKeyword("class");
        } else if (ctx.unannPrimitiveType() != null) {
            visit(ctx.unannPrimitiveType());
            int size = 0;
            if (ctx.LBRACK() != null && ctx.RBRACK() != null) {
                size = ctx.LBRACK().size();
                for (int i = 0; i < size; i++) {
                    builder.append("[");
                    builder.append("]");
                }
            }
            builder.append(".");
            if (size == 0) {
                appendKeyword("this");
            } else {
                appendKeyword("class");
            }
        } else if (ctx.expression() != null) {
            builder.append("(");
            visit(ctx.expression());
            builder.append(")");
        }
        return builder;
    }

    @Override
    public StringBuilder visitPrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primary(
            PrimaryNoNewArray_lfno_primary_lfno_arrayAccess_lfno_primaryContext ctx) {
        if (ctx.literal() != null) {
            visit(ctx.literal());
        } else if (ctx.classInstanceCreationExpression_lfno_primary() != null) {
            visit(ctx.classInstanceCreationExpression_lfno_primary());
        } else if (ctx.fieldAccess_lfno_primary() != null) {
            visit(ctx.fieldAccess_lfno_primary());
        } else if (ctx.methodInvocation_lfno_primary() != null) {
            visit(ctx.methodInvocation_lfno_primary());
        } else if (ctx.methodReference_lfno_primary() != null) {
            visit(ctx.methodReference_lfno_primary());
        } else if (ctx.THIS() != null) {
            appendKeyword("this");
        } else if (ctx.typeName() != null) {
            visit(ctx.typeName());
            int size = 0;
            if (ctx.LBRACK() != null && ctx.RBRACK() != null) {
                size = ctx.LBRACK().size();
                for (int i = 0; i < size; i++) {
                    builder.append("[");
                    builder.append("]");
                }
            }
            builder.append(".");
            if (size == 0) {
                appendKeyword("this");
            } else {
                appendKeyword("class");
            }
        } else if (ctx.VOID() != null) {
            appendKeyword("void");
            builder.append(".");
            appendKeyword("class");
        } else if (ctx.unannPrimitiveType() != null) {
            visit(ctx.unannPrimitiveType());
            int size = 0;
            if (ctx.LBRACK() != null && ctx.RBRACK() != null) {
                size = ctx.LBRACK().size();
                for (int i = 0; i < size; i++) {
                    builder.append("[");
                    builder.append("]");
                }
            }
            builder.append(".");
            if (size == 0) {
                appendKeyword("this");
            } else {
                appendKeyword("class");
            }
        } else if (ctx.expression() != null) {
            builder.append("(");
            visit(ctx.expression());
            builder.append(")");
        }
        return builder;
    }

    @Override
    public StringBuilder visitClassLiteral(ClassLiteralContext ctx) {
        if(ctx.VOID() != null) {
            appendKeyword("void");
            builder.append(".");
            appendKeyword("class");
        } else {
            if(ctx.typeName() != null) {
                visit(ctx.typeName());
            } else if(ctx.numericType() != null) {
                visit(ctx.numericType());
            } else if(ctx.BOOLEAN() != null) {
                appendKeyword("boolean");
            }
            int size = 0;
            if (ctx.LBRACK() != null && ctx.RBRACK() != null) {
                size = ctx.LBRACK().size();
                for (int i = 0; i < size; i++) {
                    builder.append("[");
                    builder.append("]");
                }
            }
            builder.append(".");
            appendKeyword("class");
        }
        return builder;
    }

    @Override
    public StringBuilder visitClassInstanceCreationExpression(ClassInstanceCreationExpressionContext ctx) {
        for(var child: ctx.children) {
            if(child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                appendKeyword(tn.getSymbol().getText());
            } else {
                visit(child);
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitTypeVariable(TypeVariableContext ctx) {
        if (ctx.annotation() != null) {
            for (var child : ctx.annotation()) {
                visit(child);
                space();
            }
        }
        visit(ctx.identifier());
        return builder;
    }

    @Override
    public StringBuilder visitMethodDeclarator(MethodDeclaratorContext ctx) {
        visit(ctx.identifier());
        builder.append('(');
        if (ctx.formalParameterList() != null) {
            visit(ctx.formalParameterList());
        }
        if (ctx.dims() != null) {
            visit(ctx.dims());
        }
        builder.append(')');
        return builder;
    }

    @Override
    public StringBuilder visitFormalParameter(FormalParameterContext ctx) {
        if (ctx.variableModifier() != null) {
            for (var child : ctx.variableModifier()) {
                visit(child);
                space();
            }
        }
        visit(ctx.unannType());
        space();
        visit(ctx.variableDeclaratorId());
        return builder;
    }

    @Override
    public StringBuilder visitFormalParameterList(FormalParameterListContext ctx) {
        if (ctx.formalParameters() != null) {
            visit(ctx.formalParameters());
            builder.append(", ");
            visit(ctx.lastFormalParameter());
        } else if (ctx.lastFormalParameter() != null) {
            visit(ctx.lastFormalParameter());
        } else if (ctx.receiverParameter() != null) {
            visit(ctx.receiverParameter());
        }
        return builder;
    }

    @Override
    public StringBuilder visitFormalParameters(FormalParametersContext ctx) {
        boolean addComma = false;
        if (ctx.receiverParameter() != null) {
            visit(ctx.receiverParameter());
        }
        if (ctx.formalParameter() != null) {
            for (var child : ctx.formalParameter()) {
                if (addComma) {
                    builder.append(", ");
                }
                visit(child);
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitLastFormalParameter(LastFormalParameterContext ctx) {
        if (ctx.formalParameter() != null) {
            visit(ctx.formalParameter());
        } else {
            if (ctx.variableModifier() != null) {
                for (var child : ctx.variableModifier()) {
                    visit(child);
                    space();
                }
            }
            visit(ctx.unannType());
            space();
            if (ctx.annotation() != null) {
                for (var child : ctx.annotation()) {
                    visit(child);
                    space();
                }
            }
            builder.append(" ...");
            visit(ctx.variableDeclaratorId());
        }
        return builder;
    }

    @Override
    public StringBuilder visitVariableModifier(VariableModifierContext ctx) {
        if (ctx.FINAL() != null) {
            appendKeyword("final");
        } else if (ctx.annotation() != null) {
            visit(ctx.annotation());
        }
        return builder;
    }

    @Override
    public StringBuilder visitDims(DimsContext ctx) {
        for (var child : ctx.children) {
            if (child instanceof AnnotationContext) {
                visit(child);
                space();
            } else if (child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                builder.append(tn.getSymbol().getText());
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitResult(ResultContext ctx) {
        if (ctx.VOID() != null) {
            appendKeyword("void");
        } else if (ctx.unannType() != null) {
            visit(ctx.unannType());
        }
        return builder;
    }

    @Override
    public StringBuilder visitUnannPrimitiveType(UnannPrimitiveTypeContext ctx) {
        if (ctx.BOOLEAN() != null) {
            appendKeyword("boolean");
        } else if (ctx.numericType() != null) {
            visit(ctx.numericType());
        }
        return builder;
    }

    @Override
    public StringBuilder visitIntegralType(IntegralTypeContext ctx) {
        if (ctx.BYTE() != null) {
            appendKeyword("byte");
        }
        if (ctx.CHAR() != null) {
            appendKeyword("char");
        }
        if (ctx.INT() != null) {
            appendKeyword("int");
        }
        if (ctx.LONG() != null) {
            appendKeyword("long");
        }
        if (ctx.SHORT() != null) {
            appendKeyword("short");
        }
        return builder;
    }

    @Override
    public StringBuilder visitFloatingPointType(FloatingPointTypeContext ctx) {
        if (ctx.DOUBLE() != null) {
            appendKeyword("double");
        }
        if (ctx.FLOAT() != null) {
            appendKeyword("float");
        }
        return builder;
    }

    @Override
    public StringBuilder visitMethodBody(MethodBodyContext ctx) {
        if (ctx.SEMI() != null) {
            builder.append(';');
        } else if (ctx.block() != null) {
            visit(ctx.block());
        }
        return builder;
    }

    @Override
    public StringBuilder visitBlock(BlockContext ctx) {
        startBlock();
        // if (ctx.blockStatements() != null) {
        // visit(ctx.blockStatements());
        // }
        endBlock();
        return builder;
    }

    @Override
    public StringBuilder visitNormalAnnotation(NormalAnnotationContext ctx) {
        builder.append("@");
        visit(ctx.typeName());
        builder.append("(");
        if (ctx.elementValuePairList() != null) {
            visit(ctx.elementValuePairList());
        }
        builder.append(")");
        return builder;
    }

    @Override
    public StringBuilder visitSingleElementAnnotation(SingleElementAnnotationContext ctx) {
        builder.append('@');
        visit(ctx.typeName());
        builder.append('(');
        visit(ctx.elementValue());
        builder.append(')');
        return builder;
    }

    @Override
    public StringBuilder visitMarkerAnnotation(MarkerAnnotationContext ctx) {
        builder.append("@");
        visit(ctx.typeName());
        return builder;
    }

    @Override
    public StringBuilder visitUnannClassType_lf_unannClassOrInterfaceType(
            UnannClassType_lf_unannClassOrInterfaceTypeContext ctx) {
        builder.append('.');
        return super.visitUnannClassType_lf_unannClassOrInterfaceType(ctx);
    }

    @Override
    public StringBuilder visitMethodModifier(MethodModifierContext ctx) {
        if (ctx.PUBLIC() != null) {
            appendKeyword("public");
        }
        if (ctx.PRIVATE() != null) {
            appendKeyword("private");
        }
        if (ctx.ABSTRACT() != null) {
            appendKeyword("abstract");
        }
        if (ctx.PROTECTED() != null) {
            appendKeyword("protected");
        }
        if (ctx.STATIC() != null) {
            appendKeyword("static");
        }
        if (ctx.FINAL() != null) {
            appendKeyword("final");
        }
        if (ctx.STRICTFP() != null) {
            appendKeyword("strictfp");
        }
        if (ctx.NATIVE() != null) {
            appendKeyword("native");
        }
        if (ctx.SYNCHRONIZED() != null) {
            appendKeyword("synchronized");
        }
        return builder;
    }

    @Override
    public StringBuilder visitConditionalExpression(ConditionalExpressionContext ctx) {
        if (ctx.expression() != null) {
            visit(ctx.conditionalOrExpression());
            builder.append('(');
            builder.append(" ? ");
            visit(ctx.expression());
            builder.append(" : ");
            if (ctx.conditionalExpression() != null) {
                visit(ctx.conditionalExpression());
            } else if (ctx.lambdaExpression() != null) {
                visit(ctx.lambdaExpression());
            }
            builder.append(')');
        } else {
            visit(ctx.conditionalOrExpression());
        }
        return builder;
    }

    @Override
    public StringBuilder visitConditionalOrExpression(ConditionalOrExpressionContext ctx) {
        if (ctx.conditionalOrExpression() != null) {
            builder.append('(');
            visit(ctx.conditionalOrExpression());
            builder.append(" || ");
            visit(ctx.conditionalAndExpression());
            builder.append(')');
        } else {
            visit(ctx.conditionalAndExpression());
        }
        return builder;
    }

    @Override
    public StringBuilder visitConditionalAndExpression(ConditionalAndExpressionContext ctx) {
        if (ctx.conditionalAndExpression() != null) {
            builder.append('(');
            visit(ctx.conditionalAndExpression());
            builder.append(" && ");
            visit(ctx.inclusiveOrExpression());
            builder.append(')');
        } else {
            visit(ctx.inclusiveOrExpression());
        }
        return builder;
    }

    @Override
    public StringBuilder visitInclusiveOrExpression(InclusiveOrExpressionContext ctx) {
        if (ctx.inclusiveOrExpression() != null) {
            builder.append('(');
            visit(ctx.inclusiveOrExpression());
            builder.append(" | ");
            visit(ctx.exclusiveOrExpression());
            builder.append(')');
        } else {
            visit(ctx.exclusiveOrExpression());
        }
        return builder;
    }

    @Override
    public StringBuilder visitExclusiveOrExpression(ExclusiveOrExpressionContext ctx) {
        if (ctx.exclusiveOrExpression() != null) {
            builder.append('(');
            visit(ctx.exclusiveOrExpression());
            builder.append(" ^ ");
            visit(ctx.andExpression());
            builder.append(')');
        } else {
            visit(ctx.andExpression());
        }
        return builder;
    }

    @Override
    public StringBuilder visitAndExpression(AndExpressionContext ctx) {
        if (ctx.andExpression() != null) {
            builder.append('(');
            visit(ctx.andExpression());
            builder.append(" & ");
            visit(ctx.equalityExpression());
            builder.append(')');
        } else {
            visit(ctx.equalityExpression());
        }
        return builder;
    }

    @Override
    public StringBuilder visitEqualityExpression(EqualityExpressionContext ctx) {
        if (ctx.equalityExpression() != null) {
            builder.append('(');
            visit(ctx.equalityExpression());
            if (ctx.EQUAL() != null) {
                builder.append(" == ");
            } else if (ctx.EQUAL() != null) {
                builder.append(" != ");
            }
            builder.append(')');
        } else {
            visit(ctx.relationalExpression());
        }
        return builder;
    }

    @Override
    public StringBuilder visitRelationalExpression(RelationalExpressionContext ctx) {
        if (ctx.relationalExpression() != null) {
            builder.append('(');
            visit(ctx.relationalExpression());
            if (ctx.GE() != null) {
                builder.append(" >= ");
            } else if (ctx.LE() != null) {
                builder.append(" <= ");
            } else if (ctx.GT() != null) {
                builder.append(" > ");
            } else if (ctx.LT() != null) {
                builder.append(" < ");
            } else if (ctx.INSTANCEOF() != null) {
                space();
                appendKeyword("instanceof");
                space();
            }
            if (ctx.shiftExpression() != null) {
                visit(ctx.shiftExpression());
            } else if (ctx.referenceType() != null) {
                visit(ctx.referenceType());
            }
            builder.append(')');
        } else if (ctx.shiftExpression() != null) {
            visit(ctx.shiftExpression());
        } else if (ctx.referenceType() != null) {
            visit(ctx.referenceType());
        }
        return builder;
    }

    @Override
    public StringBuilder visitShiftExpression(ShiftExpressionContext ctx) {
        if (ctx.shiftExpression() != null) {
            builder.append('(');
            visit(ctx.shiftExpression());
            if (ctx.GT() != null && ctx.GT().size() == 2) {
                builder.append(" >> ");
            } else if (ctx.GT() != null && ctx.GT().size() == 3) {
                builder.append(" >>> ");
            } else if (ctx.LT() != null && ctx.LT().size() == 2) {
                builder.append(" << ");
            }
            visit(ctx.additiveExpression());
            builder.append(')');
        } else {
            visit(ctx.additiveExpression());
        }
        return builder;
    }

    @Override
    public StringBuilder visitAdditiveExpression(AdditiveExpressionContext ctx) {
        if (ctx.additiveExpression() != null) {
            builder.append('(');
            visit(ctx.additiveExpression());
            if (ctx.ADD() != null) {
                builder.append(" + ");
            } else if (ctx.SUB() != null) {
                builder.append(" - ");
            }
            visit(ctx.multiplicativeExpression());
            builder.append(')');
        } else {
            visit(ctx.multiplicativeExpression());
        }
        return builder;
    }

    @Override
    public StringBuilder visitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
        if (ctx.multiplicativeExpression() != null) {
            builder.append('(');
            visit(ctx.multiplicativeExpression());
            if (ctx.MUL() != null) {
                builder.append(" * ");
            } else if (ctx.DIV() != null) {
                builder.append(" / ");
            } else if (ctx.MOD() != null) {
                builder.append(" % ");
            }
            visit(ctx.unaryExpression());
            builder.append(')');
        } else {
            visit(ctx.unaryExpression());
        }
        return builder;
    }

    @Override
    public StringBuilder visitPreIncrementExpression(PreIncrementExpressionContext ctx) {
        builder.append("++");
        visit(ctx.unaryExpression());
        return builder;
    }

    @Override
    public StringBuilder visitPreDecrementExpression(PreDecrementExpressionContext ctx) {
        builder.append("--");
        visit(ctx.unaryExpression());
        return builder;
    }

    @Override
    public StringBuilder visitUnaryExpression(UnaryExpressionContext ctx) {
        if (ctx.preDecrementExpression() != null) {
            visit(ctx.preDecrementExpression());
        } else if (ctx.preIncrementExpression() != null) {
            visit(ctx.preIncrementExpression());
        } else if (ctx.unaryExpression() != null) {
            if (ctx.ADD() != null) {
                builder.append('+');
            } else if (ctx.SUB() != null) {
                builder.append('-');
            }
            visit(ctx.unaryExpression());
        } else if (ctx.unaryExpressionNotPlusMinus() != null) {
            visit(ctx.unaryExpressionNotPlusMinus());
        }
        return builder;
    }

    @Override
    public StringBuilder visitUnaryExpressionNotPlusMinus(UnaryExpressionNotPlusMinusContext ctx) {
        if (ctx.postfixExpression() != null) {
            visit(ctx.postfixExpression());
        } else if (ctx.unaryExpression() != null) {
            if (ctx.TILDE() != null) {
                builder.append('~');
            } else if (ctx.BANG() != null) {
                builder.append('!');
            }
            visit(ctx.unaryExpression());
        } else if (ctx.castExpression() != null) {
            visit(ctx.castExpression());
        }
        return builder;
    }

    @Override
    public StringBuilder visitPostIncrementExpression(PostIncrementExpressionContext ctx) {
        visit(ctx.postfixExpression());
        builder.append("++");
        return builder;
    }

    @Override
    public StringBuilder visitPostIncrementExpression_lf_postfixExpression(
            PostIncrementExpression_lf_postfixExpressionContext ctx) {
        builder.append("++");
        return builder;
    }

    @Override
    public StringBuilder visitPostDecrementExpression(PostDecrementExpressionContext ctx) {
        visit(ctx.postfixExpression());
        builder.append("--");
        return builder;
    }

    @Override
    public StringBuilder visitPostDecrementExpression_lf_postfixExpression(
            PostDecrementExpression_lf_postfixExpressionContext ctx) {
        builder.append("--");
        return builder;
    }

    @Override
    public StringBuilder visitCastExpression(CastExpressionContext ctx) {
        if (ctx.unaryExpression() != null && ctx.primitiveType() != null) {
            builder.append("(");
            visit(ctx.primitiveType());
            builder.append(")");
            visit(ctx.unaryExpression());
        } else if (ctx.referenceType() != null) {
            builder.append("(");
            visit(ctx.referenceType());
            if (ctx.additionalBound() != null) {
                for (var child : ctx.additionalBound()) {
                    visit(child);
                }
            }
            builder.append(")");
            if (ctx.unaryExpressionNotPlusMinus() != null) {
                visit(ctx.unaryExpressionNotPlusMinus());
            } else if (ctx.lambdaExpression() != null) {
                visit(ctx.lambdaExpression());
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitExpressionName(ExpressionNameContext ctx) {
        if (ctx.ambiguousName() != null) {
            visit(ctx.ambiguousName());
            builder.append('.');
        }
        visit(ctx.identifier());
        return builder;
    }

    @Override
    public StringBuilder visitAmbiguousName(AmbiguousNameContext ctx) {
        if (ctx.ambiguousName() != null) {
            visit(ctx.ambiguousName());
            builder.append('.');
        }
        visit(ctx.identifier());
        return builder;
    }

    @Override
    public StringBuilder visitPackageOrTypeName(PackageOrTypeNameContext ctx) {
        if (ctx.packageOrTypeName() != null) {
            visit(ctx.packageOrTypeName());
            builder.append('.');
        }
        visit(ctx.identifier());
        return builder;
    }

    @Override
    public StringBuilder visitPackageName(PackageNameContext ctx) {
        if (ctx.packageName() != null) {
            visit(ctx.packageName());
            builder.append('.');
        }
        visit(ctx.identifier());
        return builder;
    }

    @Override
    public StringBuilder visitTypeName(TypeNameContext ctx) {
        if (ctx.packageOrTypeName() != null) {
            visit(ctx.packageOrTypeName());
            builder.append('.');
        }
        visit(ctx.identifier());
        return builder;
    }

    private void startBlock() {
        builder.append(" {");
        indentLevel++;
        newLine();
    }

    private void endBlock() {
        indentLevel--;
        newLine();
        builder.append("}");
        newLine();
    }

    private void newLine() {
        builder.append(LINE_SEPARATOR);
        for (int i = 0; i < indentLevel; i++) {
            builder.append(INDENT);
        }
    }

    private void space() {
        builder.append(' ');
    }

    private void appendKeyword(String keyword) {
        builder.append(KeywordTranslator.translateKeyword(keyword, "en", targetLanguage));
    }
}
