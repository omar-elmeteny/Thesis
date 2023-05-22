package eg.edu.guc.csen.translator;

public class KeyValueRegex extends KeyValuePair{

    private String regex;

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex == null ? "" : regex;
    }

    public KeyValueRegex(String key, String value, String regex) {
        super(key, value);
        this.regex = regex == null ? "" : regex;
    }
    
}
