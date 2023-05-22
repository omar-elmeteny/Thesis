package eg.edu.guc.csen.languagelocalization.pomutils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Resource;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

public class PomHelper {

    public static boolean pomXmlExists(IProject project) {
        return project.getFile("pom.xml").exists();
    }

    public static String updateMavenProject(IProject project) {
        if (project == null) {
            return "Please select a project";
        }
        if (!pomXmlExists(project)) {
            return "Please select a maven project";
        }
        IFile pomXmlFile = project.getFile("pom.xml");
        // get OS path to pom.xml file
        String pomXmlPath = pomXmlFile.getLocation().toFile().getAbsolutePath();
        Model model;
        try {
            model = parsePomXml(pomXmlPath);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return "Failed to parse pom.xml file";
        }

        boolean writeFile = false;
        if(!isDependencyAdded(model)) {
            addDependency(model);
            writeFile = true;
        }
        if(!isResourceAdded(model)) {
            addResource(model);
            writeFile = true;
        }
        if (!isTranspilerPluginInstalled(model)) {
            addTranspilerPlugin(model);
            writeFile = true;
        }
        if (!isBuildHelperPluginInstalled(model)) {
            addBuildHelperPlugin(model);
            writeFile = true;
        }
        // if(!isPluginManagementInstalled(model)) {
        //     addPluginManagement(model);
        //     writeFile = true;
        // }
        if (!checkPluginRepository(model, "central", "https://repo1.maven.org/maven2")) {
            writeFile = true;
            addPluginRepository(model, "central", "https://repo1.maven.org/maven2", false);
        }
        if (!checkPluginRepository(model, "linode-thesis-snapshots", "https://maven.languageslocalization.com/snapshots")) {
            writeFile = true;
            addPluginRepository(model, "linode-thesis-snapshots", "https://maven.languageslocalization.com/snapshots", true);
        }
        try {
            if (writeFile) {
                writeMavenPomModelToPomXmlFile(model, pomXmlPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to write to pom.xml file";
        }
        return null;
    }

    private static void writeMavenPomModelToPomXmlFile(Model model, String path) throws IOException {
        // write model to file
        MavenXpp3Writer mavenWriter = new MavenXpp3Writer();
        StringWriter contentWriter = new StringWriter();
        mavenWriter.write(contentWriter, model);
        String content = contentWriter.toString();
        String toMatch = "(\\s*)(<id>transpiler-generate-sources</id>)";
        content = content.replaceAll(toMatch, "$1<?m2e execute onConfiguration?>$1$2");
        try(FileWriter writer = new FileWriter(path)) {
            writer.write(content);
        }
    }

    private static void addDependency(Model model) {
        if(model.getDependencies() == null) {
            model.setDependencies(new ArrayList<>());
        }
        Dependency dependency = new Dependency();
        dependency.setGroupId("eg.edu.guc.csen");
        dependency.setArtifactId("localizationruntimehelper");
        dependency.setVersion("1.0.0-SNAPSHOT");
        model.getDependencies().add(dependency);
    }

    private static boolean isDependencyAdded(Model model) {
        if(model.getDependencies() == null) {
            return false;
        }
        for(Dependency dependency : model.getDependencies()) {
            if(dependency.getGroupId().equals("eg.edu.guc.csen") && dependency.getArtifactId().equals("localizationruntimehelper")) {
                return true;
            }
        }
        return false;
    }

    private static void addResource(Model model) {
        if(model.getBuild() == null) {
            model.setBuild(new org.apache.maven.model.Build());
        }
        if(model.getBuild().getResources() == null) {
            model.getBuild().setResources(new ArrayList<>());
        }
        Resource resource = new Resource();
        resource.setDirectory("${basedir}");
        resource.setIncludes(new ArrayList<>());
        resource.getIncludes().add("translations.guct");
        resource.setExcludes(new ArrayList<>());
        resource.getExcludes().add("**/*.guc");
        model.getBuild().getResources().add(resource);
    }

    private static boolean isResourceAdded(Model model) {
        if(model.getBuild() == null) {
            return false;
        }
        if(model.getBuild().getResources() == null) {
            return false;
        }
        for(Resource resource : model.getBuild().getResources()) {
            if(resource.getDirectory().equals("${basedir}") && resource.getIncludes().contains("translations.guct") && resource.getExcludes().contains("**/*.guc")) {
                return true;
            }
        }
        return false;
    }

    private static void addPluginManagement(Model model) {
        if (model.getBuild() == null) {
            model.setBuild(new org.apache.maven.model.Build());
        }
        if (model.getBuild().getPluginManagement() == null) {
            model.getBuild().setPluginManagement(new org.apache.maven.model.PluginManagement());
        }
        if (model.getBuild().getPluginManagement().getPlugins() == null) {
            model.getBuild().getPluginManagement().setPlugins(new java.util.ArrayList<Plugin>());
        }
        Plugin plugin = new Plugin();
        plugin.setGroupId("org.eclipse.m2e");
        plugin.setArtifactId("lifecycle-mapping");
        plugin.setVersion("1.0.0");
        Xpp3Dom configuration = new Xpp3Dom("configuration");
        Xpp3Dom lifecycleMappingMetadata = new Xpp3Dom("lifecycleMappingMetadata");
        Xpp3Dom pluginExecutions = new Xpp3Dom("pluginExecutions");
        Xpp3Dom pluginExecution = new Xpp3Dom("pluginExecution");
        Xpp3Dom pluginExecutionFilter = new Xpp3Dom("pluginExecutionFilter");
        Xpp3Dom action = new Xpp3Dom("action");
        Xpp3Dom groupId = new Xpp3Dom("groupId");
        groupId.setValue("eg.edu.guc.csen");
        Xpp3Dom artifactId = new Xpp3Dom("artifactId");
        artifactId.setValue("transpiler-maven-plugin");
        Xpp3Dom versionRange = new Xpp3Dom("versionRange");
        versionRange.setValue("[1.0.0,)");
        Xpp3Dom goals = new Xpp3Dom("goals");   
        Xpp3Dom goal = new Xpp3Dom("goal");
        goal.setValue("transpile");
        Xpp3Dom execute = new Xpp3Dom("execute");
        Xpp3Dom runOnIncremental = new Xpp3Dom("runOnIncremental");
        runOnIncremental.setValue("true");
        execute.addChild(runOnIncremental);
        action.addChild(execute);
        goals.addChild(goal);
        pluginExecutionFilter.addChild(groupId);
        pluginExecutionFilter.addChild(artifactId);
        pluginExecutionFilter.addChild(versionRange);
        pluginExecutionFilter.addChild(goals);
        pluginExecution.addChild(pluginExecutionFilter);
        pluginExecution.addChild(action);
        pluginExecutions.addChild(pluginExecution);
        lifecycleMappingMetadata.addChild(pluginExecutions);
        configuration.addChild(lifecycleMappingMetadata);
        plugin.setConfiguration(configuration);
        model.getBuild().getPluginManagement().getPlugins().add(plugin);
    }

    private static void addTranspilerPlugin(Model model) {
        if (model.getBuild() == null) {
            model.setBuild(new org.apache.maven.model.Build());
        }
        if (model.getBuild().getPlugins() == null) {
            model.getBuild().setPlugins(new java.util.ArrayList<Plugin>());
        }
        Plugin plugin = new Plugin();
        plugin.setGroupId("eg.edu.guc.csen");
        plugin.setArtifactId("transpiler-maven-plugin");
        plugin.setVersion("1.0.0-SNAPSHOT");
        plugin.setExecutions(new ArrayList<>());

        PluginExecution execution = new PluginExecution();
        execution.setId("transpiler-generate-sources");
        execution.setPhase("generate-sources");
        execution.setGoals(new ArrayList<>());
        execution.getGoals().add("transpile");
        plugin.getExecutions().add(execution);
        PluginExecution execution2 = new PluginExecution();
        execution2.setId("sourcemap-postprocess");
        execution2.setPhase("compile");
        execution2.setGoals(new ArrayList<>());
        execution2.getGoals().add("sourcemap-postprocessor");
        plugin.getExecutions().add(execution2);
        // add <?m2e execute onConfiguration?>

        model.getBuild().getPlugins().add(plugin);
    }

    private static void addBuildHelperPlugin(Model model) {
        if (model.getBuild() == null) {
            model.setBuild(new org.apache.maven.model.Build());
        }
        if (model.getBuild().getPlugins() == null) {
            model.getBuild().setPlugins(new java.util.ArrayList<Plugin>());
        }
        Plugin plugin = new Plugin();
        plugin.setGroupId("org.codehaus.mojo");
        plugin.setArtifactId("build-helper-maven-plugin");
        plugin.setVersion("3.3.0");
        plugin.setExecutions(new ArrayList<>());

        PluginExecution execution = new PluginExecution();
        execution.setId("add-source");
        execution.setPhase("generate-sources");
        execution.setGoals(new ArrayList<>());
        execution.getGoals().add("add-source");

        Xpp3Dom configuration = new Xpp3Dom("configuration");
        Xpp3Dom sources = new Xpp3Dom("sources");
        Xpp3Dom source = new Xpp3Dom("source");
        source.setValue("${project.build.directory}/generated-sources/guc");
        sources.addChild(source);
        configuration.addChild(sources);
        execution.setConfiguration(configuration);
        plugin.getExecutions().add(execution);

        model.getBuild().getPlugins().add(plugin);
    }

    private static void addPluginRepository(Model model, String id, String url, boolean snapshots) {
        if (model.getPluginRepositories() == null) {
            model.setPluginRepositories(new java.util.ArrayList<org.apache.maven.model.Repository>());
        }
        org.apache.maven.model.Repository repository = new org.apache.maven.model.Repository();
        repository.setId(id);
        repository.setUrl(url);
        if (snapshots) {
            repository.setSnapshots(new org.apache.maven.model.RepositoryPolicy());
            repository.getSnapshots().setEnabled(true);
        }
        model.getPluginRepositories().add(repository);
    }

    private static boolean checkPluginRepository(Model model, String id, String url) {
        if (model.getPluginRepositories() != null) {
            for (int i = 0; i < model.getPluginRepositories().size(); i++) {
                org.apache.maven.model.Repository repository = model.getPluginRepositories().get(i);
                if (repository.getId().equals(id) && repository.getUrl().equals(url)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isTranspilerPluginInstalled(Model model) {
        return isPluginInstalled("eg.edu.guc.csen", "transpiler-maven-plugin", model);
    }

    private static boolean isBuildHelperPluginInstalled(Model model) {
        return isPluginInstalled("org.codehaus.mojo", "build-helper-maven-plugin", model);
    }

    
    private static boolean isPluginManagementInstalled(Model model) {
        if(model.getBuild() != null && model.getBuild().getPluginManagement() != null && model.getBuild().getPluginManagement().getPlugins() != null) {
            for (int i = 0; i < model.getBuild().getPluginManagement().getPlugins().size(); i++) {
                Plugin plugin = model.getBuild().getPluginManagement().getPlugins().get(i);
                if (plugin.getGroupId().equals("org.eclipse.m2e") && plugin.getArtifactId().equals("lifecycle-mapping")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isPluginInstalled(String groupId, String artifcatId, Model model) {
        if (model.getBuild() != null && model.getBuild().getPlugins() != null) {
            for (int i = 0; i < model.getBuild().getPlugins().size(); i++) {
                Plugin plugin = model.getBuild().getPlugins().get(i);
                if (plugin.getGroupId().equals(groupId) && plugin.getArtifactId().equals(artifcatId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Model parsePomXml(String path) throws IOException, XmlPullParserException {
        try(FileReader reader = new FileReader(path)) {
            MavenXpp3Reader mavenreader = new MavenXpp3Reader();
            Model model = mavenreader.read(reader);
            return model;
        }
    }
}
