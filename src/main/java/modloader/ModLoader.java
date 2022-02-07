package modloader;

import java.util.Objects;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.achievement.Achievement;
import net.minecraft.block.BlockBase;
import net.minecraft.client.GameStartupError;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StatEntity;
import net.minecraft.client.TexturePackManager;
import net.minecraft.client.gui.screen.ScreenBase;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.TextureBinder;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PlayerRenderer;
import net.minecraft.client.render.entity.TileEntityRenderDispatcher;
import net.minecraft.client.render.tileentity.TileEntityRenderer;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.Session;
import net.minecraft.entity.EntityBase;
import net.minecraft.entity.EntityEntry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Living;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.Block;
import net.minecraft.item.ItemBase;
import net.minecraft.item.ItemInstance;
import net.minecraft.level.BlockView;
import net.minecraft.level.Level;
import net.minecraft.level.biome.Biome;
import net.minecraft.level.biome.Hell;
import net.minecraft.level.biome.Sky;
import net.minecraft.level.source.LevelSource;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeRegistry;
import net.minecraft.recipe.SmeltingRecipeRegistry;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.tileentity.TileEntityBase;
import net.modificationstation.stationapi.mixin.block.StatsAccessor;
import net.modificationstation.stationapi.mixin.recipe.RecipeRegistryAccessor;
import org.lwjgl.input.Keyboard;
import paulevs.betaloader.mixin.common.EntityRegistryAccessor;
import paulevs.betaloader.mixin.common.TileEntityBaseAccessor;
import paulevs.betaloader.remapping.ModEntry;
import paulevs.betaloader.remapping.RemapUtil;
import paulevs.betaloader.rendering.BLTexturesManager;
import paulevs.betaloader.rendering.BlockRendererData;
import paulevs.betaloader.utilities.JavassistUtil;
import paulevs.betaloader.utilities.ModsStorage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ModLoader {
	private static final Map<Integer, BaseMod> blockModels = new HashMap<>();
	private static final Map<Integer, Boolean> blockSpecialInv = new HashMap<>();
	private static final File cfgdir = new File(Minecraft.getGameDirectory(), "/config/");
	private static final File cfgfile = new File(cfgdir, "ModLoader.cfg");
	public static java.util.logging.Level cfgLoggingLevel = java.util.logging.Level.FINER;
	private static long clock = 0L;
	public static final boolean DEBUG = false;
	private static Field field_animList = null;
	private static Field field_armorList = null;
	private static Field field_blockList = null;
	private static Field field_modifiers = null;
	private static Field field_TileEntityRenderers = null;
	private static boolean hasInit = false;
	private static int highestEntityId = 3000;
	private static final Map<BaseMod, Boolean> inGameHooks = new HashMap<>();
	private static final Map<BaseMod, Boolean> inGUIHooks = new HashMap<>();
	private static Minecraft instance = null;
	private static final Map<BaseMod, Map<KeyBinding, boolean[]>> keyList = new HashMap<>();
	private static final File logfile = new File(Minecraft.getGameDirectory(), "ModLoader.txt");
	private static final Logger logger = Logger.getLogger("ModLoader");
	private static FileHandler logHandler = null;
	private static final LinkedList<BaseMod> modList = new LinkedList<>();
	private static int nextBlockModelID = 1000;
	private static final Map<Integer, Map<String, Integer>> overrides = new HashMap<>();
	public static final Properties props = new Properties();
	private static Biome[] standardBiomes;
	private static String texPack = null;
	public static final String VERSION = "ModLoader Beta 1.7.3";
	
	/**
	 * Used to give your achievement a readable name and description.
	 * @param achievement
	 * @param name
	 * @param description
	 */
	public static void AddAchievementDesc(Achievement achievement, String name, String description) {
		try {
			if (achievement.NAME.contains(".")) {
				String[] nameParts = achievement.NAME.split("\\.");
				if (nameParts.length == 2) {
					String modID = nameParts[1];
					AddLocalization("achievement." + modID, name);
					AddLocalization("achievement." + modID + ".desc", description);
					setPrivateValue(Stat.class, achievement, 1, TranslationStorage.getInstance().translate("achievement." + modID));
					setPrivateValue(Achievement.class, achievement, 3, TranslationStorage.getInstance().translate("achievement." + modID + ".desc"));
				}
				else {
					setPrivateValue(Stat.class, achievement, 1, name);
					setPrivateValue(Achievement.class, achievement, 3, description);
				}
			}
			else {
				setPrivateValue(Stat.class, achievement, 1, name);
				setPrivateValue(Achievement.class, achievement, 3, description);
			}
		}
		catch (IllegalArgumentException | NoSuchFieldException | SecurityException v1) {
			logger.throwing("ModLoader", "AddAchievementDesc", v1);
			ThrowException(v1);
		}
	}
	
	/**
	 * Used for adding new sources of fuel to the furnace.
	 * @param id
	 * @return
	 */
	public static int AddAllFuel(int id) {
		logger.finest("Finding fuel for " + id);
		int fuelID = 0;
		for (Iterator<BaseMod> iterator = modList.iterator(); (iterator.hasNext()) && (fuelID == 0); ) {
			fuelID = iterator.next().AddFuel(id);
		}
		if (fuelID != 0) {
			logger.finest("Returned " + fuelID);
		}
		return fuelID;
	}
	
	/**
	 * Used to add all mod entity renderers.
	 * @param rendererMap
	 */
	public static void AddAllRenderers(Map<Class<? extends EntityBase>, EntityRenderer> rendererMap) {
		for (BaseMod mod : modList) {
			mod.AddRenderer(rendererMap);
		}
	}
	
	/**
	 * Registers one animation instance.
	 * @param animation
	 */
	public static void addAnimation(TextureBinder animation) {
		logger.finest("Adding animation " + animation.toString());
		BLTexturesManager.addAnimation(animation);
	}
	
	/**
	 * Use this when you need the player to have new armor skin.
	 * @param armor
	 * @return
	 */
	public static int AddArmor(String armor) {
		try {
			String[] parts = (String[]) field_armorList.get(null);
			List<String> stringList = Arrays.asList(parts);
			List<String> resultList = new ArrayList<>(stringList);
			if (!resultList.contains(armor)) {
				resultList.add(armor);
			}
			int armorIndex = resultList.indexOf(armor);
			field_armorList.set(null, resultList.toArray(new String[0]));
			return armorIndex;
		}
		catch (IllegalArgumentException | IllegalAccessException exception) {
			logger.throwing("ModLoader", "AddArmor", exception);
			ThrowException("An impossible error has occurred!", exception);
		}
		return -1;
	}
	
	/**
	 * Method for adding raw strings to the translation table.
	 * @param key
	 * @param value
	 */
	public static void AddLocalization(String key, String value) {
		Properties properties = null;
		try {
			properties = getPrivateValue(TranslationStorage.class, TranslationStorage.getInstance(), 1);
		}
		catch (SecurityException | NoSuchFieldException exception) {
			logger.throwing("ModLoader", "AddLocalization", exception);
			ThrowException(exception);
		}
		if (properties != null) {
			properties.put(key, value);
		}
	}
	
	/**
	 * Add mod into loader. Changed from original. Original args were loader and mod class name.
	 * @param loader current {@link ClassLoader} to load mods.
	 * @param modEntry {@link ModEntry} for the mod.
	 */
	private static void addMod(ClassLoader loader, ModEntry modEntry) {
		ModsStorage.loadingMod = modEntry;
		String modID = modEntry.getModID().toString();
		File modFile = modEntry.getModConvertedFile();
		String modClassName = modEntry.getClasspath() + "." + modEntry.getMainClass();
		Class<? extends BaseMod> modClass = JavassistUtil.getModClass(loader, modFile, modClassName);
		try {
			BaseMod mod = modClass.newInstance();
			if (mod != null) {
				modList.add(mod);
				String message = "Mod Loaded: \"" + mod + "\" from " + modID;
				logger.fine(message);
				System.out.println(message);
				// TODO separate mod loading and texture loading
				mod.RegisterAnimation(getMinecraftInstance());
			}
		}
		catch (Exception exception) {
			logger.fine("Failed to load mod from \"" + modID + "\"");
			logger.fine("Reason: " + exception);
			System.out.println("Failed to load mod from \"" + modID + "\"");
			System.out.println("Reason: " + exception);
			logger.throwing("ModLoader", "addMod", exception);
			//ThrowException(exception);
		}
	}
	
	/**
	 * This method will allow adding name to item in inventory.
	 * @param instance
	 * @param name
	 */
	public static void AddName(Object instance, String name) {
		String translationKey = null;
		if ((instance instanceof ItemBase)) {
			ItemBase item = (ItemBase) instance;
			if (item.getTranslationKey() != null) {
				translationKey = item.getTranslationKey() + ".name";
			}
		}
		else if ((instance instanceof BlockBase)) {
			BlockBase block = (BlockBase) instance;
			if (block.getTranslationKey() != null) {
				translationKey = block.getTranslationKey() + ".name";
			}
		}
		else if ((instance instanceof ItemInstance)) {
			ItemInstance v2 = (ItemInstance) instance;
			if (v2.getTranslationKey() != null) {
				translationKey = v2.getTranslationKey() + ".name";
			}
		}
		else {
			Exception exception = new Exception(instance.getClass().getName() + " cannot have name attached to it!");
			logger.throwing("ModLoader", "AddName", exception);
			ThrowException(exception);
		}
		if (translationKey != null) {
			AddLocalization(translationKey, name);
		}
		else {
			Exception v2 = new Exception(instance + " is missing name tag!");
			logger.throwing("ModLoader", "AddName", v2);
			ThrowException(v2);
		}
	}
	
	/**
	 * Use this to add custom images for your items and blocks.
	 * @param fileToOverride
	 * @param fileToAdd
	 * @return
	 */
	public static int addOverride(String fileToOverride, String fileToAdd) {
		try {
			int spriteIndex = -1;
			if (fileToOverride.equals("/terrain.png")) {
				spriteIndex = BLTexturesManager.getBlockTexture(fileToAdd);
			}
			else if (fileToOverride.equals("/gui/items.png")) {
				spriteIndex = BLTexturesManager.getItemTexture(fileToAdd);
			}
			addOverride(fileToOverride, fileToAdd, spriteIndex);
			return spriteIndex;
		}
		catch (Throwable throwable) {
			logger.throwing("ModLoader", "addOverride", throwable);
			ThrowException(throwable);
			throw new RuntimeException(throwable);
		}
	}
	
	/**
	 * Registers one texture override to be done.
	 * @param path
	 * @param overlayPath
	 * @param index
	 */
	public static void addOverride(String path, String overlayPath, int index) {
		if (path.equals("/terrain.png")) {
			BLTexturesManager.setBlockTexture(index, overlayPath);
		}
		else if (path.equals("/gui/items.png")) {
			BLTexturesManager.setItemTexture(index, overlayPath);
		}
	}
	
	/**
	 * Gets next available index for this sprite map.
	 * @param path
	 * @return
	 */
	public static int getUniqueSpriteIndex(String path) {
		if (path.equals("/gui/items.png")) {
			return BLTexturesManager.pollItemTextureID();
		}
		if (path.equals("/terrain.png")) {
			return BLTexturesManager.pollBlockTextureID();
		}
		Exception v1 = new Exception("No registry for this texture: " + path);
		logger.throwing("ModLoader", "getUniqueItemSpriteIndex", v1);
		ThrowException(v1);
		return 0;
	}
	
	/**
	 * Add recipe to crafting list.
	 * @param output
	 * @param ingredients
	 */
	public static void AddRecipe(ItemInstance output, Object... ingredients) {
		RecipeRegistryAccessor accessor = (RecipeRegistryAccessor) RecipeRegistry.getInstance();
		accessor.invokeAddShapedRecipe(output, ingredients);
	}
	
	/**
	 * Add recipe to crafting list.
	 * @param output
	 * @param ingredients
	 */
	public static void AddShapelessRecipe(ItemInstance output, Object... ingredients) {
		RecipeRegistryAccessor accessor = (RecipeRegistryAccessor) RecipeRegistry.getInstance();
		accessor.invokeAddShapelessRecipe(output, ingredients);
	}
	
	/**
	 * Used to add smelting recipes to the furnace.
	 * @param input
	 * @param output
	 */
	public static void AddSmelting(int input, ItemInstance output) {
		SmeltingRecipeRegistry.getInstance().addSmeltingRecipe(input, output);
	}
	
	/**
	 * Add entity to spawn list for all biomes except Hell.
	 * @param entityClass
	 * @param weightedProb
	 * @param entityType
	 */
	public static void AddSpawn(Class<? extends Living> entityClass, int weightedProb, EntityType entityType) {
		AddSpawn(entityClass, weightedProb, entityType, (Biome[]) null);
	}
	
	/**
	 * Add entity to spawn list for selected biomes.
	 * @param entityClass
	 * @param weightedProb
	 * @param entityType
	 * @param biomes
	 */
	public static void AddSpawn(Class<? extends Living> entityClass, int weightedProb, EntityType entityType, Biome... biomes) {
		if (entityClass == null) {
			throw new IllegalArgumentException("entityClass cannot be null");
		}
		if (entityType == null) {
			throw new IllegalArgumentException("spawnList cannot be null");
		}
		if (biomes == null) {
			biomes = standardBiomes;
		}
		for (Biome biome : biomes) {
			@SuppressWarnings("unchecked")
			List<EntityEntry> spawnList = biome.getSpawnList(entityType);
			if (spawnList != null) {
				boolean hasEntry = false;
				for (EntityEntry entry : spawnList) {
					if (entry.entryClass == entityClass) {
						entry.rarity = weightedProb;
						hasEntry = true;
						break;
					}
				}
				if (!hasEntry) {
					spawnList.add(new EntityEntry(entityClass, weightedProb));
				}
			}
		}
	}
	
	/**
	 * Add entity to spawn list for all biomes except Hell.
	 * @param entityName
	 * @param weightedProb
	 * @param entityType
	 */
	public static void AddSpawn(String entityName, int weightedProb, EntityType entityType) {
		AddSpawn(entityName, weightedProb, entityType, (Biome[]) null);
	}
	
	/**
	 * Add entity to spawn list for selected biomes.
	 * @param entityName
	 * @param weightedProb
	 * @param entityType
	 * @param biomes
	 */
	@SuppressWarnings("unchecked")
	public static void AddSpawn(String entityName, int weightedProb, EntityType entityType, Biome... biomes) {
		Class<? extends EntityBase> entityClass = EntityRegistryAccessor.getStringToClassMap().get(entityName);
		if (entityClass != null && Living.class.isAssignableFrom(entityClass)) {
			AddSpawn((Class<? extends Living>) entityClass, weightedProb, entityType, biomes);
		}
	}
	
	/**
	 * Dispenses the entity associated with the selected item.
	 * @param level
	 * @param x
	 * @param y
	 * @param z
	 * @param xVel
	 * @param zVel
	 * @param itemInstance
	 * @return
	 */
	public static boolean DispenseEntity(Level level, double x, double y, double z, int xVel, int zVel, ItemInstance itemInstance) {
		boolean dispensed = false;
		for (Iterator<BaseMod> iterator = modList.iterator(); iterator.hasNext() && !dispensed; ) {
			dispensed = iterator.next().DispenseEntity(level, x, y, z, xVel, zVel, itemInstance);
		}
		return dispensed;
	}
	
	/**
	 * Use this method if you need a list of loaded mods.
	 * @return
	 */
	public static List<BaseMod> getLoadedMods() {
		return Collections.unmodifiableList(modList);
	}
	
	/**
	 * Use this to get a reference to the logger ModLoader uses.
	 * @return
	 */
	public static Logger getLogger() {
		return logger;
	}
	
	/**
	 * Use this method to get a reference to Minecraft instance.
	 * Method was changed from original.
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static Minecraft getMinecraftInstance() {
		if (instance == null) {
			instance = (Minecraft) FabricLoader.getInstance().getGameInstance();
		}
		return instance;
	}
	
	/**
	 * Used for getting value of private fields.
	 * @param instanceClass
	 * @param instance
	 * @param fieldindex
	 * @param <T>
	 * @param <E>
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("unchecked")
	public static <T, E> T getPrivateValue(Class<? super E> instanceClass, E instance, int fieldindex) throws IllegalArgumentException, SecurityException, NoSuchFieldException {
		try {
			Field field = instanceClass.getDeclaredFields()[fieldindex];
			field.setAccessible(true);
			return (T) field.get(instance);
		}
		catch (IllegalAccessException exception) {
			logger.throwing("ModLoader", "getPrivateValue", exception);
			ThrowException("An impossible error has occurred!", exception);
		}
		return null;
	}
	
	/**
	 * Used for getting value of private fields.
	 * @param instanceClass
	 * @param instance
	 * @param fieldName
	 * @param <T>
	 * @param <E>
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	@SuppressWarnings("unchecked")
	public static <T, E> T getPrivateValue(Class<? super E> instanceClass, E instance, String fieldName) throws IllegalArgumentException, SecurityException, NoSuchFieldException {
		fieldName = RemapUtil.getFieldName(instanceClass, fieldName);
		if (fieldName.isEmpty()) {
			return null;
		}
		try {
			Field field = instanceClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(instance);
		}
		catch (IllegalAccessException exception) {
			logger.throwing("ModLoader", "getPrivateValue", exception);
			ThrowException("An impossible error has occurred!", exception);
		}
		return null;
	}
	
	/**
	 * Assigns a model id for blocks to use for the given mod.
	 * @param mod
	 * @param full3DItem
	 * @return
	 */
	public static int getUniqueBlockModelID(BaseMod mod, boolean full3DItem) {
		int index = nextBlockModelID++;
		blockModels.put(index, mod);
		blockSpecialInv.put(index, full3DItem);
		return index;
	}
	
	/**
	 * Gets next Entity ID to use.
	 * @return
	 */
	public static int getUniqueEntityId() {
		return highestEntityId++;
	}
	
	public static void init() {
		if (hasInit) {
			return;
		}
		
		hasInit = true;
		
		try {
			instance = getMinecraftInstance();
			field_modifiers = Field.class.getDeclaredField("modifiers");
			field_modifiers.setAccessible(true);
			field_blockList = Session.class.getDeclaredFields()[0];
			field_blockList.setAccessible(true);
			field_TileEntityRenderers = TileEntityRenderDispatcher.class.getDeclaredFields()[0];
			field_TileEntityRenderers.setAccessible(true);
			field_armorList = PlayerRenderer.class.getDeclaredFields()[3];
			field_modifiers.setInt(field_armorList, field_armorList.getModifiers() & 0xFFFFFFEF);
			field_armorList.setAccessible(true);
			field_animList = TextureManager.class.getDeclaredFields()[6];
			field_animList.setAccessible(true);
			
			Field[] biomeFields = Biome.class.getDeclaredFields();
			
			List<Biome> biomeList = new LinkedList<>();
			for (Field biomeField : biomeFields) {
				Class<?> type = biomeField.getType();
				if (((biomeField.getModifiers() & 0x8) != 0) && (type.isAssignableFrom(Biome.class))) {
					Biome biome = (Biome) biomeField.get(null);
					if ((!(biome instanceof Hell)) && (!(biome instanceof Sky))) {
						biomeList.add(biome);
					}
				}
			}
			standardBiomes = biomeList.toArray(new Biome[0]);
			ModsStorage.loadingMod = null;
		}
		catch (SecurityException | IllegalAccessException | IllegalArgumentException | NoSuchFieldException exception) {
			logger.throwing("ModLoader", "init", exception);
			ThrowException(exception);
			throw new RuntimeException(exception);
		}
		try {
			loadConfig();
			if (props.containsKey("loggingLevel")) {
				cfgLoggingLevel = java.util.logging.Level.parse(props.getProperty("loggingLevel"));
			}
			if (props.containsKey("grassFix")) {
				BlockRendererData.cfgGrassFix = Boolean.parseBoolean(props.getProperty("grassFix"));
			}
			logger.setLevel(cfgLoggingLevel);
			if (((logfile.exists()) || (logfile.createNewFile())) && (logfile.canWrite()) && (logHandler == null)) {
				logHandler = new FileHandler(logfile.getPath());
				logHandler.setFormatter(new SimpleFormatter());
				logger.addHandler(logHandler);
			}
			logger.fine(VERSION + " Initializing...");
			
			System.out.println(VERSION + " Initializing...");
			readFromModFolder();
			System.out.println("Done.");
			
			props.setProperty("loggingLevel", cfgLoggingLevel.getName());
			props.setProperty("grassFix", Boolean.toString(BlockRendererData.cfgGrassFix));
			for (BaseMod mod : modList) {
				mod.ModsLoaded();
				if (!props.containsKey(mod.getClass().getName())) {
					props.setProperty(mod.getClass().getName(), "on");
				}
			}
			System.out.println("Instance: " + instance);
			instance.options.keyBindings = RegisterAllKeys(instance.options.keyBindings);
			instance.options.load();
			
			initStats();
			
			saveConfig();
		}
		catch (Throwable throwable) {
			logger.throwing("ModLoader", "init", throwable);
			ThrowException("ModLoader has failed to initialize.", throwable);
			if (logHandler != null) {
				logHandler.close();
			}
			throw new RuntimeException(throwable);
		}
		
		logger.fine("Initialized");
	}

	@SuppressWarnings("unchecked")
	private static void initStats() {
		Map<Integer, Stat> map = StatsAccessor.getIdMap();
		for (int i = 0; i < BlockBase.BY_ID.length; i++) {
			if ((!map.containsKey(16777216 + i)) && (BlockBase.BY_ID[i] != null) && (BlockBase.BY_ID[i].isStatEnabled())) {
				String v2 = TranslationStorage.getInstance().translate("stat.mineBlock", BlockBase.BY_ID[i].getTranslatedName());
				Stats.mineBlock[i] = new StatEntity(16777216 + i, v2, i).register();
				Stats.blocksMinedList.add(Stats.mineBlock[i]);
			}
		}
		for (int v1 = 0; v1 < ItemBase.byId.length; v1++) {
			if ((!map.containsKey(16908288 + v1)) && (ItemBase.byId[v1] != null)) {
				String v2 = TranslationStorage.getInstance().translate("stat.useItem", ItemBase.byId[v1].getTranslatedName());
				Stats.useItem[v1] = new StatEntity(16908288 + v1, v2, v1).register();
				if (v1 >= BlockBase.BY_ID.length) {
					Stats.useStatList.add(Stats.useItem[v1]);
				}
			}
			if ((!map.containsKey(16973824 + v1)) && (ItemBase.byId[v1] != null) && (ItemBase.byId[v1].hasDurability())) {
				String v2 = TranslationStorage.getInstance().translate("stat.breakItem", ItemBase.byId[v1].getTranslatedName());
				Stats.breakItem[v1] = new StatEntity(16973824 + v1, v2, v1).register();
			}
		}
		HashSet<Integer> idMap = new HashSet<>();
		for (Object recipe : RecipeRegistry.getInstance().getRecipes()) {
			idMap.add(((Recipe) recipe).getOutput().itemId);
		}
		for (Object item : SmeltingRecipeRegistry.getInstance().getRecipes().values()) {
			idMap.add(((ItemInstance) item).itemId);
		}
		for (int id : idMap) {
			if ((!map.containsKey(16842752 + id)) && (ItemBase.byId[id] != null)) {
				String v4 = TranslationStorage.getInstance().translate("stat.craftItem", ItemBase.byId[id].getTranslatedName());
				Stats.timesCrafted[id] = new StatEntity(16842752 + id, v4, id).register();
			}
		}
	}
	
	/**
	 * Use this method to check if GUI is opened for the player.
	 * @param gui
	 * @return
	 */
	public static boolean isGUIOpen(Class<? extends ScreenBase> gui) {
		Minecraft minecraft = getMinecraftInstance();
		if (gui == null) {
			return minecraft.currentScreen == null;
		}
		if (minecraft.currentScreen == null) {
			return false;
		}
		return gui.isInstance(minecraft.currentScreen);
	}
	
	/**
	 * Checks if a mod is loaded.
	 * @param modName
	 * @return
	 */
	public static boolean isModLoaded(String modName) {
		Class<?> modClass;
		try {
			modClass = Class.forName(modName);
		}
		catch (ClassNotFoundException v2) {
			return false;
		}
		for (BaseMod mod : modList) {
			if (modClass.isInstance(mod)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Reads the config file and stores the contents in props.
	 * @throws IOException
	 */
	public static void loadConfig() throws IOException {
		cfgdir.mkdir();
		if ((!cfgfile.exists()) && (!cfgfile.createNewFile())) {
			return;
		}
		if (cfgfile.canRead()) {
			InputStream v1 = new FileInputStream(cfgfile);
			props.load(v1);
			v1.close();
		}
	}
	
	/**
	 * Loads an image from a file in the jar into a BufferedImage.
	 * @param textureManager
	 * @param path
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage loadImage(TextureManager textureManager, String path) throws Exception {
		TexturePackManager manager = getPrivateValue(TextureManager.class, textureManager, 11);
		InputStream stream = manager.texturePack.getResourceAsStream(path);
		if (stream == null) {
			throw new Exception("Image not found: " + path);
		}
		BufferedImage image = ImageIO.read(stream);
		if (image == null) {
			throw new Exception("Image corrupted: " + path);
		}
		return image;
	}
	
	/**
	 * Is called when an item is picked up from the world.
	 * @param a1
	 * @param a2
	 */
	public static void OnItemPickup(PlayerBase a1, ItemInstance a2) {
		for (BaseMod v1 : modList) {
			v1.OnItemPickup(a1, a2);
		}
	}
	
	/**
	 * This method is called every tick while minecraft is running.
	 * @param minecraft
	 */
	public static void OnTick(Minecraft minecraft) {
		if (ModLoader.texPack == null || minecraft.options.skin != ModLoader.texPack) {
			ModLoader.texPack = minecraft.options.skin;
		}
		long time = 0L;
		if (minecraft.level != null) {
			time = minecraft.level.getLevelTime();
			final Iterator<Entry<BaseMod, Boolean>> iterator = ModLoader.inGameHooks.entrySet().iterator();
			while (iterator.hasNext()) {
				final Entry<BaseMod, Boolean> entry = iterator.next();
				if (ModLoader.clock == time && entry.getValue()) {
					continue;
				}
				if (entry.getKey().OnTickInGame(minecraft)) {
					continue;
				}
				iterator.remove();
			}
		}
		if (minecraft.currentScreen != null) {
			final Iterator<Entry<BaseMod, Boolean>> iterator = ModLoader.inGUIHooks.entrySet().iterator();
			while (iterator.hasNext()) {
				final Entry<BaseMod, Boolean> entry = iterator.next();
				if (ModLoader.clock == time && (entry.getValue() & minecraft.level != null)) {
					continue;
				}
				if (entry.getKey().OnTickInGUI(minecraft, minecraft.currentScreen)) {
					continue;
				}
				iterator.remove();
			}
		}
		if (ModLoader.clock != time) {
			for (final Entry<BaseMod, Map<KeyBinding, boolean[]>> keySet : ModLoader.keyList.entrySet()) {
				for (final Entry<KeyBinding, boolean[]> entry : keySet.getValue().entrySet()) {
					final boolean isDown = Keyboard.isKeyDown(entry.getKey().key);
					final boolean[] value = entry.getValue();
					final boolean compare = value[1];
					value[1] = isDown;
					if (isDown) {
						if (compare && !value[0]) {
							continue;
						}
						keySet.getKey().KeyboardEvent(entry.getKey());
					}
				}
			}
		}
		ModLoader.clock = time;
	}
	
	/**
	 * Opens GUI for use with mods.
	 * @param playerBase
	 * @param screenBase
	 */
	public static void OpenGUI(PlayerBase playerBase, ScreenBase screenBase) {
		Minecraft minecraft = getMinecraftInstance();
		if (minecraft.player != playerBase) {
			return;
		}
		if (screenBase != null) {
			minecraft.openScreen(screenBase);
		}
	}
	
	/**
	 * Used for generating new blocks in the world.
	 * @param levelSource
	 * @param chunkX
	 * @param chunkZ
	 * @param level
	 */
	public static void PopulateChunk(LevelSource levelSource, int chunkX, int chunkZ, Level level) {
		Random random = new Random(level.getSeed());
		long offsetX = random.nextLong() / 2L * 2L + 1L;
		long offsetZ = random.nextLong() / 2L * 2L + 1L;
		random.setSeed(chunkX * offsetX + chunkZ * offsetZ ^ level.getSeed());
		for (BaseMod mod : modList) {
			if (levelSource.toString().equals("RandomLevelSource")) {
				mod.GenerateSurface(level, random, chunkX << 4, chunkZ << 4);
			}
			else if (levelSource.toString().equals("HellRandomLevelSource")) {
				mod.GenerateNether(level, random, chunkX << 4, chunkZ << 4);
			}
		}
	}
	
	/**
	 * Load mods from folder. Changed from original. Original had folder (File) argument.
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	private static void readFromModFolder() throws IOException, IllegalArgumentException, SecurityException, NoSuchMethodException {
		ClassLoader loader = Minecraft.class.getClassLoader();
		Method method = loader.getClass().getDeclaredMethod("addURL", URL.class);
		method.setAccessible(true);
		
		ModsStorage.getMods().stream().map(entry -> {
			try {
				return entry.getModConvertedFile().toURI().toURL();
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return null;
		}).filter(Objects::nonNull).forEach(url -> {
			try {
				method.invoke(loader, url);
			}
			catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		});
		
		List<ModEntry> files = ModsStorage.getMods();
		for (ModEntry entry: files) {
			logger.finer("Adding mods from " + entry.getModOriginalFile().getCanonicalPath());
			logger.finer("Jar found.");
			addMod(loader, entry);
		}
	}
	
	/**
	 * Appends all mod key handlers to the given array and returns it.
	 * @param keyBindings
	 * @return
	 */
	public static KeyBinding[] RegisterAllKeys(KeyBinding[] keyBindings) {
		List<KeyBinding> v1 = new LinkedList<>(Arrays.asList(keyBindings));
		for (Map<KeyBinding, boolean[]> v2 : keyList.values()) {
			v1.addAll(v2.keySet());
		}
		return v1.toArray(new KeyBinding[0]);
	}
	
	/**
	 * Processes all registered texture overrides.
	 * @param manager
	 */
	/*public static void RegisterAllTextureOverrides(TextureManager manager) {
		animList.clear();
		Minecraft minecraft = getMinecraftInstance();
		for (final BaseMod mod : modList) {
			mod.RegisterAnimation(minecraft);
		}
		for (TextureBinder textureBinder : animList) {
			manager.addTextureBinder(textureBinder);
		}
		for (final Map.Entry<Integer, Map<String, Integer>> override : overrides.entrySet()) {
			for (final Map.Entry<String, Integer> entry : override.getValue().entrySet()) {
				final String key = entry.getKey();
				final int textureIndex = entry.getValue();
				final int textureSize = override.getKey();
				try {
					final BufferedImage image = loadImage(manager, key);
					final TextureBinder textureStatic = new ModTextureStatic(textureIndex, textureSize, image);
					manager.addTextureBinder(textureStatic);
				}
				catch (Exception exception) {
					ModLoader.logger.throwing("ModLoader", "RegisterAllTextureOverrides", exception);
					ThrowException(exception);
					throw new RuntimeException(exception);
				}
			}
		}
	}*/
	
	/**
	 * Adds block to list of blocks the player can use.
	 * @param a1
	 */
	public static void RegisterBlock(BlockBase a1) {
		RegisterBlock(a1, null);
	}
	
	/**
	 * Adds block to list of blocks the player can use.
	 * @param block
	 * @param itemclass
	 */
	public static void RegisterBlock(BlockBase block, Class<? extends Block> itemclass) {
		try {
			if (block == null) {
				throw new IllegalArgumentException("block parameter cannot be null.");
			}
			@SuppressWarnings("unchecked")
			List<BlockBase> v1 = (List<BlockBase>) field_blockList.get(null);
			v1.add(block);
			
			Block item;
			int blockID = block.id;
			if (itemclass != null) {
				item = itemclass.getConstructor(Integer.TYPE).newInstance(blockID - 256);
			}
			else {
				item = new Block(blockID - 256);
			}
			if ((BlockBase.BY_ID[blockID] != null) && (ItemBase.byId[blockID] == null)) {
				ItemBase.byId[blockID] = item;
			}
			//Identifier id = Identifier.of("betaloader:" + block.getTranslationKey());
			//BlockRegistry.INSTANCE.register(id, block);
			//ItemRegistry.INSTANCE.register(id, item);
		}
		catch (IllegalArgumentException | NoSuchMethodException | InvocationTargetException | InstantiationException | SecurityException | IllegalAccessException exception) {
			logger.throwing("ModLoader", "RegisterBlock", exception);
			ThrowException(exception);
		}
	}
	
	/**
	 * Registers an entity ID.
	 * @param entityClass
	 * @param entityName
	 * @param id
	 */
	public static void RegisterEntityID(Class<? extends EntityBase> entityClass, String entityName, int id) {
		try {
			EntityRegistryAccessor.callRegister(entityClass, entityName, id);
		}
		catch (IllegalArgumentException exception) {
			logger.throwing("ModLoader", "RegisterEntityID", exception);
			ThrowException(exception);
		}
	}
	
	/**
	 * Use this to add an assignable key to the options menu.
	 * @param mod
	 * @param keyBinding
	 * @param allowRepeat
	 */
	public static void RegisterKey(BaseMod mod, KeyBinding keyBinding, boolean allowRepeat) {
		Map<KeyBinding, boolean[]> vkeyBindingMap = keyList.get(mod);
		if (vkeyBindingMap == null) {
			vkeyBindingMap = new HashMap<>();
		}
		vkeyBindingMap.put(keyBinding, new boolean[] {allowRepeat, false});
		keyList.put(mod, vkeyBindingMap);
	}
	
	/**
	 * Registers a tile entity.
	 * @param tileEntityClass
	 * @param id
	 */
	public static void RegisterTileEntity(Class<? extends TileEntityBase> tileEntityClass, String id) {
		RegisterTileEntity(tileEntityClass, id, null);
	}
	
	/**
	 * Registers a tile entity.
	 * @param tileEntityClass
	 * @param id
	 * @param renderer
	 */
	public static void RegisterTileEntity(Class<? extends TileEntityBase> tileEntityClass, String id, TileEntityRenderer renderer) {
		try {
			TileEntityBaseAccessor.callRegister(tileEntityClass, id);
			if (renderer != null) {
				TileEntityRenderDispatcher dispatcher = TileEntityRenderDispatcher.INSTANCE;
				@SuppressWarnings("unchecked")
				Map<Class<? extends TileEntityBase>, TileEntityRenderer> v2 = (Map<Class<? extends TileEntityBase>, TileEntityRenderer>) field_TileEntityRenderers.get(dispatcher);
				v2.put(tileEntityClass, renderer);
				renderer.setRenderDispatcher(dispatcher);
			}
		}
		catch (IllegalArgumentException | IllegalAccessException exception) {
			logger.throwing("ModLoader", "RegisterTileEntity", exception);
			ThrowException(exception);
		}
	}
	
	/**
	 * Remove entity from spawn list for all biomes except Hell.
	 * @param entityClass
	 * @param entityType
	 */
	public static void RemoveSpawn(Class<? extends Living> entityClass, EntityType entityType) {
		RemoveSpawn(entityClass, entityType, (Biome[]) null);
	}
	
	/**
	 * Remove entity from spawn list for selected biomes.
	 * @param entityClass
	 * @param entityType
	 * @param biomes
	 */
	public static void RemoveSpawn(Class<? extends Living> entityClass, EntityType entityType, Biome... biomes) {
		if (entityClass == null) {
			throw new IllegalArgumentException("entityClass cannot be null");
		}
		if (entityType == null) {
			throw new IllegalArgumentException("spawnList cannot be null");
		}
		if (biomes == null) {
			biomes = standardBiomes;
		}
		for (Biome biome : biomes) {
			@SuppressWarnings("unchecked")
			List<EntityEntry> spawnList = biome.getSpawnList(entityType);
			if (spawnList != null) {
				spawnList.removeIf(entry -> entry.entryClass == entityClass);
			}
		}
	}
	
	/**
	 * Remove entity from spawn list for all biomes except Hell.
	 * @param entityName
	 * @param entityType
	 */
	public static void RemoveSpawn(String entityName, EntityType entityType) {
		RemoveSpawn(entityName, entityType, (Biome[]) null);
	}
	
	/**
	 * Remove entity from spawn list for selected biomes.
	 * @param entityName
	 * @param entityType
	 * @param biomes
	 */
	@SuppressWarnings("unchecked")
	public static void RemoveSpawn(String entityName, EntityType entityType, Biome... biomes) {
		Class<? extends EntityBase> entityClass = EntityRegistryAccessor.getStringToClassMap().get(entityName);
		if ((entityClass != null) && (Living.class.isAssignableFrom(entityClass))) {
			RemoveSpawn((Class<? extends Living>) entityClass, entityType, biomes);
		}
	}
	
	/**
	 * Determines how the block should be rendered.
	 * @param modelID
	 * @return
	 */
	public static boolean RenderBlockIsItemFull3D(int modelID) {
		if (!blockSpecialInv.containsKey(modelID)) {
			return modelID == 16;
		}
		return blockSpecialInv.get(modelID);
	}
	
	/**
	 * Renders a block in inventory.
	 * @param blockRenderer
	 * @param block
	 * @param meta
	 * @param modelID
	 */
	public static void RenderInvBlock(BlockRenderer blockRenderer, BlockBase block, int meta, int modelID) {
		BaseMod mod = blockModels.get(modelID);
		if (mod == null) {
			return;
		}
		mod.RenderInvBlock(blockRenderer, block, meta, modelID);
	}
	
	/**
	 * Renders a block in the world.
	 * @param blockRenderer
	 * @param view
	 * @param x
	 * @param y
	 * @param z
	 * @param block
	 * @param modelID
	 * @return
	 */
	public static boolean RenderWorldBlock(BlockRenderer blockRenderer, BlockView view, int x, int y, int z, BlockBase block, int modelID) {
		BaseMod mod = blockModels.get(modelID);
		if (mod == null) {
			return false;
		}
		return mod.RenderWorldBlock(blockRenderer, view, x, y, z, block, modelID);
	}
	
	/**
	 * Saves props to the config file.
	 * @throws IOException
	 */
	public static void saveConfig() throws IOException {
		cfgdir.mkdir();
		if ((!cfgfile.exists()) && (!cfgfile.createNewFile())) {
			return;
		}
		if (cfgfile.canWrite()) {
			OutputStream v1 = new FileOutputStream(cfgfile);
			props.store(v1, "ModLoader Config");
			v1.close();
		}
	}
	
	/**
	 * Enable or disable BaseMod.OnTickInGame(net.minecraft.client.Minecraft)
	 * @param mod
	 * @param enable
	 * @param useClock
	 */
	public static void SetInGameHook(BaseMod mod, boolean enable, boolean useClock) {
		if (enable) {
			inGameHooks.put(mod, useClock);
		}
		else {
			inGameHooks.remove(mod);
		}
	}
	
	/**
	 * Enable or disable BaseMod.OnTickInGUI(net.minecraft.client.Minecraft, da)
	 * @param mod
	 * @param enable
	 * @param useClock
	 */
	public static void SetInGUIHook(BaseMod mod, boolean enable, boolean useClock) {
		if (enable) {
			inGUIHooks.put(mod, useClock);
		}
		else {
			inGUIHooks.remove(mod);
		}
	}
	
	/**
	 * Used for setting value of private fields.
	 * @param instanceClass
	 * @param instance
	 * @param fieldIndex
	 * @param value
	 * @param <T>
	 * @param <E>
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	public static <T, E> void setPrivateValue(Class<? super T> instanceClass, T instance, int fieldIndex, E value) throws IllegalArgumentException, SecurityException, NoSuchFieldException {
		try {
			Field field = instanceClass.getDeclaredFields()[fieldIndex];
			field.setAccessible(true);
			int modifier = field_modifiers.getInt(field);
			if ((modifier & 0x10) != 0) {
				field_modifiers.setInt(field, modifier & 0xFFFFFFEF);
			}
			field.set(instance, value);
		}
		catch (IllegalAccessException exception) {
			logger.throwing("ModLoader", "setPrivateValue", exception);
			ThrowException("An impossible error has occurred!", exception);
		}
	}
	
	/**
	 * Used for setting value of private fields.
	 * @param instanceClass
	 * @param instance
	 * @param fieldName
	 * @param value
	 * @param <T>
	 * @param <E>
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	public static <T, E> void setPrivateValue(Class<? super T> instanceClass, T instance, String fieldName, E value) throws IllegalArgumentException, SecurityException, NoSuchFieldException {
		try {
			Field field = instanceClass.getDeclaredField(fieldName);
			int modifier = field_modifiers.getInt(field);
			if ((modifier & 0x10) != 0) {
				field_modifiers.setInt(field, modifier & 0xFFFFFFEF);
			}
			field.setAccessible(true);
			field.set(instance, value);
		}
		catch (IllegalAccessException exception) {
			logger.throwing("ModLoader", "setPrivateValue", exception);
			ThrowException("An impossible error has occurred!", exception);
		}
	}
	
	private static void setupProperties(Class<? extends BaseMod> modClass) throws IllegalArgumentException, IllegalAccessException, IOException, SecurityException, NoSuchFieldException {
		Properties properties = new Properties();
		
		File configFile = new File(cfgdir, modClass.getName() + ".cfg");
		if ((configFile.exists()) && (configFile.canRead())) {
			properties.load(new FileInputStream(configFile));
		}
		StringBuilder builder = new StringBuilder();
		Field[] arrayOfField;
		int j = (arrayOfField = modClass.getFields()).length;
		for (int i = 0; i < j; i++) {
			Field field = arrayOfField[i];
			if (((field.getModifiers() & 0x8) != 0) && (field.isAnnotationPresent(MLProp.class))) {
				Class<?> type = field.getType();
				MLProp annotation = field.getAnnotation(MLProp.class);
				String name = annotation.name().length() == 0 ? field.getName() : annotation.name();
				Object obj = field.get(null);
				
				StringBuilder builder1 = new StringBuilder();
				if (annotation.min() != Double.NEGATIVE_INFINITY) {
					builder1.append(String.format(",>=%.1f", annotation.min()));
				}
				if (annotation.max() != Double.POSITIVE_INFINITY) {
					builder1.append(String.format(",<=%.1f", annotation.max()));
				}
				StringBuilder builder2 = new StringBuilder();
				if (annotation.info().length() > 0) {
					builder2.append(" -- ");
					builder2.append(annotation.info());
				}
				builder.append(String.format("%s (%s:%s%s)%s\n", name, type.getName(), obj, builder1, builder2));
				if (properties.containsKey(name)) {
					String property = properties.getProperty(name);
					
					Object propertyValue = null;
					if (type.isAssignableFrom(String.class)) {
						propertyValue = property;
					}
					else if (type.isAssignableFrom(Integer.TYPE)) {
						propertyValue = Integer.parseInt(property);
					}
					else if (type.isAssignableFrom(Short.TYPE)) {
						propertyValue = Short.parseShort(property);
					}
					else if (type.isAssignableFrom(Byte.TYPE)) {
						propertyValue = Byte.parseByte(property);
					}
					else if (type.isAssignableFrom(Boolean.TYPE)) {
						propertyValue = Boolean.parseBoolean(property);
					}
					else if (type.isAssignableFrom(Float.TYPE)) {
						propertyValue = Float.parseFloat(property);
					}
					else if (type.isAssignableFrom(Double.TYPE)) {
						propertyValue = Double.parseDouble(property);
					}
					if (propertyValue != null) {
						if ((propertyValue instanceof Number)) {
							double doubleValue = ((Number) propertyValue).doubleValue();
							if ((annotation.min() == Double.NEGATIVE_INFINITY) || (doubleValue >= annotation.min())) {
								if ((annotation.max() != Double.POSITIVE_INFINITY) && (doubleValue > annotation.max())) {}
							}
						}
						else {
							logger.finer(name + " set to " + propertyValue);
							if (!propertyValue.equals(obj)) {
								field.set(null, propertyValue);
							}
						}
					}
				}
				else {
					logger.finer(name + " not in config, using default: " + obj);
					properties.setProperty(name, obj.toString());
				}
			}
		}
		if ((!properties.isEmpty()) && ((configFile.exists()) || (configFile.createNewFile())) && (configFile.canWrite())) {
			properties.store(new FileOutputStream(configFile), builder.toString());
		}
	}
	
	/**
	 * Is called when an item is picked up from crafting result slot.
	 * @param playerBase
	 * @param itemInstance
	 */
	public static void TakenFromCrafting(PlayerBase playerBase, ItemInstance itemInstance) {
		for (BaseMod v1 : modList) {
			v1.TakenFromCrafting(playerBase, itemInstance);
		}
	}
	
	/**
	 * Is called when an item is picked up from furnace result slot.
	 * @param playerBase
	 * @param itemInstance
	 */
	public static void TakenFromFurnace(PlayerBase playerBase, ItemInstance itemInstance) {
		for (BaseMod v1 : modList) {
			v1.TakenFromFurnace(playerBase, itemInstance);
		}
	}
	
	/**
	 * Used for catching an error and generating an error report.
	 * @param message
	 * @param exception
	 */
	public static void ThrowException(String message, Throwable exception) {
		Minecraft minecraft = getMinecraftInstance();
		if (minecraft != null) {
			minecraft.showGameStartupError(new GameStartupError(message, exception));
		}
		else {
			throw new RuntimeException(exception);
		}
	}
	
	private static void ThrowException(Throwable a1) {
		ThrowException("Exception occurred in ModLoader", a1);
	}
}
