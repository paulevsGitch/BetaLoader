package modloader;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
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
import net.minecraft.entity.EntityRegistry;
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
import net.minecraft.level.dimension.DimensionFile;
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
import paulevs.betaloader.utilities.AccessHandler;
import paulevs.betaloader.utilities.BlockRendererData;
import paulevs.betaloader.utilities.ModsStorage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
	private static final List<TextureBinder> animList = new LinkedList();
	private static final Map<Integer, BaseMod> blockModels = new HashMap();
	private static final Map<Integer, Boolean> blockSpecialInv = new HashMap();
	private static final File cfgdir = new File(Minecraft.getGameDirectory(), "/config/");
	private static final File cfgfile = new File(cfgdir, "ModLoader.cfg");
	public static java.util.logging.Level cfgLoggingLevel = java.util.logging.Level.FINER;
	private static Map<String, Class<? extends EntityBase>> classMap = null;
	private static long clock = 0L;
	public static final boolean DEBUG = false;
	private static Field field_animList = null;
	private static Field field_armorList = null;
	private static Field field_blockList = null;
	private static Field field_modifiers = null;
	private static Field field_TileEntityRenderers = null;
	private static boolean hasInit = false;
	private static int highestEntityId = 3000;
	private static final Map<BaseMod, Boolean> inGameHooks = new HashMap();
	private static final Map<BaseMod, Boolean> inGUIHooks = new HashMap();
	private static Minecraft instance = null;
	private static int itemSpriteIndex = 0;
	private static int itemSpritesLeft = 0;
	private static final Map<BaseMod, Map<KeyBinding, boolean[]>> keyList = new HashMap();
	private static final File logfile = new File(Minecraft.getGameDirectory(), "ModLoader.txt");
	private static final Logger logger = Logger.getLogger("ModLoader");
	private static FileHandler logHandler = null;
	private static final LinkedList<BaseMod> modList = new LinkedList();
	private static int nextBlockModelID = 1000;
	private static final Map<Integer, Map<String, Integer>> overrides = new HashMap();
	public static final Properties props = new Properties();
	private static Biome[] standardBiomes;
	private static int terrainSpriteIndex = 0;
	private static int terrainSpritesLeft = 0;
	private static String texPack = null;
	private static boolean texturesAdded = false;
	private static final boolean[] usedItemSprites = new boolean[256];
	private static final boolean[] usedTerrainSprites = new boolean[256];
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
		catch (IllegalArgumentException v1) {
			logger.throwing("ModLoader", "AddAchievementDesc", v1);
			ThrowException(v1);
		}
		catch (SecurityException v1) {
			logger.throwing("ModLoader", "AddAchievementDesc", v1);
			ThrowException(v1);
		}
		catch (NoSuchFieldException v1) {
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
		if (!hasInit) {
			init();
			logger.fine("Initialized");
		}
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
		for (TextureBinder textureBinder : animList) {
			if ((textureBinder.renderMode == animation.renderMode) && (textureBinder.index == animation.index)) {
				animList.remove(animation);
				break;
			}
		}
		animList.add(animation);
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
			List<String> resultList = new ArrayList();
			resultList.addAll(stringList);
			if (!resultList.contains(armor)) {
				resultList.add(armor);
			}
			int armorIndex = resultList.indexOf(armor);
			field_armorList.set(null, resultList.toArray(new String[0]));
			return armorIndex;
		}
		catch (IllegalArgumentException exception) {
			logger.throwing("ModLoader", "AddArmor", exception);
			ThrowException("An impossible error has occured!", exception);
		}
		catch (IllegalAccessException exception) {
			logger.throwing("ModLoader", "AddArmor", exception);
			ThrowException("An impossible error has occured!", exception);
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
		catch (SecurityException exception) {
			logger.throwing("ModLoader", "AddLocalization", exception);
			ThrowException(exception);
		}
		catch (NoSuchFieldException exception) {
			logger.throwing("ModLoader", "AddLocalization", exception);
			ThrowException(exception);
		}
		if (properties != null) {
			properties.put(key, value);
		}
	}
	
	/**
	 * Add mod into loader. Changed from original. Original args were loader and mod class name.
	 * @param loader
	 * @param modFile
	 */
	private static void addMod(ClassLoader loader, File modFile) {
		ClassLoader sideLoader = ModsStorage.getSideLoader(modFile);
		String modID = ModsStorage.getModID(modFile);
		
		try {
			String modClassName = ModsStorage.getModClass(modFile);
			
			// TODO replace javassist with something else. Probably create proper mappings for tiny.
			Class<? extends BaseMod> modClass = (Class<? extends BaseMod>) sideLoader.loadClass(modClassName);
			Constructor<?> constructor = modClass.getDeclaredConstructor();
			
			if (!Modifier.isPublic(constructor.getModifiers()))  {
				ClassPool pool = ClassPool.getDefault();
				pool.insertClassPath(new ClassClassPath(modClass));
				CtClass cc = pool.get(modClassName);
				cc.getConstructor("()V").setModifiers(Modifier.PUBLIC);
				modClass = (Class<? extends BaseMod>) cc.toClass();
			}
			else {
				modClass = (Class<? extends BaseMod>) loader.loadClass(modClassName);
			}
			
			setupProperties(modClass);
			BaseMod mod = modClass.newInstance();
			
			if (mod != null) {
				modList.add(mod);
				String message = "Mod Loaded: \"" + mod + "\" from " + modID;
				logger.fine(message);
				System.out.println(message);
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
			int spriteIndex = getUniqueSpriteIndex(fileToOverride);
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
		int textureType;
		int atlasID;
		if (path.equals("/terrain.png")) {
			textureType = 0;
			atlasID = terrainSpritesLeft;
		}
		else if (path.equals("/gui/items.png")) {
			textureType = 1;
			atlasID = itemSpritesLeft;
		}
		else {
			return;
		}
		System.out.println("Overriding " + path + " with " + overlayPath + " @ " + index + ". " + atlasID + " left.");
		logger.finer("addOverride(" + path + "," + overlayPath + "," + index + "). " + atlasID + " left.");
		Map<String, Integer> overrideMap = overrides.get(Integer.valueOf(textureType));
		if (overrideMap == null) {
			overrideMap = new HashMap();
			overrides.put(Integer.valueOf(textureType), overrideMap);
		}
		overrideMap.put(overlayPath, Integer.valueOf(index));
	}
	
	/**
	 * Add recipe to crafting list.
	 * @param output
	 * @param ingredients
	 */
	public static void AddRecipe(ItemInstance output, Object... ingredients) {
		RecipeRegistryAccessor accessor = RecipeRegistryAccessor.class.cast(RecipeRegistry.getInstance());
		accessor.invokeAddShapedRecipe(output, ingredients);
	}
	
	/**
	 * Add recipe to crafting list.
	 * @param output
	 * @param ingredients
	 */
	public static void AddShapelessRecipe(ItemInstance output, Object... ingredients) {
		RecipeRegistryAccessor accessor = RecipeRegistryAccessor.class.cast(RecipeRegistry.getInstance());
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
		AddSpawn(entityClass, weightedProb, entityType, null);
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
		for (int i = 0; i < biomes.length; i++) {
			List<EntityEntry> spawnList = biomes[i].getSpawnList(entityType);
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
		AddSpawn(entityName, weightedProb, entityType, null);
	}
	
	/**
	 * Add entity to spawn list for selected biomes.
	 * @param entityName
	 * @param weightedProb
	 * @param entityType
	 * @param biomes
	 */
	public static void AddSpawn(String entityName, int weightedProb, EntityType entityType, Biome... biomes) {
		Class<? extends EntityBase> entityClass = classMap.get(entityName);
		if ((entityClass != null) && (Living.class.isAssignableFrom(entityClass))) {
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
		for (Iterator<BaseMod> iterator = modList.iterator(); (iterator.hasNext()) && (!dispensed); ) {
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
			instance = Minecraft.class.cast(FabricLoader.getInstance().getGameInstance());
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
	public static <T, E> T getPrivateValue(Class<? super E> instanceClass, E instance, int fieldindex) throws IllegalArgumentException, SecurityException, NoSuchFieldException {
		try {
			Field field = instanceClass.getDeclaredFields()[fieldindex];
			field.setAccessible(true);
			return (T) field.get(instance);
		}
		catch (IllegalAccessException exception) {
			logger.throwing("ModLoader", "getPrivateValue", exception);
			ThrowException("An impossible error has occured!", exception);
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
	public static <T, E> T getPrivateValue(Class<? super E> instanceClass, E instance, String fieldName) throws IllegalArgumentException, SecurityException, NoSuchFieldException {
		try {
			Field field = instanceClass.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(instance);
		}
		catch (IllegalAccessException exception) {
			logger.throwing("ModLoader", "getPrivateValue", exception);
			ThrowException("An impossible error has occured!", exception);
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
		blockModels.put(Integer.valueOf(index), mod);
		blockSpecialInv.put(Integer.valueOf(index), Boolean.valueOf(full3DItem));
		return index;
	}
	
	/**
	 * Gets next Entity ID to use.
	 * @return
	 */
	public static int getUniqueEntityId() {
		return highestEntityId++;
	}
	
	/**
	 * Gets next available index for this sprite map.
	 * @return
	 */
	private static int getUniqueItemSpriteIndex() {
		for (; itemSpriteIndex < usedItemSprites.length; itemSpriteIndex += 1) {
			if (!usedItemSprites[itemSpriteIndex]) {
				usedItemSprites[itemSpriteIndex] = true;
				itemSpritesLeft -= 1;
				return itemSpriteIndex++;
			}
		}
		Exception exception = new Exception("No more empty item sprite indices left!");
		logger.throwing("ModLoader", "getUniqueItemSpriteIndex", exception);
		ThrowException(exception);
		return 0;
	}
	
	/**
	 * Gets next available index for this sprite map.
	 * @param path
	 * @return
	 */
	public static int getUniqueSpriteIndex(String path) {
		if (path.equals("/gui/items.png")) {
			return getUniqueItemSpriteIndex();
		}
		if (path.equals("/terrain.png")) {
			return getUniqueTerrainSpriteIndex();
		}
		Exception v1 = new Exception("No registry for this texture: " + path);
		logger.throwing("ModLoader", "getUniqueItemSpriteIndex", v1);
		ThrowException(v1);
		return 0;
	}
	
	private static int getUniqueTerrainSpriteIndex() {
		for (; terrainSpriteIndex < usedTerrainSprites.length; terrainSpriteIndex += 1) {
			if (!usedTerrainSprites[terrainSpriteIndex]) {
				usedTerrainSprites[terrainSpriteIndex] = true;
				terrainSpritesLeft -= 1;
				return terrainSpriteIndex++;
			}
		}
		Exception exception = new Exception("No more empty terrain sprite indices left!");
		logger.throwing("ModLoader", "getUniqueItemSpriteIndex", exception);
		ThrowException(exception);
		return 0;
	}
	
	private static void init() {
		hasInit = true;
		
		String availableItems = "1111111111111111111111111111111111111101111111011111111111111001111111111111111111111111111011111111100110000011111110000000001111111001100000110000000100000011000000010000001100000000000000110000000000000000000000000000000000000000000000001100000000000000";
		String availableBlock = "1111111111111111111111111111110111111111111111111111110111111111111111111111000111111011111111111111001111111110111111111111100011111111000010001111011110000000111111000000000011111100000000001111000000000111111000000000001101000000000001111111111111000011";
		
		for (int i = 0; i < 256; i++) {
			usedItemSprites[i] = availableItems.charAt(i) == '1';
			if (!usedItemSprites[i]) {
				itemSpritesLeft += 1;
			}
			usedTerrainSprites[i] = availableBlock.charAt(i) == '1';
			if (!usedTerrainSprites[i]) {
				terrainSpritesLeft += 1;
			}
		}
		try {
			instance = getPrivateValue(Minecraft.class, null, 1);
			instance.gameRenderer = new EntityRendererProxy(instance);
			classMap = getPrivateValue(EntityRegistry.class, null, 0);
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
			
			List<Biome> biomeList = new LinkedList();
			for (int i = 0; i < biomeFields.length; i++) {
				Class<?> type = biomeFields[i].getType();
				if (((biomeFields[i].getModifiers() & 0x8) != 0) && (type.isAssignableFrom(Biome.class))) {
					Biome biome = (Biome) biomeFields[i].get(null);
					if ((!(biome instanceof Hell)) && (!(biome instanceof Sky))) {
						biomeList.add(biome);
					}
				}
			}
			standardBiomes = biomeList.toArray(new Biome[0]);
		}
		catch (SecurityException exception) {
			logger.throwing("ModLoader", "init", exception);
			ThrowException(exception);
			throw new RuntimeException(exception);
		}
		catch (NoSuchFieldException exception) {
			logger.throwing("ModLoader", "init", exception);
			ThrowException(exception);
			throw new RuntimeException(exception);
		}
		catch (IllegalArgumentException exception) {
			logger.throwing("ModLoader", "init", exception);
			ThrowException(exception);
			throw new RuntimeException(exception);
		}
		catch (IllegalAccessException exception) {
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
			logger.fine("ModLoader Beta 1.7.3 Initializing...");
			
			System.out.println("ModLoader Beta 1.7.3 Initializing...");
			File modFile = new File(ModLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			readFromModFolder();
			readFromClassPath(modFile);
			System.out.println("Done.");
			
			props.setProperty("loggingLevel", cfgLoggingLevel.getName());
			props.setProperty("grassFix", Boolean.toString(BlockRendererData.cfgGrassFix));
			for (BaseMod mod : modList) {
				mod.ModsLoaded();
				if (!props.containsKey(mod.getClass().getName())) {
					props.setProperty(mod.getClass().getName(), "on");
				}
			}
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
	}
	
	private static void initStats() {
		Map<Integer, Stat> map = StatsAccessor.getIdMap();
		for (int i = 0; i < BlockBase.BY_ID.length; i++) {
			if ((!map.containsKey(Integer.valueOf(16777216 + i))) && (BlockBase.BY_ID[i] != null) && (BlockBase.BY_ID[i].isStatEnabled())) {
				String v2 = TranslationStorage.getInstance().translate("stat.mineBlock", new Object[] { BlockBase.BY_ID[i].getTranslatedName() });
				Stats.mineBlock[i] = new StatEntity(16777216 + i, v2, i).register();
				Stats.blocksMinedList.add(Stats.mineBlock[i]);
			}
		}
		for (int v1 = 0; v1 < ItemBase.byId.length; v1++) {
			if ((!map.containsKey(Integer.valueOf(16908288 + v1))) && (ItemBase.byId[v1] != null)) {
				String v2 = TranslationStorage.getInstance().translate("stat.useItem", new Object[] {ItemBase.byId[v1].getTranslatedName()});
				Stats.useItem[v1] = new StatEntity(16908288 + v1, v2, v1).register();
				if (v1 >= BlockBase.BY_ID.length) {
					Stats.useStatList.add(Stats.useItem[v1]);
				}
			}
			if ((!map.containsKey(Integer.valueOf(16973824 + v1))) && (ItemBase.byId[v1] != null) && (ItemBase.byId[v1].hasDurability())) {
				String v2 = TranslationStorage.getInstance().translate("stat.breakItem", new Object[] {ItemBase.byId[v1].getTranslatedName()});
				Stats.breakItem[v1] = new StatEntity(16973824 + v1, v2, v1).register();
			}
		}
		HashSet<Integer> idMap = new HashSet();
		for (Object recipe : RecipeRegistry.getInstance().getRecipes()) {
			idMap.add(Integer.valueOf(((Recipe) recipe).getOutput().itemId));
		}
		for (Object item : SmeltingRecipeRegistry.getInstance().getRecipes().values()) {
			idMap.add(Integer.valueOf(((ItemInstance) item).itemId));
		}
		for (int id : idMap) {
			if ((!map.containsKey(Integer.valueOf(16842752 + id))) && (ItemBase.byId[id] != null)) {
				String v4 = TranslationStorage.getInstance().translate("stat.craftItem", new Object[] { ItemBase.byId[id].getTranslatedName() });
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
		if ((minecraft.currentScreen == null) && (gui != null)) {
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
		Class<?> modClass = null;
		try {
			modClass = Class.forName(modName);
		}
		catch (ClassNotFoundException v2) {
			return false;
		}
		if (modClass != null) {
			for (BaseMod mod : modList) {
				if (modClass.isInstance(mod)) {
					return true;
				}
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
		if (!ModLoader.hasInit) {
			init();
			ModLoader.logger.fine("Initialized");
		}
		if (ModLoader.texPack == null || minecraft.options.skin != ModLoader.texPack) {
			ModLoader.texturesAdded = false;
			ModLoader.texPack = minecraft.options.skin;
		}
		if (!ModLoader.texturesAdded && minecraft.textureManager != null) {
			RegisterAllTextureOverrides(minecraft.textureManager);
			ModLoader.texturesAdded = true;
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
		if (!hasInit) {
			init();
			logger.fine("Initialized");
		}
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
		if (!hasInit) {
			init();
			logger.fine("Initialized");
		}
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
	
	private static void readFromClassPath(File file) throws FileNotFoundException, IOException {
		// Temporary Disabled Code
		/*logger.finer("Adding mods from " + file.getCanonicalPath());
		ClassLoader loader = ModLoader.class.getClassLoader();
		if ((file.isFile()) && ((file.getName().endsWith(".jar")) || (file.getName().endsWith(".zip")))) {
			logger.finer("Zip found.");
			InputStream fileInputStream = new FileInputStream(file);
			ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
			while (true) {
				ZipEntry entry = zipInputStream.getNextEntry();
				if (entry == null) {
					break;
				}
				String name = entry.getName();
				if ((!entry.isDirectory()) && (name.startsWith("mod_")) && (name.endsWith(".class"))) {
					addMod(loader, name);
				}
			}
			fileInputStream.close();
		}
		else if (file.isDirectory()) {
			Package modPackage = ModLoader.class.getPackage();
			if (modPackage != null) {
				String name = modPackage.getName().replace('.', File.separatorChar);
				file = new File(file, name);
			}
			logger.finer("Directory found.");
			File[] files = file.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					String name = files[i].getName();
					if ((files[i].isFile()) && (name.startsWith("mod_")) && (name.endsWith(".class"))) {
						addMod(loader, name);
					}
				}
			}
		}*/
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
		
		ModsStorage.getMods().stream().map(file -> {
			try {
				return file.toURI().toURL();
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return null;
		}).filter(url -> url != null).forEach(url -> {
			try {
				method.invoke(loader, url);
			}
			catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		});
		
		Collection<File> files = ModsStorage.getMods();
		for (File modFile: files) {
			logger.finer("Adding mods from " + modFile.getCanonicalPath());
			logger.finer("Jar found.");
			addMod(loader, modFile);
		}
		
		// Old code
		/*for (int i = 0; i < files.length; i++) {
			File modFile = files[i];
			if ((modFile.isDirectory()) || ((modFile.isFile()) && ((modFile.getName().endsWith(".jar")) || (modFile.getName().endsWith(".zip"))))) {
				logger.finer("Adding mods from " + modFile.getCanonicalPath());
				if (modFile.isFile()) {
					logger.finer("Zip found.");
					InputStream fileInputStream = new FileInputStream(modFile);
					ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
					while (true) {
						ZipEntry entry = zipInputStream.getNextEntry();
						if (entry == null) {
							break;
						}
						String name = entry.getName();
						if ((!entry.isDirectory()) && (name.startsWith("mod_")) && (name.endsWith(".class"))) {
							addMod(loader, name);
						}
					}
					zipInputStream.close();
					fileInputStream.close();
				}
				else if (modFile.isDirectory()) {
					Package modPackage = ModLoader.class.getPackage();
					if (modPackage != null) {
						String name = modPackage.getName().replace('.', File.separatorChar);
						modFile = new File(modFile, name);
					}
					logger.finer("Directory found.");
					File[] modFiles = modFile.listFiles();
					if (modFiles != null) {
						for (int j = 0; j < modFiles.length; j++) {
							String name = modFiles[j].getName();
							if ((modFiles[j].isFile()) && (name.startsWith("mod_")) && (name.endsWith(".class"))) {
								addMod(loader, name);
							}
						}
					}
				}
			}
		}*/
	}
	
	/**
	 * Appends all mod key handlers to the given array and returns it.
	 * @param keyBindings
	 * @return
	 */
	public static KeyBinding[] RegisterAllKeys(KeyBinding[] keyBindings) {
		List<KeyBinding> v1 = new LinkedList();
		v1.addAll(Arrays.asList(keyBindings));
		for (Map<KeyBinding, boolean[]> v2 : keyList.values()) {
			v1.addAll(v2.keySet());
		}
		return v1.toArray(new KeyBinding[0]);
	}
	
	/**
	 * Processes all registered texture overrides.
	 * @param manager
	 */
	public static void RegisterAllTextureOverrides(TextureManager manager) {
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
	}
	
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
			List<BlockBase> v1 = (List) field_blockList.get(null);
			v1.add(block);
			
			Block v3;
			int blockID = block.id;
			if (itemclass != null) {
				v3 = itemclass.getConstructor(new Class[] {Integer.TYPE}).newInstance(new Object[] {Integer.valueOf(blockID - 256)});
			}
			else {
				v3 = new Block(blockID - 256);
			}
			if ((BlockBase.BY_ID[blockID] != null) && (ItemBase.byId[blockID] == null)) {
				ItemBase.byId[blockID] = v3;
			}
		}
		catch (IllegalArgumentException exception) {
			logger.throwing("ModLoader", "RegisterBlock", exception);
			ThrowException(exception);
		}
		catch (IllegalAccessException exception) {
			logger.throwing("ModLoader", "RegisterBlock", exception);
			ThrowException(exception);
		}
		catch (SecurityException exception) {
			logger.throwing("ModLoader", "RegisterBlock", exception);
			ThrowException(exception);
		}
		catch (InstantiationException exception) {
			logger.throwing("ModLoader", "RegisterBlock", exception);
			ThrowException(exception);
		}
		catch (InvocationTargetException exception) {
			logger.throwing("ModLoader", "RegisterBlock", exception);
			ThrowException(exception);
		}
		catch (NoSuchMethodException exception) {
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
			vkeyBindingMap = new HashMap();
		}
		vkeyBindingMap.put(keyBinding, new boolean[] {allowRepeat});
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
				Map<Class<? extends TileEntityBase>, TileEntityRenderer> v2 = (Map) field_TileEntityRenderers.get(dispatcher);
				v2.put(tileEntityClass, renderer);
				renderer.setRenderDispatcher(dispatcher);
			}
		}
		catch (IllegalArgumentException exception) {
			logger.throwing("ModLoader", "RegisterTileEntity", exception);
			ThrowException(exception);
		}
		catch (IllegalAccessException exception) {
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
		RemoveSpawn(entityClass, entityType, null);
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
		for (int i = 0; i < biomes.length; i++) {
			List<EntityEntry> spawnList = biomes[i].getSpawnList(entityType);
			if (spawnList != null) {
				for (Iterator<EntityEntry> iterator = spawnList.iterator(); iterator.hasNext(); ) {
					EntityEntry entry = iterator.next();
					if (entry.entryClass == entityClass) {
						iterator.remove();
					}
				}
			}
		}
	}
	
	/**
	 * Remove entity from spawn list for all biomes except Hell.
	 * @param entityName
	 * @param entityType
	 */
	public static void RemoveSpawn(String entityName, EntityType entityType) {
		RemoveSpawn(entityName, entityType, null);
	}
	
	/**
	 * Remove entity from spawn list for selected biomes.
	 * @param entityName
	 * @param entityType
	 * @param biomes
	 */
	public static void RemoveSpawn(String entityName, EntityType entityType, Biome... biomes) {
		Class<? extends EntityBase> entityClass = classMap.get(entityName);
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
		if (!blockSpecialInv.containsKey(Integer.valueOf(modelID))) {
			return modelID == 16;
		}
		return (blockSpecialInv.get(Integer.valueOf(modelID))).booleanValue();
	}
	
	/**
	 * Renders a block in inventory.
	 * @param blockRenderer
	 * @param block
	 * @param meta
	 * @param modelID
	 */
	public static void RenderInvBlock(BlockRenderer blockRenderer, BlockBase block, int meta, int modelID) {
		BaseMod mod = blockModels.get(Integer.valueOf(modelID));
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
		BaseMod mod = blockModels.get(Integer.valueOf(modelID));
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
			inGameHooks.put(mod, Boolean.valueOf(useClock));
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
			inGUIHooks.put(mod, Boolean.valueOf(useClock));
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
			ThrowException("An impossible error has occured!", exception);
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
			ThrowException("An impossible error has occured!", exception);
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
					builder1.append(String.format(",>=%.1f", new Object[] {Double.valueOf(annotation.min())}));
				}
				if (annotation.max() != Double.POSITIVE_INFINITY) {
					builder1.append(String.format(",<=%.1f", new Object[] {Double.valueOf(annotation.max())}));
				}
				StringBuilder builder2 = new StringBuilder();
				if (annotation.info().length() > 0) {
					builder2.append(" -- ");
					builder2.append(annotation.info());
				}
				builder.append(String.format("%s (%s:%s%s)%s\n", new Object[] {name, type.getName(), obj, builder1, builder2}));
				if (properties.containsKey(name)) {
					String property = properties.getProperty(name);
					
					Object propertyValue = null;
					if (type.isAssignableFrom(String.class)) {
						propertyValue = property;
					}
					else if (type.isAssignableFrom(Integer.TYPE)) {
						propertyValue = Integer.valueOf(Integer.parseInt(property));
					}
					else if (type.isAssignableFrom(Short.TYPE)) {
						propertyValue = Short.valueOf(Short.parseShort(property));
					}
					else if (type.isAssignableFrom(Byte.TYPE)) {
						propertyValue = Byte.valueOf(Byte.parseByte(property));
					}
					else if (type.isAssignableFrom(Boolean.TYPE)) {
						propertyValue = Boolean.valueOf(Boolean.parseBoolean(property));
					}
					else if (type.isAssignableFrom(Float.TYPE)) {
						propertyValue = Float.valueOf(Float.parseFloat(property));
					}
					else if (type.isAssignableFrom(Double.TYPE)) {
						propertyValue = Double.valueOf(Double.parseDouble(property));
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
		ThrowException("Exception occured in ModLoader", a1);
	}
	
	/**
	 * Custom method.
	 * Executed after Minecraft client is initiated
	 */
	public static void onMinecraftInit() {
		// TODO changes this with some access handler
		//if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
			//AccessHandler.changeClassAccess("net/minecraft/class_81.class");
			//AccessHandler.changeClassAccess(DimensionFile.class);
		//}
		//AccessHandler.changeAccess(null, null);
		if (!hasInit) {
			init();
			logger.fine("Initialized");
		}
	}
}