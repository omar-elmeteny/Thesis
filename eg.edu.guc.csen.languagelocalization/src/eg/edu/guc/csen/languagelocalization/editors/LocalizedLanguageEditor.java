package eg.edu.guc.csen.languagelocalization.editors;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.MultiPageEditorPart;

import eg.edu.guc.csen.languagelocalization.translations.Helper;
import eg.edu.guc.csen.languagelocalization.translations.Language;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

//import eg.edu.guc.csen.languagelocalization.translations.Helper;
//import eg.edu.guc.csen.languagelocalization.translations.Language;
//
//import java.util.ArrayList;
//import java.util.Set;
//import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

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
		final ArrayList<Language> languages = Helper.getLanguages().getLanguages();

		String[] languageNames = new String[languages.size()];
		for (int i = 0; i < languageNames.length; i++) {
			Language lang = languages.get(i);

			languageNames[i] = lang.getName().equals(lang.getNativeName()) ? lang.getName()
					: lang.getName() + " (" + lang.getNativeName() + ")";
		}
		languageCombo.setItems(languageNames);

		// Create the keyword table
		Label keywordLabel = new Label(composite, SWT.NONE);
		keywordLabel.setText("Keyword");

		Label translationLabel = new Label(composite, SWT.NONE);
		translationLabel.setText("Translation");

		TableViewer tableViewer = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);

		Table keywordTable = tableViewer.getTable();
		keywordTable.setHeaderVisible(true);
		keywordTable.setLinesVisible(true);

		// Define the table columns
		TableLayout keywordTableLayout = new TableLayout();
		keywordTable.setLayout(keywordTableLayout);

		TableColumn keywordColumn = new TableColumn(keywordTable, SWT.NONE);
		keywordColumn.setText("Keyword");
		keywordColumn.setWidth(200);
		keywordTableLayout.addColumnData(new ColumnWeightData(40));

		TableColumn translationColumn = new TableColumn(keywordTable, SWT.NONE);
		translationColumn.setText("Translation");
		keywordTableLayout.addColumnData(new ColumnWeightData(60));

		// Add a selection listener to the language dropdown
		languageCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Get the selected language
				int languageIndex = languageCombo.getSelectionIndex();

				Language lang = languages.get(languageIndex);

				Set<Map.Entry<String, String>> translations = Helper.getKeywordTranslations()
						.getTranslationsByLanguage(lang.getKey());
				String[][] tableData = new String[translations.size()][];
				int i = 0;
				for (Map.Entry<String, String> entry : translations) {
					tableData[i] = new String[] { entry.getKey(), entry.getValue() };
					i++;
				}
			}
		});

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
