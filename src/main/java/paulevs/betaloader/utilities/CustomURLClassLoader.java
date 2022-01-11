package paulevs.betaloader.utilities;

import net.minecraft.client.Minecraft;

import java.net.URL;
import java.net.URLClassLoader;

public class CustomURLClassLoader extends URLClassLoader {
	private static CustomURLClassLoader INSTANCE = new CustomURLClassLoader();
	
	private CustomURLClassLoader() {
		super(new URL[512], Minecraft.class.getClassLoader());
	}
	
	public void addURL(URL url) {
		super.addURL(url);
	}
	
	public static CustomURLClassLoader getInstance() {
		return INSTANCE;
	}
}
