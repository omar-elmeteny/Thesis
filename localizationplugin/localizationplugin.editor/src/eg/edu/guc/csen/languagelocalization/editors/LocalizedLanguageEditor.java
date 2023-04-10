package eg.edu.guc.csen.languagelocalization.editors;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;

/**
 * 
 * @author omar-elmeteny This class creates a 2 page editor
 *         <ul>
 *         <li>The first page contains the source code localised in a language
 *         </li>
 *         <li>The second page contains a list of keywords and their translation
 *         in that language.</li>
 *         </ul>
 */
public class LocalizedLanguageEditor extends MultiPageEditorPart {

	/** The text editor used in page 0. */
	private TextEditor editor;

	private void createTextEditorPage() {
		try {
			editor = new BidiTextEditor();
			
			int index = addPage(editor, getEditorInput());
			setPageText(index, editor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		setPartName(input.getName());
	}

	@Override
	protected void createPages() {
		createTextEditorPage();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
		
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}
}
