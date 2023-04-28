package eg.edu.guc.csen.keywordtranslator;

import java.util.HashMap;

public class ExceptionTranslationEntry {

    private HashMap<String, String> messages = new HashMap<String, String>();

    public ExceptionTranslationEntry() {
        super();
    }
    

    private String regex;

    public String getRegex() {
        return regex;
    }


    public void setRegex(String regex) {
        this.regex = regex;
    }

    public HashMap<String, String> getMessages() {
        return messages;
    }
}
