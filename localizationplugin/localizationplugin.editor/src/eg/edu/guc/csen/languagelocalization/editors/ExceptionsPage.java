package eg.edu.guc.csen.languagelocalization.editors;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

import eg.edu.guc.csen.keywordtranslator.ExceptionTranslations;
import eg.edu.guc.csen.keywordtranslator.KeyValuePair;
import eg.edu.guc.csen.keywordtranslator.KeyValueRegex;

public class ExceptionsPage extends Composite {

    private final Combo languageCombo;
    private final KeyValuePair currentLanguage;
    private final TreeViewer treeViewer;
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
        wordColumn.getColumn().setWidth(200);
        wordColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                var p = (IdentifiersTranslationPage.BaseTreeObject) element;
                return p.getName();
            }
        });

        TreeViewerColumn regexColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        regexColumn.getColumn().setText("Regular Expression");
        regexColumn.getColumn().setWidth(200);
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
                exceptionsTranslations.addTranslation(word, regex, currentLanguage.getKey(), translation);
                p.setRegex(regex);
                p.setTranslation(translation);
                updateExceptionsTree();
                parentEditor.updateEditor();
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
                return element instanceof ExeptionTranslationTreeObject;
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
                p.setTranslation(translation);
                treeViewer.update(element, null);
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

    protected void updateExceptionsTree() {
        int selectionIndex = languageCombo.getSelectionIndex();
        if (selectionIndex == -1) {
            return;
        }
        currentLanguage.setKey(TranslationsPage.languages.get(selectionIndex).getKey());
        currentLanguage.setValue(TranslationsPage.languages.get(selectionIndex).getName());
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
                for (String entry : IdentifiersTranslationPage.srcZipEntries) {
                    if (entry.endsWith(".java")) {
                        String[] parts = entry.split("/");
                        String moduleName = parts[0];
                        ModuleTreeObject module = null;
                        for (ModuleTreeObject m : result) {
                            if (m.getName().equals(moduleName)) {
                                module = m;
                                break;
                            }
                        }

                        if (module == null) {
                            module = new ModuleTreeObject(this, moduleName);
                            result.add(module);
                        }

                    }
                }
                children = result.toArray(new ModuleTreeObject[result.size()]);
            }
            return children;
        }
    }

    private class ModuleTreeObject extends IdentifiersTranslationPage.BaseTreeObject {
        private ExeptionTranslationTreeObject[] children = null;

        public ModuleTreeObject(RootTreeObject parent, String name) {
            super(parent, name);
        }

        public synchronized IdentifiersTranslationPage.BaseTreeObject[] getChildren() {
            if (children == null) {
                ArrayList<ExeptionTranslationTreeObject> result = new ArrayList<>();
                ArrayList<PackageTreeObject> packages = new ArrayList<>();
                String fullName = this.getFullName() + "/";
                for (String entry : IdentifiersTranslationPage.srcZipEntries) {
                    if (!entry.startsWith(fullName) || !entry.endsWith(".java")) {
                        continue;
                    }
                    if (entry.endsWith("/package-info.java") || entry.endsWith("/module-info.java")) {
                        continue;
                    }
                    String strippedName = entry.substring(fullName.length());
                    String[] parts = strippedName.split("/");
                    if (parts.length <= 1) {
                        continue;
                    }
                    String packageName = parts[0];
                    PackageTreeObject packageObject = null;
                    for (PackageTreeObject p : packages) {
                        if (p.getName().equals(packageName)) {
                            packageObject = p;
                            break;
                        }
                    }

                    if (packageObject == null) {
                        packageObject = new PackageTreeObject(this, packageName);
                        packages.add(packageObject);
                        traversePackageRecursively(packageObject, result);
                    }
                }
                children = result.toArray(new ExeptionTranslationTreeObject[result.size()]);
                Arrays.sort(children);
            }
            return children;
        }

        private void traversePackageRecursively(PackageTreeObject packageObject,
                ArrayList<ExeptionTranslationTreeObject> result) {
            for (IdentifiersTranslationPage.BaseTreeObject child : packageObject.getChildren()) {
                if (child instanceof PackageTreeObject) {
                    traversePackageRecursively((PackageTreeObject) child, result);
                } else if (child instanceof ExeptionTranslationTreeObject) {
                    result.add((ExeptionTranslationTreeObject) child);
                }
            }
        }
    }

    private class PackageTreeObject extends IdentifiersTranslationPage.BaseTreeObject {

        private IdentifiersTranslationPage.BaseTreeObject[] children = null;

        public PackageTreeObject(IdentifiersTranslationPage.BaseTreeObject parent, String name) {
            super(parent, name);
        }

        public synchronized IdentifiersTranslationPage.BaseTreeObject[] getChildren() {
            if (children == null) {
                ArrayList<PackageTreeObject> childPackages = new ArrayList<>();
                ArrayList<ExeptionTranslationTreeObject> childTypes = new ArrayList<>();
                String fullName = this.getFullName() + "/";
                for (String entry : IdentifiersTranslationPage.srcZipEntries) {
                    if (!entry.startsWith(fullName) || !entry.endsWith(".java")) {
                        continue;
                    }
                    if (entry.endsWith("/package-info.java") || entry.endsWith("/module-info.java")) {
                        continue;
                    }
                    String strippedName = entry.substring(fullName.length());
                    String[] parts = strippedName.split("/");
                    if (parts.length < 1) {
                        continue;
                    }
                    if (parts.length == 1) {
                        String typeName = parts[0].substring(0, parts[0].length() - 5);
                        String typeFullName = fullName.substring(fullName.indexOf('/') + 1).replace("/", ".")
                                + typeName;
                        try {
                            Class<?> type = Class.forName(typeFullName);
                            if (!Throwable.class.isAssignableFrom(type)) {
                                continue;
                            }
                        } catch (ClassNotFoundException | NoClassDefFoundError | ExceptionInInitializerError
                                | UnsatisfiedLinkError e) {
                            continue;
                        }
                        ArrayList<KeyValueRegex> entries = exceptionTranslations.getTranslationEntries(typeFullName,
                                fullName);
                        if (entries == null) {
                            continue;
                        }
                        for (KeyValueRegex e : entries) {
                            childTypes.add(new ExeptionTranslationTreeObject(this, typeName, e.getRegex(), typeFullName,
                                    e.getValue()));
                        }

                    } else {
                        String packageName = parts[0];
                        PackageTreeObject packageObject = null;
                        for (PackageTreeObject p : childPackages) {
                            if (p.getName().equals(packageName)) {
                                packageObject = p;
                                break;
                            }
                        }

                        if (packageObject == null) {
                            packageObject = new PackageTreeObject(this, packageName);
                            childPackages.add(packageObject);
                        }
                    }

                }
                children = new IdentifiersTranslationPage.BaseTreeObject[childTypes.size() + childPackages.size()];
                int i = 0;
                for (PackageTreeObject p : childPackages) {
                    children[i++] = p;
                }
                for (ExeptionTranslationTreeObject t : childTypes) {
                    children[i++] = t;
                }
            }
            return children;
        }

    }

    private static class ExeptionTranslationTreeObject 
            extends IdentifiersTranslationPage.BaseTreeObject
            implements
            Comparable<ExeptionTranslationTreeObject> {

        private String regex;
        private final String typeFullname;
        private String translation;

        public String getRegex() {
            return regex == null ? "" : regex;
        }

        public void setRegex(String regex) {
            this.regex = regex;
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

        public ExeptionTranslationTreeObject(PackageTreeObject parent, String name, String regex, String typeFullname,
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
    }

}
