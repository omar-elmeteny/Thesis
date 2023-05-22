package eg.edu.guc.csen.languagelocalization.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import eg.edu.guc.csen.translator.ExceptionTranslations;
import eg.edu.guc.csen.translator.KeyValuePair;
import eg.edu.guc.csen.translator.KeyValueRegex;
import eg.edu.guc.csen.translator.Language;
import eg.edu.guc.csen.languagelocalization.editors.IdentifiersTranslationPage.BaseTreeObject;

public class ExceptionsPage extends Composite {

    private final Combo languageCombo;
    private final KeyValuePair currentLanguage;
    private final TreeViewer treeViewer;
    private final ExceptionTranslations exceptionTranslations;
    private ExceptionTranslations defaultExceptionsTranslations;
	private final Button addDefaultsButton;

    public ExceptionsPage(Composite parent, TranslationsEditor parentEditor,
            ExceptionTranslations exceptionsTranslations, ExceptionTranslations defaultExceptionsTranslations) {
        super(parent, SWT.NONE);
        this.exceptionTranslations = exceptionsTranslations;
        this.defaultExceptionsTranslations = defaultExceptionsTranslations;
        parentEditor.getTranslations();
        currentLanguage = new KeyValuePair(TranslationsPage.languages.get(0).getKey(),
                TranslationsPage.languages.get(0).getName());
        GridLayout layout = new GridLayout(1, false);
        this.setLayout(layout);


         // Search composite
         Composite searchComposite = new Composite(this, SWT.NONE);
         searchComposite.setLayout(new GridLayout(4, false));
         Label searchLabel = new Label(searchComposite, SWT.NONE);
         searchLabel.setText("Search:");
         final Text searchText = new Text(searchComposite, SWT.SEARCH);
         searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
         searchComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
         Button exactSearch = new Button(searchComposite, SWT.CHECK);
         exactSearch.setText("Exact match");
         exactSearch.setSelection(true);
         Button caseSensitive = new Button(searchComposite, SWT.CHECK);
         caseSensitive.setText("Case sensitive");
         caseSensitive.setSelection(true);
         
         searchText.addModifyListener(new ModifyListener() {
             @Override
             public void modifyText(ModifyEvent e) {
                 updateSearch(searchText.getText().trim(), exactSearch.getSelection(), caseSensitive.getSelection());
             }
         });
 
         exactSearch.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 updateSearch(searchText.getText().trim(), exactSearch.getSelection(), caseSensitive.getSelection());
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 updateSearch(searchText.getText().trim(), exactSearch.getSelection(), caseSensitive.getSelection());
             }
         });
 
         caseSensitive.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 updateSearch(searchText.getText().trim(), exactSearch.getSelection(), caseSensitive.getSelection());
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 updateSearch(searchText.getText().trim(), exactSearch.getSelection(), caseSensitive.getSelection());
             }
         });
         

        Composite languageComposite = new Composite(this, SWT.NONE);
        languageComposite.setLayout(new GridLayout(3, false));
        Label languageLabel = new Label(languageComposite, SWT.NONE);
        languageLabel.setText("Language:");
        languageCombo = new Combo(languageComposite, SWT.READ_ONLY);
        languageCombo.setItems(TranslationsPage.languageNames);
        addDefaultsButton = new Button(languageComposite, SWT.PUSH);
		addDefaultsButton.setText("Add Defaults");
		addDefaultsButton.setEnabled(false);

		// Add click listener to the add defaults button
		addDefaultsButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(org.eclipse.swt.events.MouseEvent e) {
				String lang = currentLanguage.getKey();
				for(String exception : defaultExceptionsTranslations.getExceptions(lang)) {
                    for (KeyValueRegex tuple : defaultExceptionsTranslations.getTranslationEntries(exception, lang)) {
                        String regex = tuple.getRegex();
                        String translation = tuple.getValue();
                        exceptionTranslations.addTranslation(exception, regex, lang, translation);
                    }
                }

				updateExceptionsTree();
				parentEditor.updateEditor();
			};
		});

        treeViewer = new TreeViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
        treeViewer.setContentProvider(new ExceptionsTreeContentProvider());

        Tree wordTable = treeViewer.getTree();
        GridData gridData = new GridData(SWT.NONE, SWT.FILL, true, true);
        gridData.heightHint = 20 * wordTable.getItemHeight();
        wordTable.setLayoutData(gridData);
        wordTable.setHeaderVisible(true);
        wordTable.setLinesVisible(true);

        TableLayout wordTableLayout = new TableLayout();
        wordTable.setLayout(wordTableLayout);

        TreeViewerColumn wordColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        wordColumn.getColumn().setText("Exception Class Name");
        wordColumn.getColumn().setWidth(350);
        wordColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var p = (IdentifiersTranslationPage.BaseTreeObject) element;
                return p.getName();
            }
        });

        TreeViewerColumn regexColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        regexColumn.getColumn().setText("Regular Expression");
        regexColumn.getColumn().setWidth(400);
        regexColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (!(element instanceof ExeptionTranslationTreeObject)) {
                    return "";
                }
                var p = (ExeptionTranslationTreeObject) element;
                return p.getRegex();
            }
        });

        regexColumn.setEditingSupport(new EditingSupport(wordColumn.getViewer()) {
            @Override
            protected void setValue(Object element, Object value) {
                if (!(element instanceof ExeptionTranslationTreeObject)) {
                    return;
                }
                ExeptionTranslationTreeObject p = (ExeptionTranslationTreeObject) element;
                String word = p.getName();
                String translation = p.getTranslation();
                String regex = (String) value;
                if (regex == null) {
                    regex = "";
                }
                if (p.getRegex().equals(value)) {
                    return;
                }
                try {
                    Pattern.compile(regex, 0);
                } catch (PatternSyntaxException e) {
                    MessageDialog.openError(treeViewer.getTree().getShell(), "Error",
                            "Invalid regular expression: " + e.getMessage());
                    return;
                }
                if (translation == null || translation.isEmpty() && !regex.isEmpty()) {
                    translation = getDefaultTranslation(regex);
                }
                ModuleTreeObject parent = (ModuleTreeObject) p.getParent();
                ExeptionTranslationTreeObject newObject = new ExeptionTranslationTreeObject(parent, p.getName(), regex,
                        p.getTypeFullname(), translation);

                if (parent.insertAfter(newObject)) {
                    exceptionsTranslations.addTranslation(word, regex, currentLanguage.getKey(), translation);
                    treeViewer.refresh(parent, true);
                    parentEditor.updateEditor();
                } else {
                    MessageDialog.openError(treeViewer.getTree().getShell(), "Error",
                            "The regular expression already exists");
                }
            }

            @Override
            protected Object getValue(Object element) {
                if (!(element instanceof ExeptionTranslationTreeObject)) {
                    return "";
                }
                ExeptionTranslationTreeObject p = (ExeptionTranslationTreeObject) element;
                return p.getRegex();
            }

            @Override
            protected CellEditor getCellEditor(Object element) {
                TextCellEditor textCellEditor = new TextCellEditor(treeViewer.getTree());
                return textCellEditor;
            }

            @Override
            protected boolean canEdit(Object element) {
                if (!(element instanceof ExeptionTranslationTreeObject)) {
                    return false;
                }
                ExeptionTranslationTreeObject p = (ExeptionTranslationTreeObject) element;
                return p.getRegex().isEmpty();
            }
        });

        TreeViewerColumn translationColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        translationColumn.getColumn().setText("Exception Message Translation");
        translationColumn.getColumn().setWidth(400);
        translationColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (!(element instanceof ExeptionTranslationTreeObject)) {
                    return "";
                }
                ExeptionTranslationTreeObject p = (ExeptionTranslationTreeObject) element;
                return p.getTranslation();
            }
        });

        translationColumn.setEditingSupport(new EditingSupport(wordColumn.getViewer()) {
            @Override
            protected void setValue(Object element, Object value) {
                if (!(element instanceof ExeptionTranslationTreeObject)) {
                    return;
                }
                ExeptionTranslationTreeObject p = (ExeptionTranslationTreeObject) element;
                String word = p.getTypeFullname();
                String translation = (String) value;
                String regex = p.getRegex();
                if (p.getTranslation().equals(value)) {
                    return;
                }
                exceptionsTranslations.addTranslation(word, regex, currentLanguage.getKey(), translation);
                if (translation == null || translation.isEmpty() && !regex.isEmpty()) {
                    ModuleTreeObject parent = (ModuleTreeObject) p.getParent();
                    parent.removeChild(p);
                    treeViewer.refresh(parent, true);
                } else {
                    p.setTranslation(translation);
                    treeViewer.update(element, null);
                }
                parentEditor.updateEditor();
            }

            @Override
            protected Object getValue(Object element) {
                if (!(element instanceof ExeptionTranslationTreeObject)) {
                    return "";
                }
                ExeptionTranslationTreeObject p = (ExeptionTranslationTreeObject) element;
                return p.getTranslation();
            }

            @Override
            protected CellEditor getCellEditor(Object element) {
                TextCellEditor textCellEditor = new TextCellEditor(treeViewer.getTree());
                return textCellEditor;
            }

            @Override
            protected boolean canEdit(Object element) {
                return element instanceof ExeptionTranslationTreeObject;
            }
        });

        SelectionAdapter adapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updateExceptionsTree();
            }
        };

        // Add a selection listener to the language dropdown
        languageCombo.addSelectionListener(adapter);

        languageCombo.select(0);
        adapter.widgetSelected(null);
        treeViewer.setInput(new RootTreeObject());
    }

    private void updateSearch(String searchTerm, boolean exact, boolean caseSensitive) {
        if (searchTerm.length() == 0) {
            treeViewer.setFilters(new ViewerFilter[0]);
            return;
        }
        final String searchTermFinal = caseSensitive ? searchTerm : searchTerm.toLowerCase();
        treeViewer.setFilters(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                BaseTreeObject obj = (BaseTreeObject) element;
                return obj.hasTerm(searchTermFinal, exact, caseSensitive);
            }
        });
        treeViewer.expandAll();
    }

    protected void updateExceptionsTree() {
        int selectionIndex = languageCombo.getSelectionIndex();
        if (selectionIndex == -1) {
            return;
        }
        currentLanguage.setKey(TranslationsPage.languages.get(selectionIndex).getKey());
        currentLanguage.setValue(TranslationsPage.languages.get(selectionIndex).getName());
        Language lang = TranslationsPage.languages.get(languageCombo.getSelectionIndex());

		addDefaultsButton.setEnabled(defaultExceptionsTranslations.hasAnyTranslationsForLanguage(lang.getKey()));
        treeViewer.setInput(new RootTreeObject());
        treeViewer.refresh(true);
    }

    private static final Pattern regexPattern = Pattern.compile("\\([^)]+\\)");

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

    private class ExceptionsTreeContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            IdentifiersTranslationPage.BaseTreeObject obj = (IdentifiersTranslationPage.BaseTreeObject) inputElement;
            if (obj == null) {
                return new Object[0];
            }
            return obj.getChildren();
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            return getElements(parentElement);
        }

        @Override
        public Object getParent(Object element) {
            IdentifiersTranslationPage.BaseTreeObject obj = (IdentifiersTranslationPage.BaseTreeObject) element;
            if (obj == null) {
                return null;
            }
            return obj.getParent();
        }

        @Override
        public boolean hasChildren(Object element) {
            IdentifiersTranslationPage.BaseTreeObject obj = (IdentifiersTranslationPage.BaseTreeObject) element;
            if (obj == null) {
                return false;
            }
            return obj.getChildren() != null && obj.getChildren().length > 0;
        }

    }

    private class RootTreeObject extends IdentifiersTranslationPage.BaseTreeObject {

        private ModuleTreeObject[] children = null;

        public RootTreeObject() {
            super(null, "");

        }

        public synchronized IdentifiersTranslationPage.BaseTreeObject[] getChildren() {
            if (children == null) {
                ArrayList<ModuleTreeObject> result = new ArrayList<>();
                for (var entry : IdentifiersTranslationPage.srcZipEntries.entrySet()) {
                    String moduleName = entry.getKey();
                    ModuleTreeObject module = new ModuleTreeObject(this, moduleName, entry.getValue());
                    if (module.typeNames.isEmpty()) {
                        continue;
                    }
                    result.add(module);
                }
                children = result.toArray(new ModuleTreeObject[result.size()]);
                Arrays.sort(children);
            }
            return children;
        }

        @Override
        public boolean hasChildren() {
            return true;
        }
    }

    private class ModuleTreeObject
            extends IdentifiersTranslationPage.BaseTreeObject
            implements Comparable<ModuleTreeObject> {
        private ExeptionTranslationTreeObject[] children = null;
        private List<String> typeNames;

        public ModuleTreeObject(RootTreeObject parent, String name, ArrayList<String> entries) {
            super(parent, name);

            this.typeNames = entries.parallelStream()
                    .filter(entry -> entry.endsWith("Error") || entry.endsWith("Exception"))
                    .toList();
        }

        public void removeChild(ExeptionTranslationTreeObject p) {
            int index = Arrays.binarySearch(children, p);
            if (index < 0) {
                return;
            }
            ExeptionTranslationTreeObject[] newChildren = new ExeptionTranslationTreeObject[children.length - 1];
            if (index > 0) {
                System.arraycopy(children, 0, newChildren, 0, index);
            }
            if (index < children.length - 1) {
                System.arraycopy(children, index + 1, newChildren, index, children.length - index - 1);
            }
            children = newChildren;
        }

        public boolean insertAfter(ExeptionTranslationTreeObject newObject) {
            int index = Arrays.binarySearch(children, newObject);
            if (index < 0) {
                index = -index - 1;
            } else {
                return false;
            }
            ExeptionTranslationTreeObject[] newChildren = new ExeptionTranslationTreeObject[children.length + 1];
            if (index > 0) {
                System.arraycopy(children, 0, newChildren, 0, index);
            }
            newChildren[index] = newObject;
            if (index < children.length) {
                System.arraycopy(children, index, newChildren, index + 1, children.length - index);
            }
            children = newChildren;
            return true;
        }

        public synchronized IdentifiersTranslationPage.BaseTreeObject[] getChildren() {
            if (children == null) {
                ArrayList<ExeptionTranslationTreeObject> result = new ArrayList<>();
                for (String typeName : typeNames) {
                    try {
                        Class<?> type = Class.forName(typeName);
                        if (!Throwable.class.isAssignableFrom(type)) {
                            continue;
                        }
                    } catch (ClassNotFoundException | NoClassDefFoundError | ExceptionInInitializerError
                            | UnsatisfiedLinkError e) {
                        continue;
                    }
                    ArrayList<KeyValueRegex> entries = exceptionTranslations.getTranslationEntries(typeName,
                            currentLanguage.getKey());
                    if (entries == null) {
                        continue;
                    }
                    int index = typeName.lastIndexOf('.');
                    String name = typeName;
                    if (index > 0) {
                        name = typeName.substring(index + 1);
                    }
                    for (KeyValueRegex entry : entries) {
                        
                        result.add(new ExeptionTranslationTreeObject(this, name, entry.getRegex(), typeName,
                                entry.getValue()));
                    }
                }
                children = result.toArray(new ExeptionTranslationTreeObject[result.size()]);
                Arrays.sort(children);
            }
            return children;
        }

        @Override
        public int compareTo(ModuleTreeObject o) {
            return getName().compareTo(o.getName());
        }

        @Override
        public boolean hasChildren() {
            return this.typeNames.size() > 0;
        }
    }

    private static class ExeptionTranslationTreeObject
            extends IdentifiersTranslationPage.BaseTreeObject
            implements
            Comparable<ExeptionTranslationTreeObject> {

        private final String regex;
        private final String typeFullname;
        private String translation;

        public String getRegex() {
            return regex == null ? "" : regex;
        }

        public String getTypeFullname() {
            return typeFullname;
        }

        public String getTranslation() {
            return translation == null ? "" : translation;
        }

        public void setTranslation(String translation) {
            this.translation = translation;
        }

        public ExeptionTranslationTreeObject(ModuleTreeObject parent, String name, String regex, String typeFullname,
                String translation) {
            super(parent, name);
            this.regex = regex;
            this.typeFullname = typeFullname;
            this.translation = translation;
        }

        public synchronized IdentifiersTranslationPage.BaseTreeObject[] getChildren() {
            return null;
        }

        @Override
        public String getName() {
            return this.getTypeFullname();
        }

        @Override
        public int compareTo(ExeptionTranslationTreeObject o) {
            int result = this.getTypeFullname().compareTo(o.getTypeFullname());
            if (result == 0) {
                result = this.getRegex().compareTo(o.getRegex());
            }
            return result;
        }

        @Override
        public boolean hasChildren() {
            return false;
        }
    }

}
