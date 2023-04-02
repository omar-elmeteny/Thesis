package eg.edu.guc.csen.languagelocalization.translations;

import java.nio.file.Paths;

public class Helper {
	// TODO: this is hardcoded for now.
	private static final String dataPath = "D:\\Projects\\Thesis\\work\\eclipseplugin\\data";
	
	private static final KeywordTranslations keywordTranslations = initializeKeywordTranslations();
	private static final Languages languages = initializeLanguages();
	
	private static KeywordTranslations initializeKeywordTranslations() {
		return new KeywordTranslations(Paths.get(dataPath, "keywords.json").toAbsolutePath().toString());
	}
	
	private static Languages initializeLanguages() {
		return new Languages(Paths.get(dataPath, "languages.json").toAbsolutePath().toString());
	}
	
	public static Languages getLanguages() {
		return languages;
	}
	
	public static KeywordTranslations getKeywordTranslations() {
		return keywordTranslations;
	}
}
