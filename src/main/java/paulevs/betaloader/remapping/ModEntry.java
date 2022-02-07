package paulevs.betaloader.remapping;

import com.google.common.base.CaseFormat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.io.CompoundTag;
import net.modificationstation.stationapi.api.registry.ModID;
import paulevs.betaloader.utilities.FakeModManager;
import paulevs.betaloader.utilities.FileUtil;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ModEntry {
	private final List<String> modClasses;
	private final File modConvertedFile;
	private final File modOriginalFile;
	private final String mainClass;
	private final String classpath;
	private final ModID modID;
	
	public ModEntry(File original, File converted, String classpath, ModID modID, String mainClass, List<String> modClasses) {
		this.modClasses = Collections.unmodifiableList(modClasses);
		this.modConvertedFile = converted;
		this.modOriginalFile = original;
		this.mainClass = mainClass;
		this.classpath = classpath;
		this.modID = modID;
	}
	
	/**
	 * Get all classes from mod file. Collection is unmodifiable.
	 * @return {@link List} of {@link String} mod class names, without ".class" ending part.
	 */
	public List<String> getModClasses() {
		return modClasses;
	}
	
	/**
	 * Get mod main class name, without ".class" ending part. Class is in converted package.
	 * @return {@link String} mod main class name.
	 */
	public String getMainClass() {
		return mainClass;
	}
	
	/**
	 * Get mod file after patching.
	 * @return {@link File} for mod.
	 */
	public File getModConvertedFile() {
		return modConvertedFile;
	}
	
	/**
	 * Get mod file before patching.
	 * @return {@link File} for mod.
	 */
	public File getModOriginalFile() {
		return modOriginalFile;
	}
	
	/**
	 * Get mod string identifier (made from main class name).
	 * @return {@link ModID} mod identifier.
	 */
	public ModID getModID() {
		return modID;
	}
	
	/**
	 * Get mod classpath (package).
	 * @return
	 */
	public String getClasspath() {
		return classpath;
	}
	
	/**
	 * Will check if mod requires update and modify mods data {@link CompoundTag}.
	 * @param modsData mods data {@link CompoundTag}.
	 * @return
	 */
	public boolean requireUpdate(CompoundTag modsData) {
		if (!modConvertedFile.exists()) {
			return true;
		}
		
		int hash = modOriginalFile.hashCode();
		long modified = modOriginalFile.lastModified();
		
		String modIDName = this.modID.toString();
		if (modsData.containsKey(modIDName)) {
			CompoundTag root = modsData.getCompoundTag(modIDName);
			if (root.getLong("modified") != modified || root.getInt("hash") != hash) {
				root.put("modified", modified);
				root.put("hash", hash);
				return true;
			}
		}
		else {
			CompoundTag root = new CompoundTag();
			modsData.put(modIDName, root);
			root.put("modified", modified);
			root.put("hash", hash);
			return true;
		}
		return false;
	}
	
	public static ModEntry makeEntry(File modFile, File remappedDir) {
		List<String> modClasses = FileUtil.getZipClasses(modFile);
		Optional<String> optional = modClasses.stream().filter(name -> name.startsWith("mod_")).findAny();
		if (!optional.isPresent()) {
			return null;
		}
		String mainClass = optional.get();
		String modID = classToID(mainClass);
		String modName = modFile.getName();
		File converted = new File(remappedDir, modName.substring(0, modName.length() - 4) + ".jar");
		String classpath = FabricLoader.getInstance().isDevelopmentEnvironment() ? modID : "net.minecraft";
		FakeModManager.addModEntry(modID);
		return new ModEntry(modFile, converted, classpath, ModID.of(modID), mainClass, modClasses);
	}
	
	private static String classToID(String modClass) {
		return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, modClass.substring(4));
	}
}
