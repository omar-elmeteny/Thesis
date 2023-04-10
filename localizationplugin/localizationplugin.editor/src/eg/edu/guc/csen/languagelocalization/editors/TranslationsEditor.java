package eg.edu.guc.csen.languagelocalization.editors;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import eg.edu.guc.csen.keywordtranslator.Translations;

public class TranslationsEditor extends MultiPageEditorPart {

    private Translations translations;
    private boolean isDirty;
    /** The text editor used in page 2. */
    private IEditorPart editor;

    public TranslationsEditor() {
    }

    private File getJsonFile() {
        return ((FileEditorInput) this.getEditorInput()).getFile().getLocation().toFile();
    }

    public Translations getTranslations() {
        if (translations == null) {
            try {
                translations = new Translations(getJsonFile());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return translations;
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        setPartName(input.getToolTipText());
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
        firePropertyChange(PROP_DIRTY);
    }

    private void createTextEditorPage() {

        editor = new TextEditor();
        try {
            IEditorInput editorInput = getEditorInput();
            int index = addPage(editor, editorInput);
            setPageText(index, editorInput.getName());
        } catch (PartInitException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void createPages() {
        TranslationsPage keywordPage = new TranslationsPage(getContainer(), getTranslations().getKeywordTranslations(),
                this);
        TranslationsPage identifierPage = new TranslationsPage(getContainer(),
                getTranslations().getIdentifierTranslations(), this);
        int index = addPage(keywordPage);
        setPageText(index, "Keywords");
        index = addPage(identifierPage);
        setPageText(index, "Identifiers");

        createTextEditorPage();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {

        try {
            getTranslations().save(getJsonFile());
            setDirty(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
