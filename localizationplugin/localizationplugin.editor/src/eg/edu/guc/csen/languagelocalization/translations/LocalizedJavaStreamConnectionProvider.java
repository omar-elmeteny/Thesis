package eg.edu.guc.csen.languagelocalization.translations;

import java.util.ArrayList;

import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

public class LocalizedJavaStreamConnectionProvider extends ProcessStreamConnectionProvider {
	public LocalizedJavaStreamConnectionProvider() {
		String javaBinLocation = "\"C:\\Program Files\\Java\\jdk-19\\bin\\java.exe\"";
		String lsInstallPath = "D:\\Projects\\Thesis\\javalanguageserver";
		String lsVersion = "1.6.400.v20210924-0641";
		String workspace = "D:\\Projects\\Thesis\\workspace";
		
		ArrayList<String> commands = new ArrayList<>();
        commands.add(javaBinLocation);
        
        commands.add("-Declipse.application=org.eclipse.jdt.ls.core.id1");
        commands.add("-Dosgi.bundles.defaultStartLevel=4");
        commands.add("-Declipse.product=org.eclipse.jdt.ls.core.product");
        commands.add("-Dlog.level=ALL");
        commands.add("-noverify");
        
        commands.add("-Xmx1G");
        commands.add("--add-modules=ALL-SYSTEM");
        
        commands.add("--add-opens");
        commands.add("java.base/java.util=ALL-UNNAMED");
        commands.add("--add-opens");
        commands.add("java.base/java.lang=ALL-UNNAMED");
        commands.add("-jar");
        commands.add(lsInstallPath + "\\plugins\\org.eclipse.equinox.launcher_" + lsVersion + ".jar");
        commands.add("-configuration");
        commands.add(lsInstallPath + "\\config_win");
        
        commands.add("-data");
        commands.add(workspace);
        setCommands(commands);
        
        StringBuilder builder = new StringBuilder("ARABIC JAVA LS:");
        for (int i = 0; i < commands.size(); i++) {
        	builder.append(' ');
        	builder.append(commands.get(i));
        }
        System.out.println(builder);
        setWorkingDirectory(lsInstallPath);
	}
}
