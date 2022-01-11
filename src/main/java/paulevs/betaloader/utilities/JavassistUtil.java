package paulevs.betaloader.utilities;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import modloader.BaseMod;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;

public class JavassistUtil {
	private static final String JAVASSIST_URL = "https://repo1.maven.org/maven2/org/javassist/javassist/3.28.0-GA/javassist-3.28.0-GA.jar";
	private static final File JAVASSIST = CacheStorage.getCacheFile("javassist.jar");
	
	public static boolean loadJavassist() {
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
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static Class<? extends BaseMod> getModClassJavassist(ClassLoader loader, File modFile, String modClassName) {
		Class<? extends BaseMod> modClass = null;
		try {
			ClassLoader sideLoader = ModsStorage.getSideLoader(modFile);
			modClass = (Class<? extends BaseMod>) sideLoader.loadClass(modClassName);
			Constructor<?> constructor = modClass.getDeclaredConstructor();
			
			if (!Modifier.isPublic(constructor.getModifiers())) {
				ClassPool pool = ClassPool.getDefault();
				pool.insertClassPath(new ClassClassPath(modClass));
				CtClass cc = pool.get(modClassName);
				cc.getConstructor("()V").setModifiers(Modifier.PUBLIC);
				modClass = (Class<? extends BaseMod>) cc.toClass();
			}
			else {
				modClass = (Class<? extends BaseMod>) loader.loadClass(modClassName);
			}
		}
		catch (NotFoundException | CannotCompileException | ClassNotFoundException | NoSuchMethodException e) {
			e.printStackTrace();
		}
		return modClass;
	}
}
