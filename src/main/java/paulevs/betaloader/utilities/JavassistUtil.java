package paulevs.betaloader.utilities;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import modloader.BaseMod;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class JavassistUtil {
	@SuppressWarnings("unchecked")
	public static Class<? extends BaseMod> getModClass(ClassLoader loader, File modFile, String modClassName) {
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
