package eg.edu.guc.csen.keywordtranslator;

public class Language {
	private String key;
	private String name;
	private String nativeName;

	public Language(String key, String name, String nativeName) {
		this.key = key;
		this.name = name;
		this.nativeName = nativeName;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNativeName() {
		return nativeName;
	}

	public void setNativeName(String nativeName) {
		this.nativeName = nativeName;
	}
}
