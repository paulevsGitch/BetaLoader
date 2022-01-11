package paulevs.betaloader.utilities;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.commons.Remapper;
import jdk.internal.org.objectweb.asm.commons.RemappingClassAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

// Based on this topic:
// https://stackoverflow.com/questions/2897685/dynamic-loading-a-class-in-java-with-a-different-package-name?rq=1

public class AdvancedClassLoader extends ClassLoader {
	public static final AdvancedClassLoader INSTANCE = new AdvancedClassLoader("testpacket", new URLClassLoader(new URL[0], URLClassLoader.getSystemClassLoader()));
	private Map<String, byte[]> bytecodes = new HashMap<>();
	private final List<Byte> byteList = new ArrayList<>(Short.MAX_VALUE);
	private final Set<Integer> loadedFiles = new HashSet<>();
	private final URLClassLoader urlClassLoader;
	private final String defaultPackageName;
	private Method method;
	
	public AdvancedClassLoader(String defaultPackageName, URLClassLoader parent) {
		super(parent);
		this.urlClassLoader = parent;
		this.defaultPackageName = defaultPackageName;
		try {
			method = urlClassLoader.getClass().getDeclaredMethod("addURL", URL.class);
			method.setAccessible(true);
		}
		catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	public void addURL(URL url) {
		try {
			method.invoke(urlClassLoader, url);
		}
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (name.contains(".")) {
			return urlClassLoader.loadClass(name);
		}
		
		System.out.println(name);
		byte[] bytecode = bytecodes.get(name);//loadBytecode(name);
		//bytecodes.remove(name);
		System.out.println(bytecode);
		byte[] remappedBytecode;
		
		try {
			remappedBytecode = rewriteDefaultPackageClassNames(bytecode);
		}
		catch (IOException e) {
			throw new RuntimeException("Could not rewrite class " + name);
		}
		
		return defineClass(/*defaultPackageName + "/" +*/ name, remappedBytecode, 0, remappedBytecode.length);
	}
	
	public byte[] rewriteDefaultPackageClassNames(byte[] bytecode) throws IOException {
		ClassReader classReader = new ClassReader(bytecode);
		ClassWriter classWriter = new ClassWriter(classReader, 0);
		
		Remapper remapper = new DefaultPackageClassNameRemapper();
		classReader.accept(new RemappingClassAdapter(classWriter, remapper), ClassReader.EXPAND_FRAMES);
		
		return classWriter.toByteArray();
	}
	
	public void addFile(File file) throws IOException {
		int hash = file.getName().hashCode();
		if (loadedFiles.contains(hash)) {
			System.out.println("Already loaded!");
			return;
		}
		
		loadedFiles.add(hash);
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			String name = entry.getName();
			if ((!entry.isDirectory()) && name.endsWith(".class")) {
				InputStream stream = zipFile.getInputStream(entry);
				bytecodes.put(name.substring(0, name.length() - 6), loadBytecode(stream));
			}
		}
		
		zipFile.close();
	}
	
	private byte[] loadBytecode(InputStream stream) throws IOException {
		/*try {
			String originalName = "./" + name.substring(name.lastIndexOf(".") + 1) + ".class";
			System.out.println(originalName);
			InputStream resource = AdvancedClassLoader.class.getResourceAsStream(originalName);
			System.out.println(resource);
			while (resource.available() > 0) {
				byteList.add((byte) (resource.read() & 255));
			}
			resource.close();
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}*/
		
		while (stream.available() > 0) {
			byteList.add((byte) (stream.read() & 255));
		}
		stream.close();
		
		byte[] result = new byte[byteList.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = byteList.get(i);
		}
		byteList.clear();
		
		return result;
	}
	
	class DefaultPackageClassNameRemapper extends Remapper {
		@Override
		public String map(String typeName) {
			boolean hasPackageName = typeName.indexOf('/') != -1;
			System.out.println(typeName + " " + hasPackageName);
			if (hasPackageName) {
				return typeName;
			} else {
				return typeName;//defaultPackageName + "." + typeName;
			}
		}
	}
}
