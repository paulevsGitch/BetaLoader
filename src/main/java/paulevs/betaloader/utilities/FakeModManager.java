package paulevs.betaloader.utilities;

import com.google.common.base.CaseFormat;
import paulevs.betaloader.remapping.ModEntry;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class FakeModManager {
	/**
	 * Will create "fake" mods for Fabric and Mod Menu.
	 * Mods will be empty jars with icon and JSON.
	 */
	public static void initFakeMods() {
		List<File> modsInFolder = getModFiles();
		List<ModEntry> loadedMods = ModsStorage.getMods();
		
		List<File> files = getFakeFiles(loadedMods);
		final int count = files.size();
		for (int i = 0; i < count; i++) {
			File file = files.get(i);
			modsInFolder.remove(file);
			if (!file.exists()) {
				ModEntry entry = loadedMods.get(i);
				File folder = makeFolder(file, entry);
				FileUtil.zipDirectory(folder, file);
				FileUtil.deleteFolder(folder);
			}
		}
		
		modsInFolder.forEach(file -> file.delete());
	}
	
	/**
	 * Get list of "fake" mod files from folder.
	 * @return {@link List} of {@link File}.
	 */
	private static List<File> getModFiles() {
		File[] files = ModsStorage.MODS_FOLDER.listFiles();
		return Arrays.stream(files).filter(file -> file.isFile() && isFakeMod(file.getName())).collect(Collectors.toList());
	}
	
	/**
	 * Detects if file has a "fake" mod name.
	 * @param name {@link String} file name.
	 * @return true if file is "fake" mod.
	 */
	private static boolean isFakeMod(String name) {
		return name.startsWith("betaloader_modentry_") && name.endsWith(".jar");
	}
	
	/**
	 * Constructs "fake" mod file instance from mod ID.
	 * @param modID {@link String} mod identifier.
	 * @return {@link File} for "fake" mod.
	 */
	private static File getFile(String modID) {
		return new File(ModsStorage.MODS_FOLDER, String.format(Locale.ROOT, "betaloader_modentry_%s.jar", modID));
	}
	
	/**
	 * Return "fake" mod files that already exists in folder.
	 * @param entries {@link List} of {@link ModEntry}.
	 * @return {@link List} of "fake" mod files.
	 */
	private static List<File> getFakeFiles(List<ModEntry> entries) {
		return entries.stream().map(entry -> getFile(entry.getModID())).collect(Collectors.toList());
	}
	
	/**
	 * Will make a temporary directory for "fake" mod file generation.
	 * @param modFile {@link File} of "fake" mod.
	 * @param modEntry {@link ModEntry} of mod.
	 * @return {@link File} created directory.
	 */
	private static File makeFolder(File modFile, ModEntry modEntry) {
		String name = modFile.getName();
		String clearName = name.substring(0, name.length() - 4);
		File folder = new File(ModsStorage.MODS_FOLDER, clearName);
		folder.mkdirs();
		
		File icon = new File(folder, "assets/" + modEntry.getModID() + "/icon.png");
		BufferedImage image = makeIcon(modEntry.getModID());
		FileUtil.saveImage(icon, image);
		
		List<String> json = makeFabricModJson(modEntry.getModID());
		FileUtil.writeTextFile(json, new File(folder, "fabric.mod.json"));
		
		return folder;
	}
	
	/**
	 * Will create icon for a mod based on its identifier.
	 * @param modID {@link String} mod identifier.
	 * @return generated {@link BufferedImage}.
	 */
	private static BufferedImage makeIcon(String modID) {
		Color color = new Color(modID.hashCode() | (255 << 24));
		float average = color.getRed() * 0.3F + color.getGreen() * 0.3F + color.getBlue() * 0.3F;
		Color secondColor = average > 200 ? Color.BLACK : Color.WHITE;
		
		BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		g.setColor(color);
		g.fillRect(8, 8, 120, 120);
		g.setColor(secondColor);
		g.fillRect(0, 0, 8, 128);
		g.fillRect(120, 0, 8, 128);
		g.fillRect(0, 0, 128, 8);
		g.fillRect(0, 120, 128, 8);
		
		String modName = modID.substring(0, 1).toUpperCase(Locale.ROOT);
		g.setFont(g.getFont().deriveFont(Font.BOLD, 100f));
		int width = g.getFontMetrics().stringWidth(modName);
		g.drawString(modName, (128 - width) >> 1, 80 + (128 - 80) / 2 - 4);
		
		return image;
	}
	
	/**
	 * Will make a fabric JSON file for mod.
	 * @param modID {@link String} mod identifier.
	 * @return {@link List} of {@link String} JSON lines.
	 */
	private static List<String> makeFabricModJson(String modID) {
		String modName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, modID);
		List<String> json = new ArrayList<>(12);
		
		json.add("{");
		json.add("	\"schemaVersion\": 1,");
		json.add("	\"id\": \"" + modID + "\",");
		json.add("	\"version\": \"0.1.0\",");
		json.add("	\"name\": \"" + modName + "\",");
		json.add("	\"description\": \"Mod converted by BetaLoader\",");
		json.add("	\"icon\": \"assets/" + modID + "/icon.png\",");
		json.add("	\"environment\": \"*\",");
		json.add("	\"depends\": {");
		json.add("		\"minecraft\": \"1.0.0-beta.7.3\",");
		json.add("		\"betaloader\": \"*\"");
		json.add("	}");
		json.add("}");
		
		return json;
	}
}
