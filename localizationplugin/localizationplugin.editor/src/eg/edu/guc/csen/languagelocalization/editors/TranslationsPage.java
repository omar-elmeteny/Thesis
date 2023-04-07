package eg.edu.guc.csen.languagelocalization.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

import eg.edu.guc.csen.keywordtranslator.KeyValuePair;
import eg.edu.guc.csen.keywordtranslator.Language;
import eg.edu.guc.csen.keywordtranslator.Languages;
import eg.edu.guc.csen.keywordtranslator.TranslationsBase;

public class TranslationsPage extends Composite{
    
    private final TranslationsBase translationsBase;

    public TranslationsBase getTranslationsBase() {
        return translationsBase;
    }

    public TranslationsPage(Composite parent, TranslationsBase translationsBase) {
        super(parent, SWT.NONE);
        this.translationsBase = translationsBase;
        GridLayout layout = new GridLayout(2, false);
		this.setLayout(layout);

		// Create the language dropdown
		Label languageLabel = new Label(this, SWT.NONE);
		languageLabel.setText("Language:");

		final Combo languageCombo = new Combo(this, SWT.READ_ONLY);
		final ArrayList<Language> languages = Languages.getLanguages();

		String[] languageNames = new String[languages.size()];
		for (int i = 0; i < languageNames.length; i++) {
			Language lang = languages.get(i);

			languageNames[i] = lang.getName().equals(lang.getNativeName()) ? lang.getName()
					: lang.getName() + " (" + lang.getNativeName() + ")";
		}
		languageCombo.setItems(languageNames);

		TableViewer tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		//final KeywordsTableContentProvider contentProvider = new KeywordsTableContentProvider();
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());

		Table wordTable = tableViewer.getTable();
		GridData gridData = new GridData( SWT.NONE, SWT.FILL, true, true );
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
		TranslationsBase transl = translationsBase;
		SelectionAdapter adapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Get the selected language
				int selectionIndex = languageCombo.getSelectionIndex();
				if (selectionIndex == -1) {
					return;
				}
				Language lang = languages.get(languageCombo.getSelectionIndex());
				ArrayList<KeyValuePair> translations = transl.getLanguageTranslations(lang.getKey());
				String[][] tableData = new String[translations.size()][];
				int i = 0;
				for (KeyValuePair entry : translations) {
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
    }
}
