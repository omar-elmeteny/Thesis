package eg.edu.guc.csen.keywordtranslator;

public class KeyValuePair implements Comparable<KeyValuePair> {
    private final String key;
    public String getKey() {
        return key;
    }

    private final String value;

    public String getValue() {
        return value;
    }

    public KeyValuePair(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }

    @Override
    public int compareTo(KeyValuePair o) {
        if (o == null) {
            return 1;
        }
        return key.compareTo(o.key);
    }
}
