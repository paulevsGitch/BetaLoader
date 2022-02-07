package paulevs.betaloader.utilities;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.NBTIO;
import paulevs.betaloader.remapping.ModEntry;
import paulevs.betaloader.remapping.RemapUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ModsStorage {
	public static final File MODS_FOLDER = new File(FabricLoader.getInstance().getGameDir().toString(), "mods");
	private static final File CONVERTED_FOLDER = CacheStorage.getCacheFile("converted_mods");
	private static final File MODS_DATA = CacheStorage.getCacheFile("mods.nbt");
	
	private static final List<ModEntry> PATCHED_MODS = new ArrayList<>();
	
	private static ClassLoader sideLoader;
	public static ModEntry loadingMod;
	
	// TODO replace JarJar with tiny remapper additional mappings
	public static void process() {
		CONVERTED_FOLDER.mkdirs();
		MODS_FOLDER.mkdirs();
		
		CompoundTag modsData = getModsDataTag(MODS_DATA);
		
		File[] modsInFolder = MODS_FOLDER.listFiles();
		List<ModEntry> addMods = new ArrayList<>();
		
		for (File mod: modsInFolder) {
			if (mod.isFile() && (mod.getName().endsWith(".zip") || mod.getName().endsWith(".jar"))) {
				ModEntry entry = ModEntry.makeEntry(mod, CONVERTED_FOLDER);
				if (entry == null) {
					continue;
				}
				PATCHED_MODS.add(entry);
				if (entry.requireUpdate(modsData)) {
					addMods.add(entry);
				}
			}
		}
		
		if (!addMods.isEmpty()) {
			saveTag(modsData, MODS_DATA);
			RemapUtil.remap(addMods, CONVERTED_FOLDER);
		}
	}
	
	/**
	 * Get converted mods with proper classpath.
	 * @return {@link List} of mod {@link ModEntry}.
	 */
	public static List<ModEntry> getMods() {
		return PATCHED_MODS;
	}
	
	public static ClassLoader getSideLoader(File modFile) {
		if (sideLoader != null) {
			try {
				Method method = sideLoader.getClass().getDeclaredMethod("addURL", URL.class);
				method.setAccessible(true);
				method.invoke(sideLoader, modFile.toURI().toURL());
			}
			catch (MalformedURLException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				e.printStackTrace();
			}
			return sideLoader;
		}
		
		List<Path> paths = new ArrayList<>();
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
}
