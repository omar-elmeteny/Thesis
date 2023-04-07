package eg.edu.guc.csen.languagelocalization.editors;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import eg.edu.guc.csen.keywordtranslator.Translations;

public class TranslationsEditor extends MultiPageEditorPart{

    public TranslationsEditor() {
    }

    @Override
    protected void createPages() {
        FileEditorInput input = (FileEditorInput) this.getEditorInput();
        Translations translations;
        try {
            translations = new Translations(input.getFile().getFullPath().toFile());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        TranslationsPage keywordPage = new TranslationsPage(getContainer(), translations.getKeywordTranslations());
        TranslationsPage identifierPage = new TranslationsPage(getContainer(), translations.getIdentifierTranslations());
        int index = addPage(keywordPage);
		setPageText(index, "Keywords");
        index = addPage(identifierPage);
        setPageText(index, "Identifiers");
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        // throw new UnsupportedOperationException("Unimplemented method 'doSave'");
    }

    @Override
    public void doSaveAs() {
        // throw new UnsupportedOperationException("Unimplemented method 'doSaveAs'");
    }

    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }

    
    
}
