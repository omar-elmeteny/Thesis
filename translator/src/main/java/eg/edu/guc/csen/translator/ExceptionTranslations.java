package eg.edu.guc.csen.translator;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExceptionTranslations {

    private HashMap<String, ArrayList<ExceptionTranslationEntry>> exceptionTranslations = new HashMap<String, ArrayList<ExceptionTranslationEntry>>();
    private Translations translations;
    private static ExceptionTranslations defaults;

    public ExceptionTranslations(Translations translations) {
        super();
        this.translations = translations;
    }

    public ExceptionTranslations(String resourcePath) {
        try {
            String jsonString = ResourceHelper.readResourceFile(resourcePath);
            loadFromJSONString(jsonString);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getExceptions(String language) {
        ArrayList<String> exceptions = new ArrayList<String>();
        for (String exception : exceptionTranslations.keySet()) {
            ArrayList<ExceptionTranslationEntry> translationEntries = exceptionTranslations.get(exception);
            for (ExceptionTranslationEntry translationEntry : translationEntries) {
                if (translationEntry.getMessages().containsKey(language)) {
                    exceptions.add(exception);
                    break;
                }
            }
        }
        return exceptions;
    }

    private void loadFromJSONString(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        loadFromJSONObject(jsonObject);
    }

    public synchronized static ExceptionTranslations getDefaults() {
        if (defaults == null) {
            defaults = new ExceptionTranslations("exceptions.json");
        }
        return defaults;
    }

    void updateFromJSONObject(JSONObject jsonObject) {
        exceptionTranslations.clear();
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

    public void addTranslation(String exception, String regex, String language, String translated) {
        if (!this.exceptionTranslations.containsKey(exception)) {
            this.exceptionTranslations.put(exception, new ArrayList<ExceptionTranslationEntry>());
        }
        ArrayList<ExceptionTranslationEntry> translationEntries = this.exceptionTranslations.get(exception);
        ExceptionTranslationEntry translationEntry = null;

        for (ExceptionTranslationEntry entry : translationEntries) {
            if (entry.getRegex() != null && entry.getRegex().equals(regex)
                    || entry.getRegex() == null && regex == null) {
                translationEntry = entry;
                break;
            }
        }
        if (translated == null || translated.isEmpty()) {
            if (translationEntry != null) {
                translationEntries.remove(translationEntry);
            }
            return;
        }
        if (translationEntry == null) {
            translationEntry = new ExceptionTranslationEntry();
            translationEntries.add(translationEntry);
        }
        translationEntry.setRegex(regex);
        translationEntry.getMessages().put(language, translated);

    }

    public ArrayList<KeyValueRegex> getTranslationEntries(String exceptionClassName, String language) {
        ArrayList<KeyValueRegex> translationEntries = new ArrayList<KeyValueRegex>();
        boolean foundEmptyRegex = false;
        if (this.exceptionTranslations.containsKey(exceptionClassName)) {

            for (ExceptionTranslationEntry translationEntry : this.exceptionTranslations.get(exceptionClassName)) {
                if (!translationEntry.getMessages().containsKey(language)) {
                    continue;
                }
                String regex = translationEntry.getRegex();
                if (regex == null || regex.length() == 0) {
                    foundEmptyRegex = true;
                }
                String translated = translationEntry.getMessages().get(language);
                translationEntries.add(new KeyValueRegex(exceptionClassName, translated, regex));
            }
        }
        if (!foundEmptyRegex) {
            translationEntries.add(new KeyValueRegex(exceptionClassName, "", ""));
        }
        return translationEntries;
    }

    public boolean hasAnyTranslationsForLanguage(String language) {
        for (String exceptionClassName : this.exceptionTranslations.keySet()) {
            ArrayList<ExceptionTranslationEntry> translationEntries = this.exceptionTranslations.get(exceptionClassName);
            for (ExceptionTranslationEntry translationEntry : translationEntries) {
                if (translationEntry.getMessages().containsKey(language)) {
                    return true;
                }
            }
        }
        return false;
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

    public Throwable translateThrowable(Throwable e, String language, HashMap<String, String> identifiersDictionary) {
        if (language == null || language.length() == 0 || language.equals("en")) {
            return e;
        }
        String className = e.getClass().getName();
        ArrayList<ExceptionTranslationEntry> translationEntries = this.exceptionTranslations.get(className);
        String message = e.getMessage();
        String translatedMessage = message;
        if (translationEntries != null) {
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
        }
        // if exception has cause, create a new exception with translatedMessage and
        // same cause and type
        if (e.getCause() != null) {
            try {
                Throwable translatedThrowable = (Throwable) e.getClass().getConstructor(String.class, Throwable.class)
                        .newInstance(translatedMessage, e.getCause());
                translatedThrowable
                        .setStackTrace(translateStackTraceElements(e.getStackTrace(), language, identifiersDictionary));
                return translatedThrowable;
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
                return e;
            }
        }
        try {
            Throwable translatedThrowable = (Throwable) e.getClass().getConstructor(String.class)
                    .newInstance(translatedMessage);
            translatedThrowable
                    .setStackTrace(translateStackTraceElements(e.getStackTrace(), language, identifiersDictionary));
            return translatedThrowable;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e1) {
            return e;
        }
    }

    private StackTraceElement[] translateStackTraceElements(StackTraceElement[] elements, String language,
            HashMap<String, String> identifiersDictionary) {
        StackTraceElement[] translatedElements = new StackTraceElement[elements.length];
        for (int i = 0; i < elements.length; i++) {
            translatedElements[i] = translateStackTraceElement(elements[i], language, identifiersDictionary);
        }
        return translatedElements;
    }

    private StackTraceElement translateStackTraceElement(StackTraceElement el, String language,
            HashMap<String, String> identifiersDictionary) {
        String className = el.getClassName();
        String methodName = el.getMethodName();

        String[] classNameSplit = className.split("\\.");
        for (int i = 0; i < classNameSplit.length; i++) {
            classNameSplit[i] = translateIdentifier(classNameSplit[i], language, identifiersDictionary);
        }
        methodName = translateIdentifier(methodName, language, identifiersDictionary);
        StackTraceElement translatedEl = new StackTraceElement(String.join(".", classNameSplit), methodName,
                el.getFileName(), el.getLineNumber());
        return translatedEl;
    }

    private String translateIdentifier(String identifier, String language,
            HashMap<String, String> identifiersDictionary) {
        if (identifiersDictionary.containsKey(identifier)) {
            return identifiersDictionary.get(identifier);
        }
        if (translations.getIdentifierTranslations().hasTranslationFromEnglish(identifier, language)) {
            return translations.getIdentifierTranslations().translateFromEnglish(identifier, language);
        }
        return identifier;
    }

}
