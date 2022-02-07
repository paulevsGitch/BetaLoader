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
import net.minecraft.item.ItemBase;
import net.modificationstation.stationapi.api.registry.BlockRegistry;
import net.modificationstation.stationapi.api.registry.ItemRegistry;
import net.modificationstation.stationapi.api.registry.ModID;
import paulevs.betaloader.remapping.ModEntry;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class IDResolver {
	private static final Map<String, Map<Character, Character>> BLOCK_ID_MAP = Maps.newHashMap();
	private static final Map<String, Map<Character, Character>> ITEM_ID_MAP = Maps.newHashMap();
	private static final Set<Character> USED_BLOCK_IDS = Sets.newHashSet();
	private static final Set<Character> USED_ITEM_IDS = Sets.newHashSet();
	private static final AtomicBoolean SAVE_CONFIG = new AtomicBoolean(false);
	private static final Set<Integer> VANILLA_BLOCKS = new HashSet<>();
	private static final Set<Integer> VANILLA_ITEMS = new HashSet<>();
	private static boolean resolveBlocks = true;
	private static boolean resolveItems = true;
	
	public static void init() {
		ModID mcID = ModID.of("minecraft");
		
		BlockRegistry.INSTANCE.forEach((id, block) -> {
			if (id.modID.equals(mcID)) {
				VANILLA_BLOCKS.add(block.id);
			}
		});
		
		ItemRegistry.INSTANCE.forEach((id, item) -> {
			if (id.modID.equals(mcID)) {
				VANILLA_ITEMS.add(item.id);
			}
		});
	}
	
	/**
	 * Will get new ID for block if original ID is already in use.
	 * @param mod {@link ModEntry} mod where block is from. Vanilla should have null.
	 * @param id original block integer id.
	 * @return new block integer id.
	 */
	public static int getBlockID(ModEntry mod, final int id) {
		if (mod == null || !resolveBlocks) {
			return id;
		}
		
		boolean isVanillaBlock = false;
		if (VANILLA_BLOCKS.contains(id)) {
			System.out.println("Mod " + mod + " overriding vanilla block: " + id + " (" + BlockBase.BY_ID[id] + ")");
			isVanillaBlock = true;
		}
		
		if (isVanillaBlock) {
			return id;
		}
		
		Map<Character, Character> modMap = BLOCK_ID_MAP.computeIfAbsent(mod.getModID().toString(), i -> Maps.newHashMap());
		int newID = modMap.computeIfAbsent((char) id, i -> {
			if (BlockBase.BY_ID[id] == null) {
				return (char) id;
			}
			SAVE_CONFIG.set(true);
			return getFreeBlockID();
		});
		
		if (BlockBase.BY_ID[newID] != null) {
			newID = getFreeBlockID();
			modMap.put((char) id, (char) newID);
			SAVE_CONFIG.set(true);
		}
		RegistryUtil.addBlock(mod.getModID(), (char) newID);
		
		USED_BLOCK_IDS.add((char) newID);
		return newID;
	}
	
	/**
	 * Will get new ID for item if original ID is already in use.
	 * @param mod {@link ModEntry} mod where block is from. Vanilla should have null.
	 * @param id original item integer id.
	 * @return new item integer id.
	 */
	public static int getItemID(ModEntry mod, final int id) {
		if (mod == null || !resolveItems) {
			return id;
		}
		
		final int realID = id + 256; // This part resolves vanilla code part with adding 256 to any item ID
		if (realID < 360) {
			return id;
		}
		
		boolean isVanillaItem = false;
		if (VANILLA_ITEMS.contains(realID)) {
			System.out.println("Mod " + mod + " overriding vanilla item: " + realID + " (" + ItemBase.byId[realID] + ")");
			isVanillaItem = true;
		}
		
		if (isVanillaItem) {
			return id;
		}
		
		Map<Character, Character> modMap = ITEM_ID_MAP.computeIfAbsent(mod.getModID().toString(), i -> Maps.newHashMap());
		int newID = modMap.computeIfAbsent((char) realID, i -> {
			if (ItemBase.byId[realID] == null) {
				return (char) realID;
			}
			SAVE_CONFIG.set(true);
			return getFreeItemID();
		});
		
		if (ItemBase.byId[newID] != null) {
			newID = getFreeItemID();
			modMap.put((char) realID, (char) newID);
			SAVE_CONFIG.set(true);
		}
		RegistryUtil.addItem(mod.getModID(), (char) newID);
		
		USED_ITEM_IDS.add((char) newID);
		return newID - 256;
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
		
		if (config.has("options")) {
			JsonObject options = config.get("options").getAsJsonObject();
			resolveBlocks = options.get("resolveBlocks").getAsBoolean();
			resolveItems = options.get("resolveItems").getAsBoolean();
		}
		
		loadBlockConfig(config);
		loadItemConfig(config);
	}
	
	/**
	 * Will load block entries from the config.
	 * @param config {@link JsonObject} config instance.
	 */
	private static void loadBlockConfig(JsonObject config) {
		if (!config.has("blocks") || !resolveBlocks) {
			return;
		}
		
		JsonObject mods = config.get("blocks").getAsJsonObject();
		mods.entrySet().forEach(modEntry -> {
			Map<Character, Character> modMap = BLOCK_ID_MAP.computeIfAbsent(modEntry.getKey(), i -> Maps.newHashMap());
			JsonObject blocks = modEntry.getValue().getAsJsonObject();
			blocks.entrySet().forEach(blockEntry -> {
				String name = blockEntry.getKey();
				int index1 = name.lastIndexOf(':');
				int index2 = name.lastIndexOf(']');
				int fromID = Integer.parseInt(name.substring(index1 + 1, index2).trim());
				int toID = blockEntry.getValue().getAsInt();
				modMap.put((char) fromID, (char) toID);
				USED_BLOCK_IDS.add((char) toID);
			});
		});
	}
	
	/**
	 * Will load item entries from the config.
	 * @param config {@link JsonObject} config instance.
	 */
	private static void loadItemConfig(JsonObject config) {
		if (!config.has("items") || !resolveItems) {
			return;
		}
		
		JsonObject mods = config.get("items").getAsJsonObject();
		mods.entrySet().forEach(modEntry -> {
			Map<Character, Character> modMap = ITEM_ID_MAP.computeIfAbsent(modEntry.getKey(), i -> Maps.newHashMap());
			JsonObject items = modEntry.getValue().getAsJsonObject();
			items.entrySet().forEach(itemEntry -> {
				String name = itemEntry.getKey();
				int index1 = name.lastIndexOf(':');
				int index2 = name.lastIndexOf(']');
				int fromID = Integer.parseInt(name.substring(index1 + 1, index2).trim());
				int toID = itemEntry.getValue().getAsInt();
				modMap.put((char) fromID, (char) toID);
				USED_ITEM_IDS.add((char) toID);
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
		saveBlockConfig(config);
		saveItemConfig(config);
		
		JsonObject options = new JsonObject();
		config.add("options", options);
		options.addProperty("resolveBlocks", true);
		options.addProperty("resolveItems", true);
		
		String configLine = gson.toJson(config);
		FileUtil.writeTextFile(Lists.newArrayList(configLine), configFile);
	}
	
	private static void saveBlockConfig(JsonObject config) {
		JsonObject mods = new JsonObject();
		config.add("blocks", mods);
		
		BLOCK_ID_MAP.entrySet().forEach(modKey -> {
			String modID = modKey.getKey();
			Map<Character, Character> modMap = modKey.getValue();
			JsonObject modEntry = new JsonObject();
			mods.add(modID, modEntry);
			modMap.forEach((idFrom, idTo) -> {
				BlockBase block = BlockBase.BY_ID[idTo];
				if (block != null) {
					String key = block.getTranslationKey();
					key = key.substring(key.indexOf('.') + 1);
					key = String.format("%s [original: %d]", key, (int) idFrom);
					modEntry.add(key, new JsonPrimitive((int) idTo));
				}
			});
		});
	}
	
	private static void saveItemConfig(JsonObject config) {
		JsonObject mods = new JsonObject();
		config.add("items", mods);
		
		ITEM_ID_MAP.entrySet().forEach(modKey -> {
			String modID = modKey.getKey();
			Map<Character, Character> modMap = modKey.getValue();
			JsonObject modEntry = new JsonObject();
			mods.add(modID, modEntry);
			modMap.forEach((idFrom, idTo) -> {
				ItemBase item = ItemBase.byId[idTo];
				String key = item.getTranslationKey();
				key = key.substring(key.indexOf('.') + 1);
				key = String.format("%s [original: %d]", key, (int) idFrom);
				modEntry.add(key, new JsonPrimitive((int) idTo));
			});
		});
	}
	
	/**
	 * Get first available block ID.
	 */
	private static char getFreeBlockID() {
		for (char id = 1; id < BlockBase.BY_ID.length; id++) {
			if (BlockBase.BY_ID[id] == null && !USED_BLOCK_IDS.contains(id)) {
				return id;
			}
		}
		throw new RuntimeException("There are no more available IDs for blocks!");
	}
	
	private static char getFreeItemID() {
		for (char id = 360; id < ItemBase.byId.length; id++) {
			if (ItemBase.byId[id] == null && !USED_ITEM_IDS.contains(id)) {
				return id;
			}
		}
		throw new RuntimeException("There are no more available IDs for items!");
	}
}
