package eg.edu.guc.csen.languagelocalization.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import eg.edu.guc.csen.languagelocalization.pomutils.PomHelper;

public class AddLanguageTranslationSupportHandler extends AbstractHandler{
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IProject project = TranslationsWindowHandler.getSelectedProject(event);
        if (project == null) {
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error",
                    "Please select a project");
            return null;
        }

        // check if the project has maven nature
        if (!TranslationsWindowHandler.isMavenProject(project)) {
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error",
                    "Please select a maven project");
            return null;
        }
        String result = PomHelper.updateMavenProject(project);
        if(result != null){
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error",
                    result);
        }
        return null;
    }
}
