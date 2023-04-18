package eg.edu.guc.csen.languagelocalization.handlers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import eg.edu.guc.csen.keywordtranslator.Translations;
import eg.edu.guc.csen.localizedtranspiler.Transpiler;
import eg.edu.guc.csen.localizedtranspiler.TranspilerOptions;

public class ConvertToGucHandler extends AbstractHandler{

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IProject project = TranslationsWindowHandler.getSelectedProject(event);
        if(project == null) {
            return null;
        }
        TranspilerOptions options = new TranspilerOptions();
        options.setSourceLanguage("en");
        options.setSourceEncoding("UTF-8");
        options.setOutputEncoding("UTF-8");
        IFile translationsFile = project.getFile("translations.guct");
        if (!translationsFile.exists()) {
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error",
                "The project does not have a translations file.");
            return null;
        }
        File translations = translationsFile.getRawLocation().makeAbsolute().toFile();
        Translations translationsObject;
        try {
            translationsObject = new Translations(translations);
        } catch (IOException e) {
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error",
                "Failed to read translations file.");
            e.printStackTrace();
            return null;
        }
        options.setTranslations(translationsObject);
        options.setTargetLanguage(translationsObject.getDefaultLanguage());
        IFile file = TranslationsWindowHandler.getSelectedFile(event);
        if(file != null) {
            convertFile(file, options);
            return null;
        }
        IFolder folder = TranslationsWindowHandler.getSelectedFolder(event);
        if(folder != null) {
            convertFolder(folder, options);
            return null;
        }
        convertProject(project, options);
        return null;
    }

    private void convertProject(IProject project, TranspilerOptions options) {
        try {
            for(IResource resource: project.members()) {
                if(resource instanceof IProject) {
                    convertProject((IProject) resource, options);
                }
                if(resource instanceof IFile) {
                    convertFile((IFile) resource, options);
                }
                if(resource instanceof IFolder) {
                    convertFolder((IFolder) resource, options);
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    private void convertFolder(IFolder folder, TranspilerOptions options) {
        try {
            for(IResource resource: folder.members()) {
                if(resource instanceof IProject) {
                    convertProject((IProject) resource, options);
                }
                if(resource instanceof IFile) {
                    convertFile((IFile) resource, options);
                }
                if(resource instanceof IFolder) {
                    convertFolder((IFolder) resource, options);
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    private void convertFile(IFile file, TranspilerOptions options) {
        // ignore non java files
        if(!file.getName().endsWith(".java")) {
            return;
        }
        // ignore files in target folder
        if(file.getProjectRelativePath().toString().startsWith("target")) {
            return;
        }
        IFile gucFile = file.getProject().getFile(file.getProjectRelativePath().toString().replaceAll(".java", ".guc"));
        // read the file
        try {
            InputStream inputStream = file.getContents();
            String contents = new String(inputStream.readAllBytes(), options.getSourceEncoding());
            String result = Transpiler.transpile(options, contents);
            gucFile.create(new ByteArrayInputStream(result.getBytes()), true, null);
        } catch (CoreException | IOException e) {
            e.printStackTrace();
            return;
        }
    }
    
}
