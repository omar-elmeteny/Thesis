package eg.edu.guc.csen.languagelocalization.editors;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.ZipFile;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
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

import eg.edu.guc.csen.translator.KeyValuePair;
import eg.edu.guc.csen.translator.Language;
import eg.edu.guc.csen.translator.Translations;
import eg.edu.guc.csen.translator.TranslationsBase;

public class IdentifiersTranslationPage extends Composite {

    private final TranslationsBase defaultTranslationsBase;
    private KeyValuePair currentLanguage;
    private final Combo languageCombo;
    private final Button addDefaultsButton;
    private final TreeViewer treeViewer;

    public IdentifiersTranslationPage(Composite parent, TranslationsBase translationsBase,
            TranslationsBase defaultTranslationsBase, TranslationsEditor parentEditor) {
        super(parent, SWT.NONE);
        this.defaultTranslationsBase = defaultTranslationsBase;
        this.currentLanguage = new KeyValuePair(TranslationsPage.languages.get(0).getKey(),
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
        
        // Create the language dropdown
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
                for (KeyValuePair pair : defaultTranslationsBase.getLanguageTranslations(lang)) {
                    String word = pair.getKey();
                    if (!translationsBase.hasTranslationFromEnglish(word, lang)
                            && defaultTranslationsBase.hasTranslationFromEnglish(word, lang)) {
                        translationsBase.addTranslation(word, lang,
                                defaultTranslationsBase.translateFromEnglish(word, lang));
                    }
                }

                updateTree();
                parentEditor.updateEditor();
            };
        });

        treeViewer = new TreeViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
        GridData gridData = new GridData(SWT.NONE, SWT.FILL, true, true);
		gridData.heightHint = 20 * treeViewer.getTree().getItemHeight();
		treeViewer.getTree().setLayoutData(gridData);
        treeViewer.setContentProvider(new IdentifiersTreeContentProvider());
        treeViewer.getTree().setHeaderVisible(true);
        treeViewer.getTree().setLinesVisible(true);
        
        TreeViewerColumn identifierColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        identifierColumn.getColumn().setText("Identifier");
        identifierColumn.getColumn().setWidth(600);
        identifierColumn.setLabelProvider(new CellLabelProvider() {
            @Override
            public void update(org.eclipse.jface.viewers.ViewerCell cell) {
                BaseTreeObject obj = (BaseTreeObject) cell.getElement();
                cell.setText(obj.getName());
            }
        });
        
        TreeViewerColumn translationColumn = new TreeViewerColumn(treeViewer, SWT.NONE);
        translationColumn.getColumn().setText("Translation");
        translationColumn.getColumn().setWidth(200);
        translationColumn.setLabelProvider(new CellLabelProvider() {
            @Override
            public void update(org.eclipse.jface.viewers.ViewerCell cell) {
                BaseTreeObject obj = (BaseTreeObject) cell.getElement();
                if (obj instanceof ModuleTreeObject) {
                    cell.setText("");
                    return;
                }
                cell.setText(translationsBase.translateFromEnglish(obj.getName(),
                        currentLanguage.getKey()));
            }
        });
        
        translationColumn.setEditingSupport(new EditingSupport(translationColumn.getViewer()) {
            @Override
			protected void setValue(Object element, Object value) {
				BaseTreeObject treeObj = (BaseTreeObject) element;
                if (treeObj instanceof ModuleTreeObject) {
                    return;
                }
				String word = treeObj.getName();
				String translation = (String) value;
				if (translationsBase.translateFromEnglish(word, currentLanguage.getKey()).equals(value)) {
					return;
				}
				if (!Translations.isValidIdentifier(translation)) {
					MessageDialog.openError(treeViewer.getTree().getShell(), "Error", "Invalid translation.");
					return;
				}
				if (!parentEditor.getTranslations().canAddTranslation(word, currentLanguage.getKey(), translation)) {
					MessageDialog.openError(treeViewer.getTree().getShell(), "Error",
							"This translation is already being used by another keyword or identifier.");
					return;
				}

				translationsBase.addTranslation(word, currentLanguage.getKey(), translation);
				treeViewer.refresh(true);
				parentEditor.updateEditor();
			}

			@Override
			protected Object getValue(Object element) {
				BaseTreeObject treeObj = (BaseTreeObject) element;
                if (treeObj instanceof ModuleTreeObject) {
                    return "";
                }
                return translationsBase.translateFromEnglish(treeObj.getName(), currentLanguage.getKey());
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				TextCellEditor textCellEditor = new TextCellEditor(treeViewer.getTree());
				return textCellEditor;
			}

			@Override
			protected boolean canEdit(Object element) {
                BaseTreeObject treeObj = (BaseTreeObject) element;
                if (treeObj instanceof ModuleTreeObject) {
                    return false;
                }
				return true;
			}
        });
        
        SelectionAdapter adapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateTree();
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
    
    public void updateTree() {
        int selectionIndex = languageCombo.getSelectionIndex();
		if (selectionIndex == -1) {
			return;
		}
		currentLanguage.setKey(TranslationsPage.languages.get(selectionIndex).getKey());
		currentLanguage.setValue(TranslationsPage.languages.get(selectionIndex).getName());
		Language lang = TranslationsPage.languages.get(languageCombo.getSelectionIndex());

		addDefaultsButton.setEnabled(defaultTranslationsBase.hasAnyTranslationsForLanguage(lang.getKey()));
		treeViewer.refresh(true);;
    }

    private static class IdentifiersTreeContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            BaseTreeObject obj = (BaseTreeObject) inputElement;
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
            BaseTreeObject obj = (BaseTreeObject) element;
            if (obj == null) {
                return null;
            }
            return obj.getParent();
        }

        @Override
        public boolean hasChildren(Object element) {
            BaseTreeObject obj = (BaseTreeObject) element;
            if (obj == null) {
                return false;
            }
            return obj.getChildren().length > 0;
        }

    }

    static abstract class BaseTreeObject {

        private final BaseTreeObject parent;
        private final String name;
        private final String lowerCaseName;

        public String getName() {
            return name;
        }

        public BaseTreeObject getParent() {
            return parent;
        }

        public BaseTreeObject(BaseTreeObject parent, String name) {
            super();
            this.parent = parent;
            this.name = name;
            this.lowerCaseName = name.toLowerCase();
        }

        public String getFullName() {
            if (parent == null) {
                return name;
            } else {
                String parentName = parent.getFullName();
                if (parentName == null || parentName.isEmpty())
                    return name;
                else
                    return parentName + "." + name;
            }
        }

        public abstract BaseTreeObject[] getChildren();

        public boolean nameMatch(String term, boolean exact, boolean caseSensitive) {
            if (exact) {
                if (caseSensitive) {
                    return name.equals(term);
                } else {
                    return lowerCaseName.equals(term);
                }
            } else {
                if (caseSensitive) {
                    return name.contains(term);
                } else {
                    return lowerCaseName.contains(term);
                }
            }
        }

        public boolean hasTerm(String term, boolean exact, boolean caseSensitive) {
            if (nameMatch(term, exact, caseSensitive))
                return true;
            if (this.getChildren() == null) {
                return false;
            }
            for (BaseTreeObject child : getChildren()) {
                if (child.hasTerm(term, exact, caseSensitive))
                    return true;
            }
            return false;
        }

        public boolean hasChildren() {
            return getChildren() != null && getChildren().length > 0;
        }
    }


    static HashMap<String, ArrayList<String>> srcZipEntries = initializeSrcZipEntries();

    private static HashMap<String, ArrayList<String>> initializeSrcZipEntries() {
        String javaBaseLocation = System.getProperty("java.home");
        String srcLocation = Path.of(javaBaseLocation, "lib", "src.zip").toString();
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        try {
            try (ZipFile zipFile = new ZipFile(srcLocation)) {
                var entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    ArrayList<String> result;
                    String name = entry.getName();
                    if (!name.endsWith(".java") || name.endsWith("package-info.java") || name.endsWith("module-info.java") || name.endsWith("package.html") || name.endsWith("overview.html") || name.endsWith("module-info.java")) {
                        continue;
                    }
                    int index = name.indexOf('/');
                    if (index == -1) {
                        continue;
                    }

                    String moduleName = entry.getName().substring(0, entry.getName().indexOf('/'));
                    String typeFullName = name.substring(index + 1, name.length() - 5).replace('/', '.');
                    if (map.containsKey(moduleName)) {
                        result = map.get(moduleName);
                    } else {
                        result = new ArrayList<>();
                        map.put(moduleName, result);
                    }
                    result.add(typeFullName);
                }

            }
        } catch (IOException e) {
        }
        return map;
    }

    private static class RootTreeObject extends BaseTreeObject {

        private ModuleTreeObject[] children = null;

        public RootTreeObject() {
            super(null, "");

        }

        public synchronized BaseTreeObject[] getChildren() {
            if (children == null) {
                ArrayList<ModuleTreeObject> result = new ArrayList<>();
                for (var entry : srcZipEntries.entrySet()) {
                    String moduleName = entry.getKey();
                    ModuleTreeObject module = null;
                    module = new ModuleTreeObject(this, moduleName, entry.getValue());
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

    private static class ModuleTreeObject 
        extends BaseTreeObject
        implements Comparable<ModuleTreeObject>
        {
        private PackageTreeObject[] children = null;
        private ArrayList<String> types;

        public ModuleTreeObject(RootTreeObject parent, String name, ArrayList<String> types) {
            super(parent, name);
            this.types = types;
        }

        public synchronized BaseTreeObject[] getChildren() {
            if (children == null) {
                ArrayList<PackageTreeObject> result = new ArrayList<>();
                HashMap<String, PackageTreeObject> map = new HashMap<>();
                for (String entry : types) {
                    int index = entry.indexOf('.');
                    if (index < 0) {
                        continue;
                    }
                    String packageName = entry.substring(0, index);
                    String typeName = entry.substring(index + 1);
                    
                    PackageTreeObject packageObject = map.get(packageName);
                    if (packageObject == null) {
                        packageObject = new PackageTreeObject(this, packageName);
                        result.add(packageObject);
                        map.put(packageName, packageObject);
                    }
                    packageObject.getTypes().add(typeName);
                }
                children = result.toArray(new PackageTreeObject[result.size()]);
            }
            return children;
        }

        @Override
        public int compareTo(ModuleTreeObject o) {
            return this.getName().compareTo(o.getName());
        }

        @Override
        public String getFullName() {
            return "";
        }

        @Override
        public boolean hasChildren() {
            return true;
        }

    }

    private static class PackageTreeObject extends BaseTreeObject {

        private BaseTreeObject[] children = null;
        private ArrayList<String> types;

        public PackageTreeObject(BaseTreeObject parent, String name) {
            super(parent, name);
            this.types = new ArrayList<>();
        }

        public ArrayList<String> getTypes() {
            return types;
        }

        public synchronized BaseTreeObject[] getChildren() {
            if (children == null) {
                ArrayList<PackageTreeObject> childPackages = new ArrayList<>();
                ArrayList<TypeTreeObject> childTypes = new ArrayList<>();
                for (String entry : types) {
                    int index = entry.indexOf('.');
                    if (index == -1) {
                        TypeTreeObject typeObject = new TypeTreeObject(this, entry);
                        childTypes.add(typeObject);
                        
                    } else {
                        String packageName = entry.substring(0, index);
                        String typeName = entry.substring(index + 1);
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
                        packageObject.getTypes().add(typeName);
                    }

                }
                children = new BaseTreeObject[childTypes.size() + childPackages.size()];
                int i = 0;
                for (PackageTreeObject p : childPackages) {
                    children[i++] = p;
                }
                for (TypeTreeObject t : childTypes) {
                    children[i++] = t;
                }
            }
            return children;
        }

        @Override
        public boolean hasChildren() {
            return true;
        }
    }

    private static class TypeTreeObject extends BaseTreeObject {

        private MemberTreeObject[] children = null;
        private boolean childrenInitialized;

        public TypeTreeObject(PackageTreeObject parent, String name) {
            super(parent, name);
        }

        public synchronized BaseTreeObject[] getChildren() {
            if (childrenInitialized) {
                return children;
            }
            if (children == null) {
                String fullName = this.getFullName();
                try {
                    Class<?> type = Class.forName(fullName);
                    ArrayList<MemberTreeObject> result = new ArrayList<>();
                    for (Field f : type.getDeclaredFields()) {
                        if ((f.getModifiers() & Modifier.PUBLIC) == 0) {
                            continue;
                        }
                        result.add(new MemberTreeObject(this, f.getName()));
                    }
                    for (Method m : type.getDeclaredMethods()) {
                        if (!m.getName().equals("<init>") && !m.getName().equals("<clinit>")) {
                            continue;
                        }
                        if ((m.getModifiers() & Modifier.PUBLIC) == 0) {
                            continue;
                        }
                        boolean found = false;
                        for (MemberTreeObject mto : result) {
                            if (mto.getName().equals(m.getName())) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            result.add(new MemberTreeObject(this, m.getName()));
                        }
                    }
                    children = result.toArray(new MemberTreeObject[result.size()]);
                    childrenInitialized = true;
                } catch (ClassNotFoundException|NoClassDefFoundError|ExceptionInInitializerError|UnsatisfiedLinkError e) {
                    // print stacktrace
                    
                }

            }
            return children;
        }

    }

    private static class MemberTreeObject extends BaseTreeObject {
        private static final BaseTreeObject[] emptyArray = {};

        public MemberTreeObject(TypeTreeObject parent, String name) {
            super(parent, name);
        }

        public BaseTreeObject[] getChildren() {
            return emptyArray;
        }

        @Override
        public boolean hasChildren() {
            return false;
        }
    }

}
