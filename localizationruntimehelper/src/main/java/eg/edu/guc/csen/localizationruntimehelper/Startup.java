package eg.edu.guc.csen.localizationruntimehelper;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Startup {

    public static void initializeApplication() {
        PrintStream ps = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        System.setOut(ps);
        ps = new PrintStream(System.err, true, StandardCharsets.UTF_8);
        System.setErr(ps);
    }
}
