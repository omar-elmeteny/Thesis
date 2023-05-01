package eg.edu.guc.csen.keywordtranslator;

public class Language {
	private final String key;
	private final String name;
	private final String nativeName;
	private final String script;
	private final boolean rtl;
	private final String atTranslation;

	public String getAtTranslation() {
		return atTranslation;
	}

	public Language(String key, String name, String nativeName, String script, boolean rtl, String atTranslation) {
		this.key = key;
		this.name = name;
		this.nativeName = nativeName;
		this.script = script;
		this.rtl = rtl;
		this.atTranslation = atTranslation;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public String getNativeName() {
		return nativeName;
	}

	public String getScript() {
		return script;
	}

	public boolean isRtl() {
		return rtl;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Language) {
			Language other = (Language) obj;
			return key.equals(other.key);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.key.hashCode();
	}

	@Override
	public String toString() {
		return this.key;
	}
}
