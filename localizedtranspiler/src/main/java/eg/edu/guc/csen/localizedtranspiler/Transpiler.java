package eg.edu.guc.csen.localizedtranspiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.io.input.BOMInputStream;

public class Transpiler {

    public static void transpile(File sourceFile, File targetFile, TranspilerOptions options)
            throws TranspilerException {
        String content;
        try {
            try (FileInputStream fileInputStream = new FileInputStream(sourceFile.toString())) {
                try (BOMInputStream bomInputStream = new BOMInputStream(fileInputStream)) {
                    content = new String(bomInputStream.readAllBytes(), Charset.forName(options.getSourceEncoding()));
                }
            }
        } catch (IOException e) {
            throw new TranspilerException("Error reading source file", e);
        }
        var res = transpile(options, content);

        try {
            Files.write(targetFile.toPath(), res.getBytes(Charset.forName(options.getOutputEncoding())));
        } catch (IOException e) {
            throw new TranspilerException("Error writing target file", e);
        }
    }

    public static String transpile(TranspilerOptions options, String content) {
        Java9Lexer lexer = new Java9Lexer(CharStreams.fromString(content));
        lexer.setSourceLanguage(options.getSourceLanguage());

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java9Parser parser = new Java9Parser(tokens);
        ParseTree tree = parser.compilationUnit();

        // String code = tree.toStringTree(parser);
        JavaGenerator generator = new JavaGenerator(options.getSourceLanguage(), options.getTargetLanguage());
        var res = generator.visit(tree);
        return res.toString();
    }
}
