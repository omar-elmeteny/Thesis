package eg.edu.guc.csen.languagelocalization.pomutils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
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
        if (!isTranspilerPluginInstalled(model)) {
            addTranspilerPlugin(model);
            writeFile = true;
        }
        if (!isBuildHelperPluginInstalled(model)) {
            addBuildHelperPlugin(model);
            writeFile = true;
        }
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
