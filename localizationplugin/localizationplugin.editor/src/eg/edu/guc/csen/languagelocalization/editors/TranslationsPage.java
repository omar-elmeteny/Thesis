package eg.edu.guc.csen.languagelocalization.editors;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import eg.edu.guc.csen.keywordtranslator.KeyValuePair;
import eg.edu.guc.csen.keywordtranslator.Language;
import eg.edu.guc.csen.keywordtranslator.Languages;
import eg.edu.guc.csen.keywordtranslator.Translations;
import eg.edu.guc.csen.keywordtranslator.TranslationsBase;

class TranslationsPage extends Composite {
	private final static ArrayList<Language> languages = initializeLanguages();
	private final static String[] languageNames = initializeLanguageNames(languages);

	private final Combo languageCombo;
	private final KeyValuePair currentLanguage;
	private final TableViewer tableViewer;
	private final TranslationsBase translationsBase;
	public TranslationsPage(Composite parent, TranslationsBase translationsBase, TranslationsEditor parentEditor) {
		super(parent, SWT.NONE);
		this.translationsBase = translationsBase;
		GridLayout layout = new GridLayout(2, false);
		this.setLayout(layout);

		// Create the language dropdown
		Label languageLabel = new Label(this, SWT.NONE);
		languageLabel.setText("Language:");

		languageCombo = new Combo(this, SWT.READ_ONLY);
		
		languages.removeIf(l -> l.getKey().equals("en"));

		languageCombo.setItems(languageNames);
		currentLanguage = new KeyValuePair(languages.get(0).getKey(), languages.get(0).getName());

		tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());

		Table wordTable = tableViewer.getTable();
		GridData gridData = new GridData(SWT.NONE, SWT.FILL, true, true);
		gridData.heightHint = 20 * wordTable.getItemHeight();
		wordTable.setLayoutData(gridData);
		wordTable.setHeaderVisible(true);
		wordTable.setLinesVisible(true);

		// Define the table columns
		TableLayout wordTableLayout = new TableLayout();
		wordTable.setLayout(wordTableLayout);

		TableViewerColumn wordColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		wordColumn.getColumn().setText("Keyword");
		wordColumn.getColumn().setWidth(200);
		wordColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				KeyValuePair p = (KeyValuePair) element;
				return p.getKey();
			}
		});

		TableViewerColumn translationColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		translationColumn.getColumn().setText("Translation");
		translationColumn.getColumn().setWidth(200);
		translationColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				KeyValuePair p = (KeyValuePair) element;
				return p.getValue();
			}
		});

		translationColumn.setEditingSupport(new EditingSupport(wordColumn.getViewer()) {
			@Override
			protected void setValue(Object element, Object value) {
				KeyValuePair p = (KeyValuePair) element;
				String word = p.getKey();
				String translation = (String) value;
				if (p.getValue().equals(value)) {
					return;
				}
				if (!Translations.isValidIdentifier(translation)) {
					MessageDialog.openError(tableViewer.getTable().getShell(), "Error", "Invalid translation.");
					return;
				}
				if (!parentEditor.getTranslations().canAddTranslation(word, currentLanguage.getKey(), translation)) {
					MessageDialog.openError(tableViewer.getTable().getShell(), "Error",
							"This translation is already being used by another keyword or identifier.");
					return;
				}

				translationsBase.addTranslation(word, currentLanguage.getKey(), translation);
				p.setValue(translation);
				tableViewer.update(element, null);
				parentEditor.updateEditor();
			}

			@Override
			protected Object getValue(Object element) {
				KeyValuePair p = (KeyValuePair) element;
				return p.getValue();
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				TextCellEditor textCellEditor = new TextCellEditor(tableViewer.getTable());
				// textCellEditor.setValue(getValue(element));
				return textCellEditor;
			}

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}
		});

		SelectionAdapter adapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateTable();
			}
		};

		// Add a selection listener to the language dropdown
		languageCombo.addSelectionListener(adapter);

		languageCombo.select(0);
		adapter.widgetSelected(null);
	}

	private static ArrayList<Language> initializeLanguages() {
		ArrayList<Language> languages = Languages.getLanguages();
		languages.removeIf(l -> l.getKey().equals("en"));
		return languages;
	}

	private static String[] initializeLanguageNames(ArrayList<Language> languages) {
		String[] languageNames = new String[languages.size()];
		for (int i = 0; i < languageNames.length; i++) {
			Language lang = languages.get(i);

			languageNames[i] = lang.getName().equals(lang.getNativeName()) ? lang.getName()
					: lang.getName() + " (" + lang.getNativeName() + ")";
		}
		return languageNames;
	}

	void updateTable() {
		int selectionIndex = languageCombo.getSelectionIndex();
		if (selectionIndex == -1) {
			return;
		}
		currentLanguage.setKey(languages.get(selectionIndex).getKey());
		currentLanguage.setValue(languages.get(selectionIndex).getName());
		Language lang = languages.get(languageCombo.getSelectionIndex());
		ArrayList<KeyValuePair> translations = translationsBase.getLanguageTranslations(lang.getKey());
		KeyValuePair[] tableData = new KeyValuePair[translations.size()];
		int i = 0;
		for (KeyValuePair entry : translations) {
			tableData[i] = entry;
			i++;
		}
		tableViewer.setInput(tableData);
	}
}
