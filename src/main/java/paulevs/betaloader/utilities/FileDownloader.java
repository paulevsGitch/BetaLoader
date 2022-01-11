package paulevs.betaloader.utilities;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

public class FileDownloader {
	private static final String MINECRAFT_URL = "https://launcher.mojang.com/v1/objects/43db9b498cb67058d2e12d394e6507722e71bb45/client.jar";
	private static final String JAVASSIST_URL = "https://repo1.maven.org/maven2/org/javassist/javassist/3.28.0-GA/javassist-3.28.0-GA.jar";
	private static final File MINECRAFT = CacheStorage.getCacheFile("minecraft.jar");
	private static final File JAVASSIST = CacheStorage.getCacheFile("javassist.jar");
	
	public static boolean load() {
		if (!FileUtil.downloadFile(MINECRAFT, MINECRAFT_URL, "Minecraft Unmapped Client")) {
			return false;
		}
		
		if (!FileUtil.downloadFile(JAVASSIST, JAVASSIST_URL, "Javassist")) {
			return false;
		}
		
		try {
			ClassLoader loader = Minecraft.class.getClassLoader();
			Method method = loader.getClass().getDeclaredMethod("addURL", URL.class);
			method.setAccessible(true);
			method.invoke(loader, JAVASSIST.toURI().toURL());
			return true;
		}
		catch (MalformedURLException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return true;
	}
}
