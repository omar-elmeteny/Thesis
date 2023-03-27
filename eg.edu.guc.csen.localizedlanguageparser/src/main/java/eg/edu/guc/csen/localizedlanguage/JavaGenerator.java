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
        if (ctx.BooleanLiteral() != null) {
            String booleanLiteral = ctx.BooleanLiteral().getText();
            String keyword = KeywordTranslator.translateKeyword(booleanLiteral, sourceLanguage, "en");
            appendKeyword(keyword);
        } else if (ctx.NullLiteral() != null) {
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
        if(ctx.annotation() != null) {
            for(var child: ctx.annotation()) {
                visit(child);
                space();
            }
        }
        visit(ctx.identifier());
        if(ctx.typeArguments() != null) {
            visit(ctx.typeArguments());
        }
        return builder;
    }

    @Override
    public StringBuilder visitClassType_lfno_classOrInterfaceType(ClassType_lfno_classOrInterfaceTypeContext ctx) {
        if(ctx.annotation() != null) {
            for(var child: ctx.annotation()) {
                visit(child);
                space();
            }
        }
        visit(ctx.identifier());
        if(ctx.typeArguments() != null) {
            visit(ctx.typeArguments());
        }
        return builder;
    }

    @Override
    public StringBuilder visitPrimitiveType(PrimitiveTypeContext ctx) {
        if(ctx.annotation() != null) {
            for(var child: ctx.annotation()) {
                visit(child);
                space();
            }
        }
        if(ctx.numericType() != null) {
            visit(ctx.numericType());
        }
        if(ctx.BOOLEAN() != null) {
            appendKeyword("boolean");
        }
        return builder;
    }

    @Override
    public StringBuilder visitTypeParameter(TypeParameterContext ctx) {
        if(ctx.typeParameterModifier() != null) {
            for (var child : ctx.typeParameterModifier())
            {
                visit(child);
                space();
            }
        }
        visit(ctx.identifier());
        if(ctx.typeBound() != null) {
            visit(ctx.typeBound());
        }
        return builder;
    }

    @Override
    public StringBuilder visitTypeBound(TypeBoundContext ctx) {
        space();
        appendKeyword("extends");
        space();
        if(ctx.typeVariable() != null) {
            visit(ctx.typeVariable());
        }
        else if(ctx.classOrInterfaceType() != null) {
            visit(ctx.classOrInterfaceType());
            if(ctx.additionalBound() != null) {
                for(var child: ctx.additionalBound()) {
                    visit(child);
                    space();
                }
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitAdditionalBound(AdditionalBoundContext ctx) {
        for(var child: ctx.children) {
            if(child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                builder.append(tn.getSymbol().getText());
            }
            else if(child instanceof InterfaceTypeContext) {
                visit(child);
                space();
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitTypeArgument(TypeArgumentContext ctx) {
        for(var child: ctx.children) {
            if(child instanceof TerminalNode) {
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
        if(ctx.annotation() != null) {
            for(var child: ctx.annotation()) {
                visit(child);
                space();
            }
        }
        builder.append("?");
        if(ctx.wildcardBounds() != null) {
            visit(ctx.wildcardBounds());
        }
        return builder;
    }

    @Override
    public StringBuilder visitWildcardBounds(WildcardBoundsContext ctx) {
        for(var child: ctx.children) {
            if(child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                appendKeyword(tn.getSymbol().getText());
            } else if(child instanceof ReferenceTypeContext) {
                visit(child);
                space();
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitModuleName(ModuleNameContext ctx) {
        for(var child: ctx.children) {
            if(child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                builder.append(tn.getSymbol().getText());
            } else if(child instanceof IdentifierContext) {
                visit((IdentifierContext) child);
                space();
            } else if(child instanceof ModuleNameContext) {
                visit((ModuleNameContext) child);
                space();
            }
        }
        return builder;
    }
    
    @Override
    public StringBuilder visitModularCompilation(ModularCompilationContext ctx) {
        if(ctx.importDeclaration() != null) {
            for(var child: ctx.importDeclaration()) {
                visit(child);
                space();
            }
        }
        visit(ctx.moduleDeclaration());
        return builder;
    }

    @Override
    public StringBuilder visitTypeDeclaration(TypeDeclarationContext ctx) {
        for(var child: ctx.children) {
            if(child instanceof TerminalNode) {
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
        if(ctx.annotation() != null) {
            for(var child: ctx.annotation()) {
                visit(child);
                space();
            }
        }
        if(ctx.OPEN() != null) {
            appendKeyword("open");
            space();
        }
        appendKeyword("module");
        space();
        visit(ctx.moduleName());
        builder.append("{");
        newLine();
        if(ctx.moduleDirective() != null) {
            for(var child: ctx.moduleDirective()) {
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
        if(ctx.REQUIRES() != null) {
            space();
            appendKeyword("reguires");
            space();
            if(ctx.requiresModifier() != null) {
                for(var child: ctx.requiresModifier()) {
                    visit(child);
                    space();
                }
            }
            for(var child: ctx.moduleName()) {
                visit(child);
                space();
            }
        } else if (ctx.EXPORTS() != null || ctx.OPERNS() != null) {
            space();
            if(ctx.EXPORTS() != null) {
                appendKeyword("exports");
            } else if(ctx.OPERNS() != null) {
                appendKeyword("opens");
            }
            space();
            visit(ctx.packageName());
            if(ctx.moduleName() != null) {
                boolean addComma = false;
                for (var child : ctx.moduleName()) {
                    if (addComma) {
                        builder.append(", ");
                    }
                    visit(child);
                    addComma = true;
                }
            }
        } else if(ctx.USES() != null) {
            space();
            appendKeyword("uses");
            space();
            for(var child: ctx.typeName()) {
                visit(child);
                space();
            }
        } else if(ctx.PROVIDES() != null) {
            space();
            appendKeyword("provides");
            space();
            for(var child: ctx.typeName()) {
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
        for(var child: ctx.children) {
            if(child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                appendKeyword(tn.getSymbol().getText());
                space();
            }
        }
        return builder;
    }

    @Override
    public StringBuilder visitTypeParameters(TypeParametersContext ctx) {
        for(var child: ctx.children) {
            if(child instanceof TerminalNode) {
                TerminalNode tn = (TerminalNode) child;
                builder.append(tn.getSymbol().getText());
            } else if(child instanceof TypeParameterListContext) {
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
        for(var child: ctx.children) {
            if(child instanceof TerminalNode) {
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
        if(ctx.fieldModifier() != null) {
            for(var child: ctx.fieldModifier()) {
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
        if(ctx.annotation() != null) {
            visit(ctx.annotation());
        } else if(ctx.FINAL() != null) {
            appendKeyword("final");
            space();
        } else if(ctx.PRIVATE() != null) {
            appendKeyword("private");
            space();
        } else if(ctx.PROTECTED() != null) {
            appendKeyword("protected");
            space();
        } else if(ctx.STATIC() != null) {
            appendKeyword("static");
            space();
        } else if(ctx.TRANSIENT() != null) {
            appendKeyword("transient");
            space();
        } else if(ctx.VOLATILE() != null) {
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
        if(ctx.ASSIGN() != null) {
            visit(ctx.variableInitializer());
        }
        return builder;
    }

    @Override
    public StringBuilder visitVariableDeclaratorId(VariableDeclaratorIdContext ctx) {
        visit(ctx.identifier());
        if(ctx.dims() != null) {
            visit(ctx.dims());
        }
        return builder;
    }

    @Override
    public StringBuilder visitReceiverParameter(ReceiverParameterContext ctx) {
        if(ctx.annotation() != null) {
            for(var child: ctx.annotation()) {
                visit(child);
                space();
            }
        }
        visit(ctx.unannType());
        if(ctx.identifier() != null) {
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
        if(ctx.constructorModifier() != null) {
            for(var child: ctx.constructorModifier()) {
                visit(child);
                space();
            }
        }
        visit(ctx.constructorDeclarator());
        if(ctx.throws_() != null) {
            visit(ctx.throws_());
            space();
        }
        visit(ctx.constructorBody());
        return builder;
    }

    @Override
    public StringBuilder visitConstructorModifier(ConstructorModifierContext ctx) {
        if(ctx.annotation() != null) {
            visit(ctx.annotation());
        } else if(ctx.PRIVATE() != null) {
            appendKeyword("private");
        } else if(ctx.PROTECTED() != null) {
            appendKeyword("protected");
        } else if(ctx.PUBLIC() != null) {
            appendKeyword("public");
        }
        return builder;
    }

    @Override
    public StringBuilder visitConstructorDeclarator(ConstructorDeclaratorContext ctx) {
        if(ctx.typeParameters() != null) {
            visit(ctx.typeParameters());
        }
        visit(ctx.simpleTypeName());
        builder.append("(");
        if(ctx.formalParameterList() != null) {
            visit(ctx.formalParameterList());
        }
        builder.append(")");
        return builder;
    }

    @Override
    public StringBuilder visitConstructorBody(ConstructorBodyContext ctx) {
        builder.append("{");
        newLine();
        if(ctx.explicitConstructorInvocation() != null) {
            visit(ctx.explicitConstructorInvocation());
        }
        if(ctx.blockStatements() != null) {
            visit(ctx.blockStatements());
        }
        newLine();
        builder.append("}");
        return builder;
    }

    @Override
    public StringBuilder visitExplicitConstructorInvocation(ExplicitConstructorInvocationContext ctx) {
        if(ctx.typeArguments() != null) {
            visit(ctx.typeArguments());
            if(ctx.SUPER() != null) {
                appendKeyword("super");
            } else if(ctx.THIS() != null) {
                appendKeyword("this");
            }
            builder.append("(");
            if(ctx.argumentList() != null) {
                visit(ctx.argumentList());
            }
            builder.append(")");
            builder.append(";");
        } else if(ctx.expressionName() != null) {
            visit(ctx.expressionName());
            builder.append(".");
            appendKeyword("super");
            builder.append("(");
            if(ctx.argumentList() != null) {
                visit(ctx.argumentList());
            }
            builder.append(")");
            builder.append(";");
        } else if(ctx.primary() != null) {
            visit(ctx.primary());
            builder.append(".");
            appendKeyword("super");
            builder.append("(");
            if(ctx.argumentList() != null) {
                visit(ctx.argumentList());
            }
            builder.append(")");
            builder.append(";");
        }
        return super.visitExplicitConstructorInvocation(ctx);
    }

    @Override
    public StringBuilder visitEnumDeclaration(EnumDeclarationContext ctx) {
        if(ctx.classModifier() != null) {
            for(var child: ctx.children) {
                visit(child);
                space();
            }
        }
        appendKeyword("enum");
        visit(ctx.identifier());
        space();
        if(ctx.superinterfaces() != null) {
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
        if(ctx.enumConstantList() != null) {
            visit(ctx.enumConstantList());
            space();
        }
        if(ctx.COMMA() != null) {
            builder.append(",");
            space();
        }
        if(ctx.enumBodyDeclarations() != null) {
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
        if(ctx.enumConstantModifier() != null) {
            for(var child: ctx.children) {
                visit(child);
                space();
            }
        }
        visit(ctx.identifier());
        space();
        if(ctx.argumentList() != null) {
            builder.append("(");
            visit(ctx.argumentList());
            builder.append(")");
        }
        if(ctx.classBody() != null) {
            visit(ctx.classBody());
        }
        return super.visitEnumConstant(ctx);
    }

    @Override
    public StringBuilder visitEnumBodyDeclarations(EnumBodyDeclarationsContext ctx) {
        builder.append(";");
        if(ctx.classBodyDeclaration() != null) {
            for(var child: ctx.classBodyDeclaration()) {
                visit(child);
            }
            space();
        }
        return super.visitEnumBodyDeclarations(ctx);
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