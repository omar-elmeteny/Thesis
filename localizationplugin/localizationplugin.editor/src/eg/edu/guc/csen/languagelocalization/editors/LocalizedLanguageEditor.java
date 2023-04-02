package eg.edu.guc.csen.languagelocalization.editors;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.MultiPageEditorPart;

import eg.edu.guc.csen.keywordtranslator.KeywordTranslations;
import eg.edu.guc.csen.keywordtranslator.Language;
import eg.edu.guc.csen.keywordtranslator.Languages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

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

	private void createLanguagePage() {
		// Create a new Composite for the page
		Composite composite = new Composite(getContainer(), SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		// Create the language dropdown
		Label languageLabel = new Label(composite, SWT.NONE);
		languageLabel.setText("Language:");

		final Combo languageCombo = new Combo(composite, SWT.READ_ONLY);
		final ArrayList<Language> languages = Languages.getLanguages();

		String[] languageNames = new String[languages.size()];
		for (int i = 0; i < languageNames.length; i++) {
			Language lang = languages.get(i);

			languageNames[i] = lang.getName().equals(lang.getNativeName()) ? lang.getName()
					: lang.getName() + " (" + lang.getNativeName() + ")";
		}
		languageCombo.setItems(languageNames);

		TableViewer tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		//final KeywordsTableContentProvider contentProvider = new KeywordsTableContentProvider();
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());

		Table keywordTable = tableViewer.getTable();
		GridData gridData = new GridData( SWT.NONE, SWT.FILL, true, true );
		gridData.heightHint = 20 * keywordTable.getItemHeight();
		keywordTable.setLayoutData(gridData);
		keywordTable.setHeaderVisible(true);
		keywordTable.setLinesVisible(true);
		
		// Define the table columns
		TableLayout keywordTableLayout = new TableLayout();
		keywordTable.setLayout(keywordTableLayout);

		TableViewerColumn keywordColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		keywordColumn.getColumn().setText("Keyword");
		keywordColumn.getColumn().setWidth(200);
		keywordColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
		    public String getText(Object element) {
		        String[] p = (String[]) element;
		        return p[0];
		    }
		});
		
		TableViewerColumn translationColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		translationColumn.getColumn().setText("Translation");
		translationColumn.getColumn().setWidth(200);
		translationColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
		    public String getText(Object element) {
		        String[] p = (String[]) element;
		        return p[1];
		    }
		});
		
		SelectionAdapter adapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Get the selected language
				int selectionIndex = languageCombo.getSelectionIndex();
				if (selectionIndex == -1) {
					return;
				}
				Language lang = languages.get(languageCombo.getSelectionIndex());
				HashMap<String, String> translations = KeywordTranslations.getLanguageTranslations(lang.getKey());
				String[][] tableData = new String[translations.size()][];
				int i = 0;
				for (Map.Entry<String, String> entry : translations.entrySet()) {
					tableData[i] = new String[] { entry.getKey(), entry.getValue() };
					i++;
				}
				tableViewer.setInput(tableData);
			}
		};
		
		// Add a selection listener to the language dropdown
		languageCombo.addSelectionListener(adapter);

		languageCombo.select(0);
		adapter.widgetSelected(null);
		// Add the composite to the editor page
		int index = addPage(composite);
		setPageText(index, editor.getTitle()  + " - Keywords");
	}

	private void createTextEditorPage() {
		try {
			editor = new TextEditor();
			int index = addPage(editor, getEditorInput());
			setPageText(index, editor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
		}
	}

	@Override
	protected void createPages() {
		createTextEditorPage();
		createLanguagePage();
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
