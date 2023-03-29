package eg.edu.guc.csen.keywordtranslator;

import java.io.IOException;
import java.io.InputStream;

class ResourceHelper {
    public static String readResourceFile(String resourceFileName) throws IOException {
        // open a resource file and return its contents as a string

        // open a resource file
        ClassLoader  classLoader = ResourceHelper.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourceFileName);

        // read the contents of the resource file
        String fileContents = new String(inputStream.readAllBytes());
        return fileContents;
    }
}
