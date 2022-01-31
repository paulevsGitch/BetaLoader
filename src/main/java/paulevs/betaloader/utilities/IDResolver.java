package paulevs.betaloader.utilities;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockBase;
import paulevs.betaloader.remapping.ModEntry;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class IDResolver {
	private static final Map<String, Map<Character, Character>> ID_MAP = Maps.newHashMap();
	private static final Set<Character> USED_IDS = Sets.newHashSet();
	private static final AtomicBoolean SAVE_CONFIG = new AtomicBoolean(false);
	
	/**
	 * Will get new ID for block if original ID is already in use.
	 * @param mod {@link ModEntry} mod where block is from. Vanilla should have null.
	 * @param id original block integer id.
	 * @return new block integer id.
	 */
	public static int getBlockID(ModEntry mod, final int id) {
		if (mod == null) {
			return id;
		}
		
		Map<Character, Character> modMap = ID_MAP.computeIfAbsent(mod.getModID(), i -> Maps.newHashMap());
		int newID = modMap.computeIfAbsent((char) id, i -> {
			if (BlockBase.BY_ID[id] == null) {
				return (char) id;
			}
			return getFreeBlockID();
		});
		
		if (BlockBase.BY_ID[newID] != null) {
			newID = getFreeBlockID();
			modMap.put((char) id, (char) newID);
			USED_IDS.add((char) newID);
			SAVE_CONFIG.set(true);
		}
		
		return newID;
	}
	
	/**
	 * Will load ID resolver config, for internal usage only.
	 */
	public static void loadConfig() {
		File configFolder = FabricLoader.getInstance().getConfigDir().toFile();
		File configFile = new File(configFolder, "betaloader/idresolver.json");
		Gson gson = new GsonBuilder().create();
		
		JsonObject config;
		if (!configFile.exists()) {
			SAVE_CONFIG.set(true);
			return;
		}
		try {
			FileReader reader = new FileReader(configFile);
			config = gson.fromJson(reader, JsonObject.class);
			reader.close();
		}
		catch (IOException e) {
			config = new JsonObject();
			e.printStackTrace();
		}
		if (!config.has("blocks")) {
			return;
		}
		JsonObject mods = config.get("blocks").getAsJsonObject();
		mods.entrySet().forEach(modEntry -> {
			Map<Character, Character> modMap = ID_MAP.computeIfAbsent(modEntry.getKey(), i -> Maps.newHashMap());
			JsonObject blocks = modEntry.getValue().getAsJsonObject();
			blocks.entrySet().forEach(blockEntry -> {
				String name = blockEntry.getKey();
				int index1 = name.lastIndexOf(':');
				int index2 = name.lastIndexOf(']');
				int fromID = Integer.parseInt(name.substring(index1 + 1, index2).trim());
				int toID = blockEntry.getValue().getAsInt();
				modMap.put((char) fromID, (char) toID);
				USED_IDS.add((char) toID);
			});
		});
	}
	
	/**
	 * Will save ID resolver config, for internal usage only.
	 */
	public static void saveConfig() {
		if (!SAVE_CONFIG.get()) {
			return;
		}
		
		File configFolder = FabricLoader.getInstance().getConfigDir().toFile();
		File configFile = new File(configFolder, "betaloader/idresolver.json");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		JsonObject config = new JsonObject();
		JsonObject mods = new JsonObject();
		config.add("blocks", mods);
		
		ID_MAP.entrySet().forEach(modKey -> {
			String modID = modKey.getKey();
			Map<Character, Character> modMap = modKey.getValue();
			JsonObject modEntry = new JsonObject();
			mods.add(modID, modEntry);
			modMap.forEach((idFrom, idTo) -> {
				BlockBase block = BlockBase.BY_ID[idTo];
				String key = block.getTranslationKey();
				key = key.substring(key.indexOf('.') + 1);
				key = String.format("%s [original: %d]", key, (int) idFrom);
				modEntry.add(key, new JsonPrimitive((int) idTo));
			});
		});
		
		String configLine = gson.toJson(config);
		FileUtil.writeTextFile(Lists.newArrayList(configLine), configFile);
	}
	
	/**
	 * Get first available block ID.
	 */
	private static char getFreeBlockID() {
		for (char id = 1; id < 256; id++) {
			if (BlockBase.BY_ID[id] == null && !USED_IDS.contains(id)) {
				return id;
			}
		}
		throw new RuntimeException("There is no more available IDs for blocks!");
	}
}
