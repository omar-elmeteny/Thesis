package eg.edu.guc.csen.languagelocalization.handlers;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

import eg.edu.guc.csen.translator.Translations;


public class TranslationsWindowHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IProject project = getSelectedProject(event);
        if (project == null) {
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error",
                    "Please select a project");
            return null;
        }

        // check if the project has maven nature
        if (!isMavenProject(project)) {
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error",
                    "Please select a maven project");
            return null;
        }
        
        IFile translationsFile = project.getFile("translations.guct");
        if (!translationsFile.exists()) {
            String content = new Translations().toJSON().toString(4);
            try {
                translationsFile.create(new ByteArrayInputStream(content.getBytes()), true, null);
            } catch (CoreException e) {
                e.printStackTrace();
                MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error",
                "Failed to create translations file.");
            }
        }

        FileEditorInput editorInput = new FileEditorInput(translationsFile);
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(editorInput,"eg.edu.guc.csen.languagelocalization.editors.TranslationsEditor");
        } catch (PartInitException e) {
            e.printStackTrace();
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error",
                "Failed to open translations file.");
        }
        

        return null;
    }

    public static boolean isMavenProject(IProject project) {
        try {
            return project.hasNature("org.eclipse.m2e.core.maven2Nature");
        } catch (CoreException e) {    
            e.printStackTrace();
            return true;
        }
    }

    public static IProject getSelectedProject(ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
                    .hasNext();) {
                Object element = it.next();
                IProject project = null;
                if (element instanceof IProject) {
                    project = (IProject) element;
                } else if (element instanceof IAdaptable) {
                    project = ((IAdaptable) element).getAdapter(IProject.class);
                }
                if (project != null) {
                    return project;
                }
            }
        }
        return null;
    }

    public static IFolder getSelectedFolder(ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
                    .hasNext();) {
                Object element = it.next();
                IFolder folder = null;
                if (element instanceof IFolder) {
                    folder = (IFolder) element;
                } else if (element instanceof IAdaptable) {
                    folder = ((IAdaptable) element).getAdapter(IFolder.class);
                }
                if (folder != null) {
                    return folder;
                }
            }
        }
        return null;
    }

    public static IFile getSelectedFile(ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
                    .hasNext();) {
                Object element = it.next();
                IFile file = null;
                if (element instanceof IFile) {
                    file = (IFile) element;
                } else if (element instanceof IAdaptable) {
                    file = ((IAdaptable) element).getAdapter(IFile.class);
                }
                if (file != null) {
                    return file;
                }
            }
        }
        return null;
    }

}
