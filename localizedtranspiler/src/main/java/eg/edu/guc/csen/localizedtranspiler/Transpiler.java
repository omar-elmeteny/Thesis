package eg.edu.guc.csen.localizedtranspiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.io.input.BOMInputStream;
import org.json.JSONObject;

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
        var res = transpileInternal(options, content);

        if (options.isGenerateSourcemap()) {
            generateSourcemap(sourceFile, targetFile, options, content);
        }

        if(options.isWriteIdentifiersDictionary()) {
            File identifiersDictionaryFile = new File(targetFile.getParentFile(), targetFile.getName() + ".identifiers");
            JSONObject identifiersDictionary = new JSONObject();
            for (HashMap.Entry<String, String> entry : res.getIdentifiersDictionary().entrySet()) {
                identifiersDictionary.put(entry.getKey(), entry.getValue());
            }
            String identifiersDictionaryString = identifiersDictionary.toString(4);
            try {
                Files.write(identifiersDictionaryFile.toPath(), identifiersDictionaryString.getBytes(Charset.forName(options.getOutputEncoding())));
            } catch (IOException e) {
                throw new TranspilerException("Error writing identifiers dictionary file", e);
            }
        }

        try {
            Files.write(targetFile.toPath(), res.getCode().getBytes(Charset.forName(options.getOutputEncoding())));
        } catch (IOException e) {
            throw new TranspilerException("Error writing target file", e);
        }
    }

    private static void generateSourcemap(File sourceFile, File targetFile, TranspilerOptions options, String content) {
        StringBuilder sourceMap = new StringBuilder();
        sourceMap.append("SMAP");
        sourceMap.append(System.lineSeparator());
        sourceMap.append(targetFile.getName());
        sourceMap.append(System.lineSeparator());
        sourceMap.append("Java");
        sourceMap.append(System.lineSeparator());
        sourceMap.append("*S ");
        sourceMap.append(getFileNameWithoutExtension(targetFile));
        sourceMap.append(System.lineSeparator());
        sourceMap.append("*F");
        sourceMap.append(System.lineSeparator());
        sourceMap.append("+ 1 ");
        sourceMap.append(sourceFile.getName());
        sourceMap.append(System.lineSeparator());
        sourceMap.append(sourceFile.getAbsoluteFile().toString());
        sourceMap.append(System.lineSeparator());
        sourceMap.append("*L");
        sourceMap.append(System.lineSeparator());
        sourceMap.append("1#1,");
        sourceMap.append(countLines(content));
        sourceMap.append(":1");
        sourceMap.append(System.lineSeparator());
        sourceMap.append("*E");
        File sourcemapFile = new File(targetFile.getParentFile(), targetFile.getName() + ".smap");
        try {
            Files.write(sourcemapFile.toPath(), sourceMap.toString().getBytes(Charset.forName(options.getOutputEncoding())));
        } catch (IOException e) {
            // Ignore                
        }
    }

    private static String getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return fileName;
        }
        return fileName.substring(0, lastIndexOf);
    }

    private static int countLines(String s) {
        // count the number of lines in a string
        int lines = 1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                lines++;
            }
        }
        return lines;
    }

    public static String transpile(TranspilerOptions options, String content) {
        return transpileInternal(options, content).getCode();
    }

    private static TranspilerResult transpileInternal(TranspilerOptions options, String content) {
        Java9Lexer lexer = new Java9Lexer(CharStreams.fromString(content));
        lexer.setTranslations(options.getTranslations());
        lexer.setSourceLanguage(options.getSourceLanguage());

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        Java9Parser parser = new Java9Parser(tokens);
        ParseTree tree = parser.compilationUnit();

        // String code = tree.toStringTree(parser);
        JavaGenerator generator = new JavaGenerator(options.getSourceLanguage(), options.getTargetLanguage(),
                options.getTranslations());
        var res = tree.accept(generator);
        return new TranspilerResult(res.toString(), generator.getTranslatedIdentifiers());
    }

    private static class TranspilerResult {
        private final String code;
        private final HashMap<String,String> identifiersDictionary;

        public HashMap<String, String> getIdentifiersDictionary() {
            return identifiersDictionary;
        }

        public TranspilerResult(String code, HashMap<String, String> identifiersDictionary) {
            this.code = code;
            this.identifiersDictionary = identifiersDictionary;
        }
        
        public String getCode() {
            return code;
        }
    }
}
