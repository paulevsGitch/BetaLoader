package paulevs.betaloader.remapping;

import com.google.common.base.CaseFormat;
import net.minecraft.util.io.CompoundTag;
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
	private final String modID;
	
	public ModEntry(File original, File converted, String modID, String mainClass, List<String> modClasses) {
		this.modClasses = Collections.unmodifiableList(modClasses);
		this.modConvertedFile = converted;
		this.modOriginalFile = original;
		this.mainClass = mainClass;
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
	 * @return {@link String} mod identifier.
	 */
	public String getModID() {
		return modID;
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
		
		if (modsData.containsKey(modID)) {
			CompoundTag root = modsData.getCompoundTag(modID);
			if (root.getLong("modified") != modified || root.getInt("hash") != hash) {
				root.put("modified", modified);
				root.put("hash", hash);
				return true;
			}
		}
		else {
			CompoundTag root = new CompoundTag();
			modsData.put(modID, root);
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
		return new ModEntry(modFile, converted, modID, modID + "." + mainClass, modClasses);
	}
	
	private static String classToID(String modClass) {
		return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, modClass.substring(4));
	}
}
