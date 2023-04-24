package eg.edu.guc.csen.keywordtranslator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.input.BOMInputStream;
import org.json.JSONObject;

import com.ibm.icu.text.Transliterator;

public class Translations {

    private final KeywordTranslations keywordTranslations = new KeywordTranslations();
    private final IdentifierTranslations identifierTranslations = new IdentifierTranslations();
    private String defaultLanguage = "ar";

    public Translations() {
        
    }

    public Translations(File jsonFile) throws IOException {
        super();
        if (jsonFile == null || !jsonFile.exists()) {
            return;
        }
        try (FileInputStream fileInputStream = new FileInputStream(jsonFile)) {
            try (BOMInputStream bomInputStream = new BOMInputStream(fileInputStream)) {
                String jsonString = new String(bomInputStream.readAllBytes(), StandardCharsets.UTF_8);
                updateFromJSONString(jsonString);
            }
        }
    }

    public void updateFromJSONString(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        updateFromJSONObject(jsonObject);
    }

    private void updateFromJSONObject(JSONObject jsonObject) {
        if (jsonObject.has("keywords")) {
            keywordTranslations.updateFromJSONObject(jsonObject.getJSONObject("keywords"));
        }
        if (jsonObject.has("identifiers")) {
            identifierTranslations.updateFromJSONObject(jsonObject.getJSONObject("identifiers"));
        } 
        if (jsonObject.has("defaultLanguage")) {
            defaultLanguage = jsonObject.getString("defaultLanguage");
        }
    }

    public boolean canAddTranslation(String word, String language, String translation) {
        if (word == null || language == null || translation == null) {
            return false;
        }
        String english = this.keywordTranslations.translateToEnglish(translation, language);
        if (word.equals(english)) {
            return true;
        }
        if (!translation.equals(english)) {
            return false;
        }
        if (Keywords.keywords.contains(translation) && !this.keywordTranslations.hasTranslationFromEnglish(translation, language)) {
            return false;
        }
        
        english = this.identifierTranslations.translateToEnglish(translation, language);
        if (word.equals(english)) {
            return true;
        }
        if (!translation.equals(english)) {
            return false;
        }
        if (Identifiers.commonIdentifiers.contains(translation) && !this.identifierTranslations.hasTranslationFromEnglish(translation, language)) {
            return false;
        }
        return true;
    }

    public void save(File jsonFile) throws IOException {
        JSONObject jsonObject = toJSON();
        Files.write(jsonFile.toPath(), jsonObject.toString(4).getBytes(StandardCharsets.UTF_8));
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("keywords", keywordTranslations.toJSON());
        jsonObject.put("identifiers", identifierTranslations.toJSON());
        jsonObject.put("defaultLanguage", defaultLanguage);
        return jsonObject;
    }

    public KeywordTranslations getKeywordTranslations() {
        return keywordTranslations;
    }

    public IdentifierTranslations getIdentifierTranslations() {
        return identifierTranslations;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public String translateKeyword(String keyword, String sourceLanguage, String targetLanguage) {
        if (sourceLanguage.equals(targetLanguage)) {
            return keyword;
        }
        if (sourceLanguage.equals("en")) {
            return keywordTranslations.translateFromEnglish(keyword, targetLanguage);
        }
        if (targetLanguage.equals("en")) {
            return keywordTranslations.translateToEnglish(keyword, sourceLanguage);
        }
        String english = keywordTranslations.translateToEnglish(keyword, sourceLanguage);
        return keywordTranslations.translateFromEnglish(english, targetLanguage);
    }

    public String translateIdentifier(String identifier, String sourceLanguage, String targetLanguage) {
        if (sourceLanguage.equals(targetLanguage)) {
            return identifier;
        }
        if (sourceLanguage.equals("en")) {
            return identifierTranslations.translateFromEnglish(identifier, targetLanguage);
        }
        if(identifierTranslations.hasTranslationToEnglish(identifier, sourceLanguage)) {
            String englishTranslation = identifierTranslations.translateToEnglish(identifier, sourceLanguage);
            if(targetLanguage.equals("en")) {
                return englishTranslation;
            }
            if(identifierTranslations.hasTranslationFromEnglish(englishTranslation, targetLanguage)) {
                return identifierTranslations.translateFromEnglish(englishTranslation, targetLanguage);
            }
        }
        if(sourceLanguage.equals("ar")) {
            Transliterator transliterator = Transliterator.getInstance("Arabic-Latin");
            identifier = transliterator.transliterate(identifier);
        }
        return replaceNonAlphaNumeric(identifier);
    }

    public static boolean isValidIdentifier(String identifier) {
        return new IdentifierValidator(identifier).isValid();
    }

    public static boolean isKeyword(String word) {
        return Keywords.keywords.contains(word);
    }

    public static boolean isCommonIdentifier(String word) {
        return Identifiers.commonIdentifiers.contains(word);
    }
    private static class IdentifierValidator {
        private int index;
        private String identifier;

        public IdentifierValidator(String identifier) {
            this.identifier = identifier;
        }

        public boolean isValid() {
            if (identifier == null || identifier.length() == 0) {
                return false;
            }
            if (!isJavaLetter()) {
                return false;
            }
            while (index < identifier.length()) {
                if (!isJavaLetterOrDigit()) {
                    return false;
                }
            }
            return true;
        }

        private boolean isJavaLetter() {
            char c = identifier.charAt(index);
            if(c >= 'a' && c <= 'z') {
                index++;
                return true;
            }
            if(c >= 'A' && c <= 'Z') {
                index++;
                return true;
            }
            if(c == '_' || c == '$') {
                index++;
                return true;
            }
    
            if (!(c >= 0 && c < 0x7f) && !(c >= 0xD800 && c <= 0xDBFF) && Character.isJavaIdentifierStart(c)) {
                index++;
                return true;
            }
    
            if (index < identifier.length() - 1 && Character.isHighSurrogate(c) && Character.isLowSurrogate(identifier.charAt(index + 1))
                && Character.isJavaIdentifierStart(Character.toCodePoint(c, identifier.charAt(index + 1)))
            ) {
                index += 2;
                return true;
            }
    
            return false;
        }

        private boolean isJavaLetterOrDigit() {
            char c = identifier.charAt(index);
            if (c >= '0' && c <= '9') {
                index++;
                return true;
            }
            if(c >= 'a' && c <= 'z') {
                index++;
                return true;
            }
            if(c >= 'A' && c <= 'Z') {
                index++;
                return true;
            }
            if(c == '_' || c == '$') {
                index++;
                return true;
            }
    
            if (!(c >= 0 && c < 0x7f) && !(c >= 0xD800 && c <= 0xDBFF) && Character.isJavaIdentifierPart(c)) {
                index++;
                return true;
            }
    
            if (index < identifier.length() - 1 && Character.isHighSurrogate(c) && Character.isLowSurrogate(identifier.charAt(index + 1))
                && Character.isJavaIdentifierPart(Character.toCodePoint(c, identifier.charAt(index + 1)))
            ) {
                index += 2;
                return true;
            }
    
            return false;
        }
    }

    private static String replaceNonAlphaNumeric(String input) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = (char)input.codePointAt(i);
            if (!shouldReplaceCharacter(c)) {
                output.append(c);
            } else {
                output.append(String.format("_x%04X_", (int)c));
            }
        }

        return output.toString();
    }

    private static boolean shouldReplaceCharacter(char c) {
        if (c == '$' || c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9'))
            return false;
        return true;
    }
}
