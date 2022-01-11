package paulevs.betaloader.utilities;

import com.google.common.base.CaseFormat;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.discovery.RuntimeModRemapper;
import net.fabricmc.loader.launch.common.FabricLauncher;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.loader.util.UrlConversionException;
import net.fabricmc.loader.util.UrlUtil;
import net.fabricmc.loader.util.mappings.TinyRemapperMappingsHelper;
import net.fabricmc.mapping.tree.TinyTree;
import net.fabricmc.tinyremapper.IMappingProvider;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.NBTIO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ModsStorage {
	private static final String MINECRAFT_URL = "https://launcher.mojang.com/v1/objects/43db9b498cb67058d2e12d394e6507722e71bb45/client.jar";
	private static final String JAVASSIST_URL = "https://repo1.maven.org/maven2/org/javassist/javassist/3.28.0-GA/javassist-3.28.0-GA.jar";
	private static final String JARJAR_URL = "https://storage.googleapis.com/google-code-archive-downloads/v2/code.google.com/jarjar/jarjar-1.4.jar";
	
	private static final File MODS_FOLDER = new File(FabricLoader.getInstance().getGameDir().toString(), "mods");
	private static final File CONVERTED_FOLDER = CacheStorage.getCacheFile("converted_mods");
	private static final File MINECRAFT = CacheStorage.getCacheFile("minecraft.jar");
	private static final File JAVASSIST = CacheStorage.getCacheFile("javassist.jar");
	private static final File MODS_DATA = CacheStorage.getCacheFile("mods.nbt");
	private static final File JARJAR = CacheStorage.getCacheFile("jarjar.jar");
	private static final File REMAP = CacheStorage.getCacheFile("remap.tiny");
	private static final File RULES = CacheStorage.getCacheFile("rules.txt");
	
	private static final Map<File, String> MOD_MAIN_CLASSES = new HashMap<>();
	private static final Map<File, String> MOD_NAMESPACES = new HashMap<>();
	private static final List<File> PATCHED_MODS = new ArrayList<>();
	
	private static TinyTree tinyTree;
	private static ClassLoader sideLoader;
	
	// TODO replace JarJar with tiny remapper additional mappings
	public static void process() {
		CONVERTED_FOLDER.mkdirs();
		MODS_FOLDER.mkdirs();
		
		if (!downloadFile(JARJAR, JARJAR_URL, "Jar Jar Links")) {
			System.out.println("Abort mod loading process!");
			return;
		}
		
		if (!downloadFile(MINECRAFT, MINECRAFT_URL, "Minecraft Unmapped Client")) {
			System.out.println("Abort mod loading process!");
			return;
		}
		
		CompoundTag modsData = getModsDataTag(MODS_DATA);
		
		File[] patchedMods = CONVERTED_FOLDER.listFiles();
		Set<File> modsInFolder = getFiles(MODS_FOLDER);
		List<File> addMods = new ArrayList<>();
		
		for (File mod: modsInFolder) {
			String modClass = loadModClass(mod);
			if (modClass.isEmpty()) {
				continue;
			}
			String modID = classToID(modClass);
			
			File patchedMod = new File(CONVERTED_FOLDER, mod.getName().replaceFirst(".zip", ".jar"));
			MOD_NAMESPACES.put(patchedMod, modID);
			MOD_MAIN_CLASSES.put(patchedMod, modID + "." + modClass.substring(0, modClass.length() - 6));
			PATCHED_MODS.add(patchedMod);
			
			long modified = mod.lastModified();
			int hash = mod.hashCode();
			if (modsData.containsKey(modID)) {
				if (!hasFile(patchedMod, patchedMods)) {
					addMods.add(mod);
				}
				else {
					CompoundTag root = modsData.getCompoundTag(modID);
					if (root.getLong("modified") != modified || root.getInt("hash") != hash) {
						addMods.add(mod);
					}
				}
			}
			else {
				CompoundTag root = new CompoundTag();
				modsData.put(modID, root);
				root.put("modified", modified);
				root.put("hash", hash);
				addMods.add(mod);
			}
		}
		
		if (!addMods.isEmpty()) {
			saveTag(modsData, MODS_DATA);
			
			if (!REMAP.exists()) {
				List<String> customMappings = new ArrayList<>();
				customMappings.add("v1\tintermediary\tclient\tserver\tnamed");
				customMappings.add("METHOD\tnet/minecraft/class_81\t()Ljava/io/File;\tcallGetParentFolder\ta\ta\tcallGetParentFolder");
				writeTextFile(customMappings, REMAP);
			}
			
			FabricLauncher launcher = FabricLauncherBase.getLauncher();
			//IMappingProvider customProvider = TinyUtils.createTinyMappingProvider(REMAP.toPath(), "client", launcher.getTargetNamespace());
			
			TinyRemapper remapper = TinyRemapper
				.newRemapper()
				//.withMappings(customProvider)
				.withMappings(TinyRemapperMappingsHelper.create(launcher.getMappingConfiguration().getMappings(), "client", launcher.getTargetNamespace()))
				.renameInvalidLocals(true)
				.ignoreFieldDesc(false)
				.propagatePrivate(true)
				.fixPackageAccess(true)
				.ignoreConflicts(true)
				.build();
			
			//Set<Path> depPaths = new HashSet<>();
			//depPaths.add(MINECRAFT.toPath());
			/*for (URL url : launcher.getLoadTimeDependencies()) {
				try {
					Path path = UrlUtil.asPath(url);
					if (!Files.exists(path)) {
						throw new RuntimeException("Path does not exist: " + path);
					}
					depPaths.add(path);
				}
				catch (UrlConversionException e) {
					throw new RuntimeException("Failed to convert '" + url + "' to path!", e);
				}
			}*/
			
			//System.out.println("\n\nPaths:\n");
			//depPaths.forEach(path -> System.out.println(path));
			//System.out.println("\n\n\n");
			
			//depPaths.forEach(path -> remapper.readClassPathAsync(path));
			
			remapper.readClassPath(MINECRAFT.toPath());
			
			List<String> constantRules = new ArrayList<>(8);
			List<String> rules = new ArrayList<>(8);
			
			constantRules.add("rule EntityRendererProxy modloader.EntityRendererProxy");
			constantRules.add("rule ModTextureAnimation modloader.ModTextureAnimation");
			constantRules.add("rule ModTextureStatic modloader.ModTextureStatic");
			constantRules.add("rule ModLoader modloader.ModLoader");
			constantRules.add("rule BaseMod modloader.BaseMod");
			constantRules.add("rule MLProp modloader.MLProp");
			
			final int count = addMods.size();
			for (int i = 0; i < count; i++) {
				File mod = addMods.get(i);
				File pathedMod = PATCHED_MODS.get(i);
				
				rules.addAll(constantRules);
				rules.add(String.format("rule * %s.@1", MOD_NAMESPACES.get(pathedMod)));
				writeTextFile(rules, RULES);
				rules.clear();
				
				String preName = CONVERTED_FOLDER.getAbsolutePath() + "/" + mod.getName().substring(0, mod.getName().length() - 4);
				String remName = preName + "mid.jar";
				String finName = preName + ".jar";
				File mid = new File(remName);
				File map = new File(finName);
				map.delete();
				
				try {
					Path output = mid.toPath();
					Path input = mod.toPath();
					
					System.out.println("Remap " + finName);
					
					OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(output).assumeArchive(true).build();
					outputConsumer.addNonClassFiles(input);
					
					remapper.readInputs(input);
					remapper.apply(outputConsumer);
					remapper.finish();
					
					outputConsumer.close();
				}
				catch (Exception e) {
					remapper.finish();
					throw new RuntimeException("Failed to remap jar", e);
				}
				
				runJarJar(RULES.getAbsolutePath(), remName, finName);
				
				mid.delete();
			}
		}
	}
	
	// TODO remove javassist
	public static boolean loadJavassist() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			return true;
		}
		
		if (!downloadFile(JAVASSIST, JAVASSIST_URL, "Javassist")) {
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
	
	/**
	 * Get converted mods with proper classpath.
	 * @return {@link Collection} of mods {@link File}.
	 */
	// TODO enhance this, replace with mod entries
	public static Collection<File> getMods() {
		return PATCHED_MODS;
	}
	
	/**
	 * Get mod class for mod file.
	 * @param file
	 * @return
	 */
	// TODO enhance this, replace with mod entries
	public static String getModClass(File file) {
		return MOD_MAIN_CLASSES.get(file);
	}
	
	/**
	 * Get mod class for mod ID string for mod file.
	 * @param file
	 * @return
	 */
	// TODO enhance this, replace with mod entries
	public static String getModID(File file) {
		return MOD_NAMESPACES.get(file);
	}
	
	public static ClassLoader getSideLoader(File modFile) {
		if (sideLoader != null) {
			try {
				Method method = sideLoader.getClass().getDeclaredMethod("addURL", URL.class);
				method.setAccessible(true);
				method.invoke(sideLoader, modFile.toURI().toURL());
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
			} return sideLoader;
		}
		
		List<Path> paths = new ArrayList<Path>();
		List<URL> urls = new ArrayList<>(FabricLauncherBase.getLauncher().getLoadTimeDependencies());
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			paths.add(FabricLoader.getInstance().getModContainer("betaloader").get().getRootPath());
		}
		else {
			try {
				File file = new File(ModsStorage.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				paths.add(file.toPath());
				file = new File(file.getParentFile().getParentFile(), ".fabric/remappedJars/minecraft-1.0.0-beta.7.3/intermediary-minecraft.jar");
				if (file.exists()) {
					paths.add(file.toPath());
				}
			}
			catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
		
		try {
			urls.add(modFile.toURI().toURL());
			for (Path path: paths) {
				urls.add(path.toUri().toURL());
			}
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		sideLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
		return sideLoader;
	}
	
	private static CompoundTag getModsDataTag(File file) {
		CompoundTag tag = null;
		if (file.exists()) {
			try {
				FileInputStream stream = new FileInputStream(file);
				tag = NBTIO.readGzipped(stream);
				stream.close();
			}
			catch (IOException exception) {
				exception.printStackTrace();
			}
		}
		return tag == null ? new CompoundTag() : tag;
	}
	
	private static void saveTag(CompoundTag tag, File file) {
		file.getParentFile().mkdirs();
		try {
			FileOutputStream stream = new FileOutputStream(file);
			NBTIO.writeGzipped(tag, stream);
			stream.close();
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	private static Set<File> getFiles(File dir) {
		return Arrays.stream(dir.listFiles()).filter(file -> {
			if (file.isFile()) {
				String name = file.getName();
				return name.endsWith(".jar") || name.endsWith(".zip");
			}
			return false;
		}).collect(Collectors.toSet());
	}
	
	private static String loadModClass(File file) {
		String modClass = "";
		
		try {
			ZipFile zipFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();
				if ((!entry.isDirectory()) && name.endsWith(".class") && name.startsWith("mod_")) {
					modClass = name;
					break;
				}
			}
			
			zipFile.close();
		}
		catch (ZipException e) {
			e.printStackTrace();
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}
		
		return modClass;
	}
	
	private static String classToID(String modClass) {
		return toSnakeCase(modClass.substring(4, modClass.length() - 6));
	}
	
	private static String toSnakeCase(String input) {
		return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, input);
	}
	
	private static boolean downloadFile(String url, File file) {
		try {
			InputStream stream = new URL(url).openStream();
			Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			stream.close();
			return true;
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}
		return false;
	}
	
	private static void writeTextFile(Collection<String> lines, File file) {
		try {
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
			for (String line : lines) {
				bufferedWriter.append(line);
				bufferedWriter.append("\n");
			}
			bufferedWriter.close();
			fileWriter.close();
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}
	}
	
	private static boolean runJarJar(String rules, String from, String to) {
		StringBuilder command = new StringBuilder();
		command.append("java -jar ");
		command.append("\"");
		command.append(JARJAR.getAbsolutePath());
		command.append("\" process \"");
		command.append(rules);
		command.append("\" \"");
		command.append(from);
		command.append("\" \"");
		command.append(to);
		command.append("\"");
		
		try {
			Process process = Runtime.getRuntime().exec(command.toString());
			process.waitFor();
			if (process.exitValue() == 0) {
				return true;
			}
		}
		catch (IOException | InterruptedException exception) {
			exception.printStackTrace();
		}
		return false;
	}
	
	private static boolean downloadFile(File file, String url, String name) {
		if (!file.exists()) {
			System.out.println(name + " is missing, trying to download");
			if (downloadFile(url, file)) {
				System.out.println("Success!");
			}
			else {
				System.out.println("Failed to download " + name + "!");
				System.out.println("You can try to download it manually:");
				System.out.println(url);
				name = file.getName();
				String path = file.getParentFile().getAbsolutePath();
				System.out.println("Rename file to \"" + name + "\" and place it into " + path + " directory.");
				return false;
			}
		}
		return true;
	}
	
	private static boolean hasFile(File file, File[] folder) {
		for (File f: folder) {
			if (f.equals(file)) {
				return true;
			}
		}
		return false;
	}
}
