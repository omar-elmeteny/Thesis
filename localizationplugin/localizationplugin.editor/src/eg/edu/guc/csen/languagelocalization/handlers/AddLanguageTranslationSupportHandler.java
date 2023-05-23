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

        // addTranslationsToEclipseClassPath(project);
        new TranslationsWindowHandler().execute(event);
        
        return null;
    }

    // private void addTranslationsToEclipseClassPath(IProject project) {
    //     try {
    //         IJavaProject javaProject = JavaCore.create(project);
    //         IClasspathEntry[] classpath = javaProject.getRawClasspath();
    //         boolean found = false;
    //         for (IClasspathEntry entry : classpath) {
    //             var patterns = entry.getInclusionPatterns();
    //             if(patterns != null){
    //                 for (var pattern : patterns) {
    //                     if(pattern.toString().equals("**/*.guct")){
    //                         found = true;
    //                         break;
    //                     }
    //                 }
    //             }
    //         }

    //         if(!found){
    //             IClasspathEntry newEntry = JavaCore.newSourceEntry(
    //                 javaProject.getPath(), 
    //                 new IPath[] { 
    //                     new Path("**/*.guct")
    //                 },
    //                 null,
    //             null);
    //             IClasspathEntry[] newClasspath = new IClasspathEntry[classpath.length + 1];
    //             System.arraycopy(classpath, 0, newClasspath, 1, classpath.length);
    //             newClasspath[0] = newEntry;
    //             javaProject.setRawClasspath(newClasspath, null);
    //             javaProject.save(null, true);
                
    //         }

    //     } catch (JavaModelException e) {
    //         e.printStackTrace();
    //     }
    // }
}
