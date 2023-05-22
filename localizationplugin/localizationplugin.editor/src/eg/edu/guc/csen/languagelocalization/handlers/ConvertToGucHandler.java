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

import eg.edu.guc.csen.translator.Translations;
import eg.edu.guc.csen.transpiler.Transpiler;
import eg.edu.guc.csen.transpiler.TranspilerOptions;

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
        try {
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
        } catch (IOException | CoreException e) {
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error",
                "Failed to convert files: " + e.getMessage() + ".");
            e.printStackTrace();
        }
        return null;
    }

    private void convertProject(IProject project, TranspilerOptions options) throws IOException, CoreException {
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
    }

    private void convertFolder(IFolder folder, TranspilerOptions options) throws IOException, CoreException {
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
    }

    private void convertFile(IFile file, TranspilerOptions options) throws IOException, CoreException {
        // ignore non java files
        if(!file.getName().endsWith(".java")) {
            return;
        }
        // ignore files in target folder
        if(file.getProjectRelativePath().toString().startsWith("target")) {
            return;
        }
        IFile gucFile = file.getProject().getFile(file.getProjectRelativePath().toString().replaceAll("\\.java", ".guc"));
        // read the file
        try(InputStream inputStream = file.getContents()) {
            String contents = new String(inputStream.readAllBytes(), options.getSourceEncoding());
            String result = Transpiler.transpile(options, contents);
            gucFile.create(new ByteArrayInputStream(result.getBytes()), true, null);
        }
        String backupPath = file.getProjectRelativePath().lastSegment().replaceAll("\\.java", ".java.bak");
        file.copy(file.getFullPath().removeLastSegments(1).append(backupPath), true, null);
        file.delete(true, true, null);
    }
    
}
