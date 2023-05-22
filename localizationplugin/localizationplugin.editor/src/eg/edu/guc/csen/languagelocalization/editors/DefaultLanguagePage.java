package eg.edu.guc.csen.languagelocalization.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import eg.edu.guc.csen.translator.KeyValuePair;
import eg.edu.guc.csen.translator.Language;
import eg.edu.guc.csen.translator.Translations;

public class DefaultLanguagePage extends Composite{

    private final Combo languageCombo;
	private final KeyValuePair currentLanguage;
    private final Translations translations;
    private final TranslationsEditor parentEditor;
    
    public DefaultLanguagePage(Composite parent, TranslationsEditor parentEditor) {
        super(parent, SWT.NONE);
        this.parentEditor = parentEditor;
        translations = parentEditor.getTranslations();

        GridLayout layout = new GridLayout(1, false);
		this.setLayout(layout);
        
        Composite languageComposite = new Composite(this, SWT.NONE);
		languageComposite.setLayout(new GridLayout(2, false));
		Label languageLabel = new Label(languageComposite, SWT.NONE);
		languageLabel.setText("Default language for the project: ");
        languageCombo = new Combo(languageComposite, SWT.READ_ONLY);
		languageCombo.setItems(TranslationsPage.languageNames);

        Language language = null;
        int languageIndex = 0;
        for(int i = 0;i < TranslationsPage.languages.size();i++){
            Language l = TranslationsPage.languages.get(i);
            if(l.getKey().equals(translations.getDefaultLanguage())){
                languageIndex = i;
                language = l;
                break;
            }
        }
        if(language == null){
            language = TranslationsPage.languages.get(0);
        }
        currentLanguage = new KeyValuePair(language.getKey(), language.getName());
        SelectionAdapter adapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateDefaultLanguage();
			}
		};

		// Add a selection listener to the language dropdown
		languageCombo.addSelectionListener(adapter);

		languageCombo.select(languageIndex);
    }

    protected void updateDefaultLanguage() {
        int selectionIndex = languageCombo.getSelectionIndex();
		if (selectionIndex == -1) {
			return;
		}
		currentLanguage.setKey(TranslationsPage.languages.get(selectionIndex).getKey());
		currentLanguage.setValue(TranslationsPage.languages.get(selectionIndex).getName());
		Language lang = TranslationsPage.languages.get(languageCombo.getSelectionIndex());
        translations.setDefaultLanguage(lang.getKey());
        parentEditor.updateEditor();
    }   
    
    public void updateCombo() {
        String currentLanguageKey = translations.getDefaultLanguage();
        if(currentLanguage.getKey().equals(currentLanguageKey)) {
            return;
        }
        for(int i = 0;i < TranslationsPage.languages.size();i++){
            Language l = TranslationsPage.languages.get(i);
            if(l.getKey().equals(currentLanguageKey)){
                languageCombo.select(i);
                currentLanguage.setKey(l.getKey());
                currentLanguage.setValue(l.getName());
                break;
            }
        }
    }
    
}
