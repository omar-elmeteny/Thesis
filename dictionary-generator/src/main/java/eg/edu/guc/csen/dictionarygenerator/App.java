package eg.edu.guc.csen.dictionarygenerator;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.naming.ldap.LdapName;

import com.google.api.services.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translate.TranslateOption;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args )
    {

        String javaBaseLocation = System.getProperty("java.home");
        String srcLocation = Path.of(javaBaseLocation, "lib", "src.zip").toString();

        String[] packagePaths = new String[] { "java.base/java/lang", "java.base/java/util", "java.base/java/io", "java.base/java/math" };

        String regex = "(" + String.join("|", packagePaths)
                    .replaceAll("/", "\\\\/")
                    .replaceAll("\\.", "\\\\.") + ")" 
                    + "\\/[A-Z][A-Za-z0-9]*\\.java";
                

        try (ZipFile zipFile = new ZipFile(srcLocation)) {
            var entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                
                // Escape java regular expression special characters
                // Create a regular expression that matches the package path
                // Check if the entry is a class file and is in one of the package paths
                if (entry.getName().matches(regex)) {
                // Get the class name
                    String className = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);
                    className = className.substring(0, className.lastIndexOf('.'));
                    System.out.println(translate(className));
                    continue;
                    // String packageName = entry.getName().substring(10, entry.getName().lastIndexOf('/')).replace('/', '.');
                    // try {
                    //     Class<?> clazz = Class.forName(packageName + "." + className);
                    //     System.out.println(clazz.getName());

                    //     // Get the methods
                    //     Method[] methods = clazz.getDeclaredMethods();
                    //     // Print the methods
                    //     for (Method method : methods) {
                    //         if ((method.getModifiers() & Modifier.PUBLIC) != 0) {
                    //             System.out.println(method.getName());
                    //         }
                    //     }

                    // } catch (ClassNotFoundException e) {
                    //     continue;
                    // }
                    
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the ZIP file: " + e.getMessage());
        }
    }

    // translate a word from English to Arabic using Google Translate API
    public static String translate(String word) {
        // Read Key from Env Variable
        String key = System.getenv("GoogleTranslateAPIKey");

        // Read project Id from Env Variable
        String projectId = System.getenv("GoogleTranslateProjectId");

        // Load the Google Translate API
        //Translate translate = TranslateOptions.getDefaultInstance().getService();
        
        // Translate the word
        List<String> words = new ArrayList<String>();
        words.add(word);

        TranslateOptions.Builder builder = TranslateOptions.newBuilder();
        builder.setProjectId(projectId)
        .setTargetLanguage("ar")
        .setApiKey(key);

        var translate = builder.build().getService();
        var translation = translate.translate(words, TranslateOption.sourceLanguage("en"), TranslateOption.targetLanguage("ar"));
        return translation.get(0).getTranslatedText();
    }
        



    // private static Class<?> getClass(String className, String packageName) {
    //     try {
    //         return Class.forName(packageName + "."
    //           + className.substring(0, className.lastIndexOf('.')));
    //     } catch (ClassNotFoundException e) {
    //         // handle the exception
    //     }
    //     return null;
    // }
}
