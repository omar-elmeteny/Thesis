package eg.edu.guc.csen.localizedtranspiler;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String [] args ) throws IOException
    {
        String content = new String(Files.readAllBytes(Paths.get("test-files/test-inputs/test.guc")), StandardCharsets.UTF_8);
        Java9Lexer lexer = new Java9Lexer(CharStreams.fromString(content));
        lexer.setSourceLanguage("de");

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java9Parser parser = new Java9Parser(tokens);
        ParseTree tree = parser.compilationUnit();

        // String code = tree.toStringTree(parser);
        JavaGenerator generator = new JavaGenerator();
        var res = generator.visit(tree);

        // println(code);
        println(res);

    }

    private static void println(Object msg) {
        try {
            PrintStream ps = new PrintStream(System.out, true, "UTF-8");
            ps.println(msg);
        } catch (UnsupportedEncodingException error) {
            System.out.println(msg);
        }
    }
}