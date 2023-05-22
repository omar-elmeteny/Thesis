package eg.edu.guc.csen.localizedtranspiler;

import eg.edu.guc.csen.translator.Translations;

public class TranspilerOptions {

    private boolean writeIdentifiersDictionary;

    public boolean isWriteIdentifiersDictionary() {
        return writeIdentifiersDictionary;
    }

    public void setWriteIdentifiersDictionary(boolean writeIdentifiersDictionary) {
        this.writeIdentifiersDictionary = writeIdentifiersDictionary;
    }

    private String sourceLanguage;

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    private String targetLanguage;

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    private String outputEncoding;

    public String getOutputEncoding() {
        return outputEncoding;
    }

    public void setOutputEncoding(String outputEncoding) {
        this.outputEncoding = outputEncoding;
    }

    private String sourceEncoding;

    public String getSourceEncoding() {
        return sourceEncoding;
    }

    public void setSourceEncoding(String sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }

    private Translations translations;

    public Translations getTranslations() {
        return translations;
    }

    public void setTranslations(Translations translations) {
        this.translations = translations;
    }

    private boolean generateSourcemap;

    public boolean isGenerateSourcemap() {
        return generateSourcemap;
    }

    public void setGenerateSourcemap(boolean generateSourcemap) {
        this.generateSourcemap = generateSourcemap;
    }
    
}
