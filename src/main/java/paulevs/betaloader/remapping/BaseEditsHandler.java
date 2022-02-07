package paulevs.betaloader.remapping;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;

public class BaseEditsHandler {
	private static ClassLoader modClassLoader;
	private static ClassLoader mcClassLoader;
	
	public static void init() {
		URL[] paths = new URL[1];
		try {
			File file = new File(BaseEditsHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			file = new File(file.getParentFile().getParentFile(), ".fabric/remappedJars/minecraft-1.0.0-beta.7.3/intermediary-minecraft.jar");
			if (file.exists()) {
				paths[0] = file.toURI().toURL();
			}
		}
		catch (URISyntaxException | MalformedURLException e) {
			e.printStackTrace();
		}
		mcClassLoader = new URLClassLoader(paths);
	}
	
	public static void getEditedClassLines() {
	
	}
}
