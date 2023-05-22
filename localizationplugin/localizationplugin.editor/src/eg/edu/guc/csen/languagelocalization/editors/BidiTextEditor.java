package eg.edu.guc.csen.languagelocalization.editors;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;

import eg.edu.guc.csen.translator.Translations;

public class BidiTextEditor extends TextEditor {

    private ISourceViewer sourceViewer;
    private int direction = SWT.LEFT_TO_RIGHT;
    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        sourceViewer =  super.createSourceViewer(parent, ruler, styles);
        loadTranslationsFileAndUpdate();
        return sourceViewer;
    }

    private void loadTranslationsFileAndUpdate() {
        if (!(getEditorInput() instanceof IFileEditorInput)) {
            return;
        }
        IFileEditorInput input = (IFileEditorInput) getEditorInput();
        IFile file = input.getFile();
        IProject project = file.getProject();
        if (project == null) {
            return;
        }
        IFile guctFile = project.getFile("translations.guct");
        if (guctFile == null || !guctFile.exists())  {
            return;
        }
        updateTextEditorDirection(guctFile);
    }

    private void updateTextEditorDirection(IFile guctFile) {
        String content = null;
        try {
            try (InputStream inputStream = guctFile.getContents()) {
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                content = new String(bytes, StandardCharsets.UTF_8);            
            }
        } catch (IOException | CoreException e) {
            e.printStackTrace();
        } 
        if (content == null || content.length() == 0) {
            return;
        }
        Translations translations = new Translations();
        translations.updateFromJSONString(content);

        String language = translations.getDefaultLanguage();
        direction = getLanguageDirection(language);

        if (sourceViewer != null) {
            sourceViewer.getTextWidget().setOrientation(direction);
        }
    }

    private static int getLanguageDirection(String language) {

        if (language == null) {
            return SWT.LEFT_TO_RIGHT;
        }
        switch (language) {
        case "ar":
        case "he":
        case "fa":
        case "ur":
            return SWT.RIGHT_TO_LEFT;
        }

        return SWT.LEFT_TO_RIGHT;
    }

    private IResourceChangeListener resourceChangeListener;
    
    @Override
    protected void initializeEditor() {
        super.initializeEditor();
        
        // Register the resource change listener
        resourceChangeListener = new TanslationsChangeListener();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener);
    }
    
    @Override
    public void dispose() {
        // Unregister the resource change listener
        if (resourceChangeListener != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
            resourceChangeListener = null;
        }
        super.dispose();
    }

    private class TanslationsChangeListener implements IResourceChangeListener {
        @Override
        public void resourceChanged(IResourceChangeEvent event) {
            IResourceDelta delta = event.getDelta();
            if (delta != null) {
                try {
                    delta.accept(new IResourceDeltaVisitor() {
                        @Override
                        public boolean visit(IResourceDelta delta) throws CoreException {
                            IResource resource = delta.getResource();
                            // Check if the resource is the file you want to monitor
                            if (resource instanceof IFile && "translations.guct".equals(resource.getName())) {
                                loadTranslationsFileAndUpdate();
                            }
                            return true; // Continue visiting resources
                        }
                    });
                } catch (CoreException e) {
                    // Handle any exceptions
                }
            }
        }

        
    }

}
