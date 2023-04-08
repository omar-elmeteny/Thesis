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

    private final KeywordTranslations keywordTranslations;
    private final IdentifierTranslations identifierTranslations;
    private String defaultLanguage = "ar";

    public Translations() {
        keywordTranslations = new KeywordTranslations();
        identifierTranslations = new IdentifierTranslations();
    }

    public Translations(File jsonFile) throws IOException {
        super();
        if (jsonFile == null || !jsonFile.exists()) {
            keywordTranslations = new KeywordTranslations();
            identifierTranslations = new IdentifierTranslations();
            return;
        }
        try (FileInputStream fileInputStream = new FileInputStream(jsonFile)) {
            try (BOMInputStream bomInputStream = new BOMInputStream(fileInputStream)) {
                String jsonString = new String(bomInputStream.readAllBytes(), StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(jsonString);
                if (jsonObject.has("keywords")) {
                    keywordTranslations = new KeywordTranslations(jsonObject.getJSONObject("keywords"));
                } else {
                    keywordTranslations = new KeywordTranslations();
                }
                if (jsonObject.has("identifiers")) {
                    identifierTranslations = new IdentifierTranslations(jsonObject.getJSONObject("identifiers"));
                } else {
                    identifierTranslations = new IdentifierTranslations();
                }
                if (jsonObject.has("defaultLanguage")) {
                    defaultLanguage = jsonObject.getString("defaultLanguage");
                }
            }
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

    public String translateKeyword(String keyword, String fromLanguage, String toLanguage) {
        if (fromLanguage.equals(toLanguage)) {
            return keyword;
        }
        if (fromLanguage.equals("en")) {
            return keywordTranslations.translateFromEnglish(keyword, toLanguage);
        }
        if (toLanguage.equals("en")) {
            return keywordTranslations.translateToEnglish(keyword, fromLanguage);
        }
        String english = keywordTranslations.translateToEnglish(keyword, fromLanguage);
        return keywordTranslations.translateFromEnglish(english, toLanguage);
    }

    public String translateIdentifier(String identifier, String sourceLanguage, String targetLanguage) {
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
