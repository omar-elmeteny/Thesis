package eg.edu.guc.csen.languagelocalization.editors;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.json.JSONException;

import eg.edu.guc.csen.translator.ExceptionTranslations;
import eg.edu.guc.csen.translator.IdentifierTranslations;
import eg.edu.guc.csen.translator.KeywordTranslations;
import eg.edu.guc.csen.translator.Translations;

public class TranslationsEditor extends MultiPageEditorPart {

    private Translations translations;
    /** The text editor used in page 2. */
    private TextEditor editor;
    private IDocument document;
    private TranslationsPage keywordPage;
    private IdentifiersTranslationPage identifierPage;
    private DefaultLanguagePage defaultLanguagePage;
    private ExceptionsPage exceptionsPage;

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
        return editor.isDirty();
    }

    // public void setDirty(boolean isDirty) {
    //     this.isDirty = isDirty;
    //     firePropertyChange(PROP_DIRTY);
    // }

    void updateEditor() {
        // Update the content of the text editor
        IEditorPart activeEditor = getActiveEditor();
        if (activeEditor instanceof TextEditor) {
            TextEditor textEditor = (TextEditor) activeEditor;
            textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).set(
                getTranslations().toString());
        }

        
        document.set(getTranslations().toJSON().toString(4));
    }

    private void createTextEditorPage() {

        editor = new TextEditor();
        try {
            IEditorInput editorInput = getEditorInput();
            int index = addPage(editor, editorInput);
            setPageText(index, editorInput.getName());
            document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        
            document.addDocumentListener(new IDocumentListener() {

                @Override
                public void documentAboutToBeChanged(DocumentEvent event) {
                    
                }

                @Override
                public void documentChanged(DocumentEvent event) {
                    try {
                        getTranslations().updateFromJSONString(event.getDocument().get());
                        keywordPage.updateTable();
                        identifierPage.updateTree();
                        defaultLanguagePage.updateCombo();
                        exceptionsPage.updateExceptionsTree();
                    } catch (JSONException e) {
                    }
                }
                
            });
            
        } catch (PartInitException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void createPages() {
        defaultLanguagePage = new DefaultLanguagePage(getContainer(), this);
        int index = addPage(defaultLanguagePage);
        setPageText(index, "Language");

        keywordPage = new TranslationsPage(getContainer(), getTranslations().getKeywordTranslations(), KeywordTranslations.getDefaults(),
            this, false);
        index = addPage(keywordPage);
        setPageText(index, "Keywords");
        
        
        identifierPage = new IdentifiersTranslationPage(getContainer(),
                getTranslations().getIdentifierTranslations(), IdentifierTranslations.getDefaults(), this);
        index = addPage(identifierPage);
        setPageText(index, "Identifiers");

        
        exceptionsPage = new ExceptionsPage(getContainer(), this,  getTranslations().getExceptionTranslations(), ExceptionTranslations.getDefaults());
        index = addPage(exceptionsPage);
        setPageText(index, "Exceptions");

        createTextEditorPage();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {

        editor.doSave(monitor);
        
    }

    @Override
    public void doSaveAs() {
        editor.doSaveAs();
    }

    @Override
    public boolean isSaveAsAllowed() {
        return editor.isSaveAsAllowed();
    }
}
