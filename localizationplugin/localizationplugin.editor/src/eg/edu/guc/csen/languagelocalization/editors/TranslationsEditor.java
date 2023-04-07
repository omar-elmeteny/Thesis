package eg.edu.guc.csen.languagelocalization.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.MultiPageEditorPart;

public class TranslationsEditor extends MultiPageEditorPart{

    public TranslationsEditor() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void createPages() {
        Composite composite = new Composite(getContainer(), SWT.NONE);
        int index = addPage(composite);
		setPageText(index, "Keywords");
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
