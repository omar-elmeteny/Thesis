package eg.edu.guc.csen.translator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.input.BOMInputStream;

class ResourceHelper {
    public static String readResourceFile(String resourceFileName) throws IOException {
        // open a resource file and return its contents as a string

        // open a resource file
        ClassLoader classLoader = ResourceHelper.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourceFileName)) {
            try (BOMInputStream bomInputStream = new BOMInputStream(inputStream)) {

                // read the contents of the resource file
                String fileContents = new String(bomInputStream.readAllBytes(), StandardCharsets.UTF_8);
                return fileContents;
            }
        }
    }
}
