package eg.edu.guc.csen.dictionarygenerator;

import java.util.ArrayList;
import java.util.List;

import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.Translate.TranslateOption;

public class GoogleTranslate {
    // translate a word from English to Arabic using Google Translate API
    public static List<String> translate(ArrayList<String> words, String targetLanguage) {
        // Read project Id from Env Variable
        String projectId = System.getenv("GOOGLE_API_PROJECT_ID");

        // Load the Google Translate API
        //Translate translate = TranslateOptions.getDefaultInstance().getService();
        
        // Translate the word
        TranslateOptions.Builder builder = TranslateOptions.newBuilder();
        builder.setProjectId(projectId)
        .setTargetLanguage("ar");

        var translate = builder.build().getService();
        var translations = translate.translate(words, TranslateOption.sourceLanguage("en"), TranslateOption.targetLanguage(targetLanguage));
        var result = new ArrayList<String>();
        for (int i = 0; i < words.size(); i++) {
            Translation translation = translations.get(i);
            String translatedText = translation.getTranslatedText();
            result.add(translatedText);
        }

        return result;
    }
}
