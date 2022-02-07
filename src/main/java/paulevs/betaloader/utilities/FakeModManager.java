package paulevs.betaloader.utilities;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.ModContainer;
import net.fabricmc.loader.api.FabricLoader;
import paulevs.betaloader.containers.BLModContainer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FakeModManager {
	/**
	 * Will create "fake" mods for Fabric, STAPI and Mod Menu.
	 */
	public static void addModEntry(String modID) {
		FabricLoader loader = FabricLoader.getInstance();
		
		if (loader.getEnvironmentType() == EnvType.CLIENT) {
			File iconFolder = CacheStorage.getCacheFile("icons");
			File iconFile = new File(iconFolder, modID + ".png");
			if (!iconFile.exists()) {
				BufferedImage icon = makeIcon(modID);
				FileUtil.saveImage(iconFile, icon);
			}
		}
		
		// Using reflections instead of accessor - accessor will not work
		Map<String, ModContainer> modMap = getModMap(loader);
		List<ModContainer> mods = getModList(loader);
		
		String modName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, modID);
		BLModContainer container = new BLModContainer(modName, modID);
		modMap.put(modID, container);
		mods.add(container);
	}
	
	/**
	 * Get mod map from Fabric loader with reflections.
	 * If fails it will return empty map.
	 * @param loader {@link FabricLoader} loader instance.
	 * @return {@link Map} of {@link String} key and {@link ModContainer} value.
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, ModContainer> getModMap(FabricLoader loader) {
		try {
			Field field = loader.getClass().getDeclaredField("modMap");
			field.setAccessible(true);
			return (Map<String, ModContainer>) field.get(loader);
		}
		catch (IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
			return Maps.newHashMap();
		}
	}
	
	/**
	 * Get mod list from Fabric loader with reflections.
	 * If fails it will return empty list.
	 * @param loader {@link FabricLoader} loader instance.
	 * @return {@link List} of {@link ModContainer}
	 */
	@SuppressWarnings("unchecked")
	private static List<ModContainer> getModList(FabricLoader loader) {
		try {
			Field field = loader.getClass().getDeclaredField("mods");
			field.setAccessible(true);
			return (List<ModContainer>) field.get(loader);
		}
		catch (IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
			return Collections.EMPTY_LIST;
		}
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
}
