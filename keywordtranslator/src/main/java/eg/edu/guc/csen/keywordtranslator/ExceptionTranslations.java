package eg.edu.guc.csen.keywordtranslator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.apache.commons.io.input.BOMInputStream;
import org.json.JSONArray;
import org.json.JSONObject;

public class ExceptionTranslations {

    private HashMap<String, ArrayList<ExceptionTranslationEntry>> exceptionTranslations = new HashMap<String, ArrayList<ExceptionTranslationEntry>>();

    public ExceptionTranslations() {
        super();
    }

    public ExceptionTranslations(File jsonFile) throws IOException {
        super();
        if (!jsonFile.exists()) {
            return;
        }
        try (FileInputStream fileInputStream = new FileInputStream(jsonFile)) {
            try (BOMInputStream bomInputStream = new BOMInputStream(fileInputStream)) {
                String jsonString = new String(bomInputStream.readAllBytes(), StandardCharsets.UTF_8);
                loadFromJSONString(jsonString);
            }
        }
    }

    public ExceptionTranslations(JSONObject jsonObject) {
        super();
    }

    void updateFromJSONObject(JSONObject jsonObject) {
        exceptionTranslations.clear();
        loadFromJSONObject(jsonObject);
    }

    private void loadFromJSONString(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        loadFromJSONObject(jsonObject);
    }

    private void loadFromJSONObject(JSONObject jsonObject) {
        for (String exceptionClassName : jsonObject.keySet()) {
            JSONArray translationEntryArray = jsonObject.getJSONArray(exceptionClassName);

            ArrayList<ExceptionTranslationEntry> translationEntries = new ArrayList<ExceptionTranslationEntry>();
            for (int i = 0; i < translationEntryArray.length(); i++) {
                JSONObject translationEntryObject = translationEntryArray.getJSONObject(i);
                ExceptionTranslationEntry translationEntry = new ExceptionTranslationEntry();
                if (translationEntryObject.has("regex")) {
                    translationEntry.setRegex(translationEntryObject.getString("regex"));
                }
                if (!translationEntryObject.has("messages")) {
                    continue;
                }
                JSONObject messagesObject = translationEntryObject.getJSONObject("messages");
                for (String language : messagesObject.keySet()) {
                    translationEntry.getMessages().put(language, messagesObject.getString(language));
                }
                translationEntries.add(translationEntry);
            }
            this.exceptionTranslations.put(exceptionClassName, translationEntries);
        }
    }

    JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        for (String exceptionClassName : this.exceptionTranslations.keySet()) {
            JSONArray translationEntryArray = new JSONArray();
            for (ExceptionTranslationEntry translationEntry : this.exceptionTranslations.get(exceptionClassName)) {
                JSONObject translationEntryObject = new JSONObject();
                if (translationEntry.getRegex() != null) {
                    translationEntryObject.put("regex", translationEntry.getRegex());
                }
                JSONObject messagesObject = new JSONObject();
                for (String language : translationEntry.getMessages().keySet()) {
                    messagesObject.put(language, translationEntry.getMessages().get(language));
                }
                translationEntryObject.put("messages", messagesObject);
                translationEntryArray.put(translationEntryObject);
            }
            jsonObject.put(exceptionClassName, translationEntryArray);
        }
        return jsonObject;
    }

    public Exception translateException(Exception e, String language) {
        if (language == null || language.length() == 0 || language.equals("en")) {
            return e;
        }
        String className = e.getClass().getName();
        if (!this.exceptionTranslations.containsKey(className)) {
            return e;
        }
        ArrayList<ExceptionTranslationEntry> translationEntries = this.exceptionTranslations.get(className);
        String message = e.getMessage();
        String translatedMessage = message;
        for (ExceptionTranslationEntry translationEntry : translationEntries) {
            if (!translationEntry.getMessages().containsKey(language)) {
                continue;
            }
            String translation = translationEntry.getMessages().get(language);
            String regex = translationEntry.getRegex();
            if (regex == null || regex.length() == 0) {
                translatedMessage = translation;
                break;
            }
            if (Pattern.matches(regex, message)) {
                translatedMessage = message.replaceAll(regex, translation);
                break;
            }
        }
        if (translatedMessage.equals(message)) {
            return e;
        }
        // if exception has cause, create a new exception with translatedMessage and
        // same cause and type
        if (e.getCause() != null) {
            try {
                Exception translatedException = (Exception) e.getClass().getConstructor(String.class, Throwable.class)
                        .newInstance(translatedMessage, e.getCause());
                translatedException.setStackTrace(e.getStackTrace());
                return translatedException;
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
                return e;
            }
        }
        try {
            Exception translatedException = (Exception) e.getClass().getConstructor(String.class)
                    .newInstance(translatedMessage);
            translatedException.setStackTrace(e.getStackTrace());
            return translatedException;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
            return e;
            
        }
    }

}
