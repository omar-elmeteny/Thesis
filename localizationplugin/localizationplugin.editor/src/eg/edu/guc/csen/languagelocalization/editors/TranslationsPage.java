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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.swt.widgets.Table;
import eg.edu.guc.csen.keywordtranslator.KeyValuePair;
import eg.edu.guc.csen.keywordtranslator.Language;
import eg.edu.guc.csen.keywordtranslator.Languages;
import eg.edu.guc.csen.keywordtranslator.Translations;
import eg.edu.guc.csen.keywordtranslator.TranslationsBase;

class TranslationsPage extends Composite {
	final static ArrayList<Language> languages = initializeLanguages();
	final static String[] languageNames = initializeLanguageNames(languages);

	private final Combo languageCombo;
	private final KeyValuePair currentLanguage;
	private final TableViewer tableViewer;
	private final TranslationsBase translationsBase;
	private final Button addDefaultsButton;
	private final TranslationsBase defaultTranslationsBase;

	public TranslationsPage(Composite parent, 
		TranslationsBase translationsBase, 
		TranslationsBase defaultTranslationsBase,
		TranslationsEditor parentEditor, boolean addIdentifier) {
		super(parent, SWT.NONE);
		this.translationsBase = translationsBase;
		this.defaultTranslationsBase = defaultTranslationsBase;
		currentLanguage = new KeyValuePair(languages.get(0).getKey(), languages.get(0).getName());

		GridLayout layout = new GridLayout(1, false);
		this.setLayout(layout);

		// Create the language dropdown
		Composite languageComposite = new Composite(this, SWT.NONE);
		languageComposite.setLayout(new GridLayout(3, false));
		Label languageLabel = new Label(languageComposite, SWT.NONE);
		languageLabel.setText("Language:");
		languageCombo = new Combo(languageComposite, SWT.READ_ONLY);
		languageCombo.setItems(languageNames);
		addDefaultsButton = new Button(languageComposite, SWT.PUSH);
		addDefaultsButton.setText("Add Defaults");
		addDefaultsButton.setEnabled(false);

		// Add click listener to the add defaults button
		addDefaultsButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
				String lang = currentLanguage.getKey();
				for (KeyValuePair pair : defaultTranslationsBase.getLanguageTranslations(lang)) {
					String word = pair.getKey();
					if (!translationsBase.hasTranslationFromEnglish(word, lang)
						&& defaultTranslationsBase.hasTranslationFromEnglish(word, lang)) {
						translationsBase.addTranslation(word, lang, defaultTranslationsBase.translateFromEnglish(word, lang));
					}
				}

				updateTable();
				parentEditor.updateEditor();
			};
		});

		if (addIdentifier) {
			Composite identifierComposite = new Composite(this, SWT.NONE);
			identifierComposite.setLayout(new GridLayout(3, false));

			Label identifierLabel = new Label(identifierComposite, SWT.NONE);
			identifierLabel.setText("New identifier:");

			Text identifierEditor = new Text(identifierComposite, SWT.BORDER);
			identifierEditor.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

			Button addButton = new Button(identifierComposite, SWT.PUSH);
			addButton.setText("Add Identifier");

			// Add click listener to the add button
			addButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
					String identifier = identifierEditor.getText();
					if (!Translations.isValidIdentifier(identifier)) {
						MessageDialog.openError(addButton.getShell(), "Error", "Invalid identifier.");
						return;
					}
					if (Translations.isCommonIdentifier(identifier) || 
							translationsBase.hasTranslationFromEnglish(identifier, currentLanguage.getKey())) {
							MessageDialog.openError(addButton.getShell(), "Error", "This identifier is already added.");
					}
					if (Translations.isKeyword(identifier)) {
						MessageDialog.openError(addButton.getShell(), "Error", "Cannot add a Java keyword.");
					}
					if (!parentEditor.getTranslations().canAddTranslation(identifier, currentLanguage.getKey(), identifier)) {
						MessageDialog.openError(addButton.getShell(), "Error", "This identifier is already being used.");
						return;
					}
					translationsBase.addTranslation(identifier, currentLanguage.getKey(), identifier);
					updateTable();
					identifierEditor.setText("");
					parentEditor.updateEditor();
				};
			});
		}



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

		addDefaultsButton.setEnabled(defaultTranslationsBase.hasAnyTranslationsForLanguage(lang.getKey()));
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
