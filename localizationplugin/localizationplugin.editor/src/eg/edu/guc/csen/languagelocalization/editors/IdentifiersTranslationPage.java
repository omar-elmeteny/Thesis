package eg.edu.guc.csen.languagelocalization.editors;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.ZipFile;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import eg.edu.guc.csen.keywordtranslator.KeyValuePair;
import eg.edu.guc.csen.keywordtranslator.Language;
import eg.edu.guc.csen.keywordtranslator.Translations;
import eg.edu.guc.csen.keywordtranslator.TranslationsBase;

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

        treeViewer.setInput(new RootTreeObject());

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

    private static abstract class BaseTreeObject {

        private final BaseTreeObject parent;
        private final String name;

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
        }

        public String getFullName() {
            if (parent == null) {
                return name;
            } else {
                String parentName = parent.getFullName();
                if (parentName == null || parentName.isEmpty())
                    return name;
                else
                    return parentName + "/" + name;
            }
        }

        public abstract BaseTreeObject[] getChildren();
    }

    private static ArrayList<String> srcZipEntries = initializeSrcZipEntries();

    private static ArrayList<String> initializeSrcZipEntries() {
        String javaBaseLocation = System.getProperty("java.home");
        String srcLocation = Path.of(javaBaseLocation, "lib", "src.zip").toString();
        try {
            try (ZipFile zipFile = new ZipFile(srcLocation)) {
                ArrayList<String> result = new ArrayList<>();
                var entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    result.add(entry.getName());
                }

                return result;
            }
        } catch (IOException e) {
            // print stacktrace
            e.printStackTrace(System.err);
            return null;
        }
    }

    private static class RootTreeObject extends BaseTreeObject {

        private ModuleTreeObject[] children = null;

        public RootTreeObject() {
            super(null, "");

        }

        public synchronized BaseTreeObject[] getChildren() {
            if (children == null) {
                ArrayList<ModuleTreeObject> result = new ArrayList<>();
                for (String entry : srcZipEntries) {
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

    private static class ModuleTreeObject extends BaseTreeObject {
        private PackageTreeObject[] children = null;

        public ModuleTreeObject(RootTreeObject parent, String name) {
            super(parent, name);
        }

        public synchronized BaseTreeObject[] getChildren() {
            if (children == null) {
                ArrayList<PackageTreeObject> result = new ArrayList<>();
                String fullName = this.getFullName() + "/";
                for (String entry : srcZipEntries) {
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
                    for (PackageTreeObject p : result) {
                        if (p.getName().equals(packageName)) {
                            packageObject = p;
                            break;
                        }
                    }

                    if (packageObject == null) {
                        packageObject = new PackageTreeObject(this, packageName);
                        result.add(packageObject);
                    }
                }
                children = result.toArray(new PackageTreeObject[result.size()]);
            }
            return children;
        }
    }

    private static class PackageTreeObject extends BaseTreeObject {

        private BaseTreeObject[] children = null;

        public PackageTreeObject(BaseTreeObject parent, String name) {
            super(parent, name);
        }

        public synchronized BaseTreeObject[] getChildren() {
            if (children == null) {
                ArrayList<PackageTreeObject> childPackages = new ArrayList<>();
                ArrayList<TypeTreeObject> childTypes = new ArrayList<>();
                String fullName = this.getFullName() + "/";
                for (String entry : srcZipEntries) {
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
                        TypeTreeObject typeObject = null;
                        for (TypeTreeObject c : childTypes) {
                            if (c.getName().equals(typeName)) {
                                typeObject = c;
                                break;
                            }
                        }

                        if (typeObject == null) {
                            typeObject = new TypeTreeObject(this, typeName);
                            childTypes.add(typeObject);
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
    }

    private static class TypeTreeObject extends BaseTreeObject {

        private MemberTreeObject[] children = null;

        public TypeTreeObject(PackageTreeObject parent, String name) {
            super(parent, name);
        }

        public synchronized BaseTreeObject[] getChildren() {
            if (children == null) {
                String fullName = this.getFullName();
                String typeName = fullName.substring(fullName.indexOf('/') + 1).replace("/", ".");
                try {
                    Class<?> type = Class.forName(typeName);
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
                } catch (ClassNotFoundException e) {
                    // print stacktrace
                    e.printStackTrace(System.err);
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
    }

}
