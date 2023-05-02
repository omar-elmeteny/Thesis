package eg.edu.guc.csen.languagelocalization.editors;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import eg.edu.guc.csen.keywordtranslator.ExceptionTranslations;
import eg.edu.guc.csen.keywordtranslator.KeyValuePair;
import eg.edu.guc.csen.keywordtranslator.KeyValueRegex;
import eg.edu.guc.csen.keywordtranslator.Language;

public class ExceptionsPage extends Composite {

    private final Combo languageCombo;
    private final KeyValuePair currentLanguage;
    private final TableViewer tableViewer;
    private final ExceptionTranslations exceptionTranslations;

    public ExceptionsPage(Composite parent, TranslationsEditor parentEditor,
            ExceptionTranslations exceptionsTranslations) {
        super(parent, SWT.NONE);
        this.exceptionTranslations = exceptionsTranslations;
        parentEditor.getTranslations();
        currentLanguage = new KeyValuePair(TranslationsPage.languages.get(0).getKey(),
                TranslationsPage.languages.get(0).getName());
        GridLayout layout = new GridLayout(1, false);
        this.setLayout(layout);

        Composite languageComposite = new Composite(this, SWT.NONE);
        languageComposite.setLayout(new GridLayout(3, false));
        Label languageLabel = new Label(languageComposite, SWT.NONE);
        languageLabel.setText("Language:");
        languageCombo = new Combo(languageComposite, SWT.READ_ONLY);
        languageCombo.setItems(TranslationsPage.languageNames);

        tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
        tableViewer.setContentProvider(ArrayContentProvider.getInstance());

        Table wordTable = tableViewer.getTable();
        GridData gridData = new GridData(SWT.NONE, SWT.FILL, true, true);
        gridData.heightHint = 20 * wordTable.getItemHeight();
        wordTable.setLayoutData(gridData);
        wordTable.setHeaderVisible(true);
        wordTable.setLinesVisible(true);

        TableLayout wordTableLayout = new TableLayout();
        wordTable.setLayout(wordTableLayout);

        TableViewerColumn wordColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        wordColumn.getColumn().setText("Exception Class Name");
        wordColumn.getColumn().setWidth(200);
        wordColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                KeyValueRegex p = (KeyValueRegex) element;
                return p.getKey();
            }
        });

        TableViewerColumn regexColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        regexColumn.getColumn().setText("Regular Expression");
        regexColumn.getColumn().setWidth(200);
        regexColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                KeyValueRegex p = (KeyValueRegex) element;
                return p.getRegex();
            }
        });

        regexColumn.setEditingSupport(new EditingSupport(wordColumn.getViewer()) {
            @Override
            protected void setValue(Object element, Object value) {
                KeyValueRegex p = (KeyValueRegex) element;
                String word = p.getKey();
                String translation = p.getValue();
                String regex = (String) value;
                if (p.getRegex().equals(value)) {
                    return;
                }
                if (translation == null) {
                    translation = getDefaultTranslation(regex);
                }
                exceptionsTranslations.addTranslation(word, regex, currentLanguage.getKey(), translation);
                p.setRegex(regex);
                updateExceptionsTable();
                parentEditor.updateEditor();
            }

            @Override
            protected Object getValue(Object element) {
                KeyValueRegex p = (KeyValueRegex) element;
                return p.getRegex();
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

        TableViewerColumn translationColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        translationColumn.getColumn().setText("Exception Message Translation");
        translationColumn.getColumn().setWidth(400);
        translationColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                KeyValueRegex p = (KeyValueRegex) element;
                return p.getValue();
            }
        });

        translationColumn.setEditingSupport(new EditingSupport(wordColumn.getViewer()) {
            @Override
            protected void setValue(Object element, Object value) {
                KeyValueRegex p = (KeyValueRegex) element;
                String word = p.getKey();
                String translation = (String) value;
                String regex = p.getRegex();
                if (p.getValue().equals(value)) {
                    return;
                }
                exceptionsTranslations.addTranslation(word, regex, currentLanguage.getKey(), translation);
                p.setValue(translation);
                tableViewer.update(element, null);
                parentEditor.updateEditor();
            }

            @Override
            protected Object getValue(Object element) {
                KeyValueRegex p = (KeyValueRegex) element;
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
                updateExceptionsTable();
            }
        };

        // Add a selection listener to the language dropdown
        languageCombo.addSelectionListener(adapter);

        languageCombo.select(0);
        adapter.widgetSelected(null);
    }

    protected void updateExceptionsTable() {
        int selectionIndex = languageCombo.getSelectionIndex();
        if (selectionIndex == -1) {
            return;
        }
        currentLanguage.setKey(TranslationsPage.languages.get(selectionIndex).getKey());
        currentLanguage.setValue(TranslationsPage.languages.get(selectionIndex).getName());
        Language lang = TranslationsPage.languages.get(languageCombo.getSelectionIndex());

        ArrayList<KeyValueRegex> tableData = new ArrayList<KeyValueRegex>();
        for (String exceptionClassName : exceptionClassNames) {
            tableData.addAll(exceptionTranslations.getTranslationEntries(exceptionClassName, lang.getKey()));
        }
        tableViewer.setInput(tableData.toArray());
    }

    private static final ArrayList<String> exceptionClassNames = getExceptionClassNames();

    public static ArrayList<String> getExceptionClassNames() {
        ArrayList<String> result = new ArrayList<String>();
        ArrayList<String> zipEntries = IdentifiersTranslationPage.srcZipEntries;
        for (String entry : zipEntries) {
            if (!entry.endsWith(".java")) {
                continue;
            }
            if (entry.endsWith("/package-info.java") || entry.endsWith("/module-info.java")) {
                continue;
            }
            try {
                String packageName = entry.substring(entry.indexOf("/") + 1, entry.lastIndexOf("/")).replace('/', '.');
                String className = entry.substring(entry.lastIndexOf("/") + 1, entry.length() - 5);

                Class<?> clazz = Class.forName(packageName + "." + className);
                if (Throwable.class.isAssignableFrom(clazz)) {
                    result.add(packageName + "." + className);
                }
            } catch (ClassNotFoundException|NoClassDefFoundError|ExceptionInInitializerError|UnsatisfiedLinkError e) {
            }
        }
        result.sort(null);
        return result;
    }

    private static final Pattern regexPattern = Pattern.compile("\\([^)]+\\)>");
    private static String getDefaultTranslation(String regex) {
        StringBuilder sb = new StringBuilder();
        int lastIndex = 0;
        int groups = 0;
        Matcher matcher = regexPattern.matcher(regex);
        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                sb.append(regex.substring(lastIndex, matcher.start()));
            }
            sb.append("$");
            sb.append(++groups);
            lastIndex = matcher.end();
        }
        if (groups == 0) {
            return regex;
        }
        if (lastIndex < regex.length()) {
            sb.append(regex.substring(lastIndex));
        }
        return sb.toString();
    }

}
