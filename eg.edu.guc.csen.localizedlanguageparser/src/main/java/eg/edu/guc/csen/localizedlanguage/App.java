package eg.edu.guc.csen.localizedlanguage;

import java.io.IOException;
import java.lang.annotation.Repeatable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import eg.edu.guc.csen.antlr4.*;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

@Repeatable(HelloValues.class)
@interface Hello{
}

@interface HelloValues {
    Hello[] value();
}
/**
 * Hello world!
 *
 */
public class App 
{
    public static void test(int @Hello() [] @Hello @Hello[] numbers) {

    }
    public static void main( String [] args ) throws IOException
    {
        String content = new String(Files.readAllBytes(Paths.get("test-files/test.guc")), StandardCharsets.UTF_8);
        Java9Lexer lexer = new Java9Lexer(CharStreams.fromString(content));

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java9Parser parser = new Java9Parser(tokens);
        ParseTree tree = parser.compilationUnit();

        String code = tree.toStringTree(parser);
        JavaGenerator generator = new JavaGenerator();
        var res = generator.visit(tree);

        System.out.println(code);
        System.out.println(res);
        
    }
}
