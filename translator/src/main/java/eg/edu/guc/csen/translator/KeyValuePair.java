package eg.edu.guc.csen.translator;

public class KeyValuePair implements Comparable<KeyValuePair> {
    private String key;
    
    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    private String value;

    public void setValue(String value) {
        this.value = value;
    }

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
