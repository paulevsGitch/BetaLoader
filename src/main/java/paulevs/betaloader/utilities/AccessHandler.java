package paulevs.betaloader.utilities;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import net.minecraft.level.dimension.DimensionFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class AccessHandler {
	/**
	 * Will change access to fields and methods in target class.
	 * Old mods used default package and ignored protected access.
	 * New mods uses packages and require some small access handling.
	 * @param className target {@link String} to modify access.
	 */
	public static void changeClassAccess(String className) {
		System.out.println("Change access for: " + className);
		
		ClassPool pool = ClassPool.getDefault();
		try {
			pool.appendClassPath("D:/MultiMC/instances/BetaLoader tests/.minecraft/.fabric/remappedJars/minecraft-1.0.0-beta.7.3/intermediary-minecraft.jar");
		}
		catch (NotFoundException e) {
			e.printStackTrace();
		}
		//pool.insertClassPath(new ClassClassPath(className));
		try {
			CtClass cc = pool.get(className);
			
			CtMethod[] methods = cc.getDeclaredMethods();
			for (CtMethod method : methods) {
				System.out.println("Method: " + method.getName());
				System.out.println("Modifiers: " + Modifier.toString(method.getModifiers()));
				if (Modifier.isProtected(method.getModifiers())) {
					method.setModifiers(Modifier.PUBLIC);
				}
			}
			
			CtField[] fields = cc.getFields();
			for (CtField field : fields) {
				if (Modifier.isProtected(field.getModifiers())) {
					System.out.println("Field: " + field.getName());
					field.setModifiers(Modifier.PUBLIC);
				}
			}
		}
		catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void changeClassAccess(Class classInst) {
		System.out.println("Change access for: " + classInst.getName());
		
		ClassPool pool = ClassPool.getDefault();
		pool.insertClassPath(new ClassClassPath(classInst));
		try {
			CtClass cc = pool.get(classInst.getName());
			
			CtMethod[] methods = cc.getDeclaredMethods();
			for (CtMethod method : methods) {
				System.out.println("Method: " + method.getName());
				System.out.println("Modifiers: " + Modifier.toString(method.getModifiers()));
				if (Modifier.isProtected(method.getModifiers())) {
					method.setModifiers(Modifier.PUBLIC);
				}
			}
			
			CtField[] fields = cc.getFields();
			for (CtField field : fields) {
				if (Modifier.isProtected(field.getModifiers())) {
					System.out.println("Field: " + field.getName());
					field.setModifiers(Modifier.PUBLIC);
				}
			}
		}
		catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
	
	// https://stackoverflow.com/questions/22591903/javassist-how-to-inject-a-method-into-a-class-in-jar/22592795
	
	public static void replaceDefault() {
		Map<String, String> replacements = new HashMap<>();
		replacements.put("", "paulevs.betaloader.utilities.ReflectionUtil.callMethod(File.class, f, \"getParentFolder\")");
	}
	
	public static void changeAccess(File modFile, Map<String, String> replacements) {
		//ClassPool pool = ClassPool.getDefault();
		//pool.insertClassPath(modFile.toString());
		DimensionFile f = new DimensionFile(new File("a"), "w", false);
		System.out.println(ReflectionUtil.callMethod(File.class, f, "getParentFolder").getName());
	}
}
