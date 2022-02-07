package paulevs.betaloader.remapping;

import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.launch.common.FabricLauncher;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.loader.util.mappings.TinyRemapperMappingsHelper;
import net.fabricmc.mapping.reader.v2.TinyMetadata;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.FieldDef;
import net.fabricmc.mapping.tree.LocalVariableDef;
import net.fabricmc.mapping.tree.MethodDef;
import net.fabricmc.mapping.tree.ParameterDef;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import net.fabricmc.tinyremapper.IMappingProvider;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyRemapper.Builder;
import org.objectweb.asm.Type;
import paulevs.betaloader.utilities.CacheStorage;
import paulevs.betaloader.utilities.FileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RemapUtil {
	private static final Map<MapEntryType, Map<String, Map<String, String>>> METHOD_REMAP = new EnumMap<>(MapEntryType.class);
	private static final Map<String, Map<String, String>> FIELDS_CACHE = new HashMap<>();
	
	private static final File LOADER_MAPPINGS = CacheStorage.getCacheFile("loader_mappings.tiny");
	private static final File MAIN_MAPPINGS = CacheStorage.getCacheFile("main_mappings.tiny");
	private static final File MOD_MAPPINGS = CacheStorage.getCacheFile("mod_mappings.tiny");
	private static final File MINECRAFT = CacheStorage.getCacheFile("minecraft.jar");
	private static TinyTree mainTree;
	
	public static void addMapping(MapEntryType type, String targetClass, String intermediary, String name) {
		METHOD_REMAP.computeIfAbsent(type, i -> new HashMap<>()).computeIfAbsent(targetClass, i -> new HashMap<>()).put(intermediary, name);
	}
	
	/**
	 * Will remap all mods in list into specified folder.
	 * @param modsToRemap {@link List} of {@link File} to remap.
	 * @param remappedDir output directory {@link File}.
	 */
	public static void remap(List<ModEntry> modsToRemap, File remappedDir) {
		// DISABLED //
		// Handling mod access with custom accessors for protected methods.
		// This is not ideal solution, but looks like it works.
		// addMapping(MapEntryType.METHOD, "net/minecraft/class_81", "method_332", "callGetParentFolder");
		// addMapping(MapEntryType.FIELD, "net/minecraft/class_18", "field_220", "modloader_LevelProperties");
		
		initMainTree();
		makeLoaderMappings();
		
		final TinyTree loaderTree = makeTree(LOADER_MAPPINGS);
		final TinyTree mainTree = RemapUtil.mainTree;
		
		modsToRemap.forEach(entry -> {
			System.out.println("Remapping: " + entry.getModOriginalFile().getName());
			makeModMappings(entry.getClasspath().replace('.', '/'), entry.getModClasses());
			TinyRemapper remapper = makeRemapper(mainTree, loaderTree, makeTree(MOD_MAPPINGS));
			remapFile(remapper, entry.getModOriginalFile().toPath(), entry.getModConvertedFile().toPath());
		});
	}
	
	/**
	 * Get {@link ClassDef} from specified class name.
	 * @param name {@link String} class name.
	 * @return
	 */
	private static ClassDef getClass(String name) {
		initMainTree();
		FabricLauncher launcher = FabricLauncherBase.getLauncher();
		String namespace = launcher.getTargetNamespace();
		String searchName = name.replace('.', '/');
		Optional<ClassDef> optional = mainTree
			.getClasses()
			.stream()
			.parallel()
			.filter(classDef -> classDef.getName(namespace).equals(searchName))
			.findAny();
		return optional.isPresent() ? optional.get() : null;
	}
	
	/**
	 * Get field name from specified class. Will return null if there will be no available name.
	 * @param cl {@link ClassDef} to get field from.
	 * @param fieldName {@link String} field name.
	 * @return
	 */
	private static String getFieldName(ClassDef cl, String fieldName) {
		FabricLauncher launcher = FabricLauncherBase.getLauncher();
		String namespace = launcher.getTargetNamespace();
		Optional<FieldDef> optional = cl
			.getFields()
			.stream()
			.parallel()
			.filter(fieldDef -> fieldDef.getName("client").equals(fieldName))
			.findAny();
		return optional.isPresent() ? optional.get().getName(namespace) : null;
	}
	
	/**
	 * Get field name from specified class. Will return null if there will be no available name.
	 * @param cl {@link Class} to get field from.
	 * @param fieldName {@link String} field name.
	 * @return
	 */
	public static String getFieldName(Class cl, String fieldName) {
		final String className = cl.getName();
		return FIELDS_CACHE.computeIfAbsent(className, i -> new HashMap<>()).computeIfAbsent(fieldName, i -> {
			ClassDef def = getClass(className);
			return def != null ? getFieldName(def, fieldName) : "";
		});
	}
	
	private static void initMainTree() {
		if (mainTree == null) {
			makeBaseMappings();
			mainTree = makeTree(MAIN_MAPPINGS);
		}
	}
	
	/**
	 * Will remap file with specified remapper and store it into output.
	 * @param remapper {@link TinyRemapper} to remap with.
	 * @param input {@link Path} for the input file.
	 * @param output {@link Path} for the output file.
	 */
	private static void remapFile(TinyRemapper remapper, Path input, Path output) {
		try {
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
	}
	
	/**
	 * Will create base mappings if they are missing. Used internal mappings as a pattern and then modify lines
	 * using internal map.
	 */
	private static void makeBaseMappings() {
		if (!MAIN_MAPPINGS.exists()) {
			List<String> mappings = FileUtil.readTextSource("mappings/mappings.tiny");
			String[] parts = mappings.get(0).split("\t", -1);
			
			final int indexInter = getIndex("intermediary", parts);
			final int indexNamed = getIndex("named", parts);
			final int size = mappings.size();
			
			for (int i = 1; i < size; i++) {
				parts = mappings.get(i).split("\t", -1);
				if (modifyLine(parts, indexInter, indexNamed)) {
					mappings.set(i, toString(parts));
				}
			}
			
			FileUtil.writeTextFile(mappings, MAIN_MAPPINGS);
		}
	}
	
	/**
	 * Get index of specified string in array.
	 * @param value {@link String} to search.
	 * @param array to search in.
	 * @return
	 */
	private static int getIndex(String value, String[] array) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(value)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Will convert array to mapping-like string (with tab separator).
	 * @param line array of {@link String} that represents mappings line.
	 * @return
	 */
	private static String toString(String... line) {
		StringBuilder builder = new StringBuilder(line[0]);
		for (int j = 1; j < line.length; j++) {
			builder.append('\t');
			builder.append(line[j]);
		}
		return builder.toString();
	}
	
	/**
	 * Will modify mapping line with internal rules. Will return true on success and false on fail.
	 * @param line
	 * @param indexInter
	 * @param indexNamed
	 * @return
	 */
	private static boolean modifyLine(String[] line, int indexInter, int indexNamed) {
		MapEntryType type = MapEntryType.getType(line[0]);
		if (type == null) {
			return false;
		}
		Map<String, Map<String, String>> mapByClass = METHOD_REMAP.get(type);
		if (mapByClass == null) {
			return false;
		}
		Map<String, String> remap = mapByClass.get(line[1]);
		if (remap == null) {
			return false;
		}
		indexInter += type.offset;
		String replacement = remap.get(line[indexInter]);
		if (replacement == null) {
			return false;
		}
		line[indexInter] = replacement;
		if (indexNamed > -1) {
			line[indexNamed + type.offset] = replacement;
		}
		return true;
	}
	
	/**
	 * Will make mappings for ModLoader namespaces.
	 */
	private static void makeLoaderMappings() {
		if (!LOADER_MAPPINGS.exists()) {
			List<String> mappings = new ArrayList<>();
			mappings.add(toString("v1", "intermediary", "client", "server", "named"));
			
			mappings.add(makeLoaderLine("EntityRendererProxy", "modloader/EntityRendererProxy"));
			mappings.add(makeLoaderLine("ModTextureAnimation", "modloader/ModTextureAnimation"));
			mappings.add(makeLoaderLine("ModTextureStatic", "modloader/ModTextureStatic"));
			mappings.add(makeLoaderLine("ModLoader", "modloader/ModLoader"));
			mappings.add(makeLoaderLine("BaseMod", "modloader/BaseMod"));
			mappings.add(makeLoaderLine("MLProp", "modloader/MLProp"));
			
			mappings.add(makeLoaderLine("NetClientHandlerEntity", "modloadermp/NetClientHandlerEntity"));
			mappings.add(makeLoaderLine("Packet230ModLoader", "modloadermp/Packet230ModLoader"));
			mappings.add(makeLoaderLine("ModLoaderMp", "modloadermp/ModLoaderMp"));
			mappings.add(makeLoaderLine("ISpawnable", "modloadermp/ISpawnable"));
			mappings.add(makeLoaderLine("BaseModMp", "modloadermp/BaseModMp"));
			
			mappings.add(makeLoaderLine("net/minecraft/src/forge/IBlockSecondaryProperties", "forge/IBlockSecondaryProperties"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/BlockTextureParticles", "forge/BlockTextureParticles"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/IArmorTextureProvider", "forge/IArmorTextureProvider"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/MinecraftForgeClient", "forge/MinecraftForgeClient"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/IDestroyToolHandler", "forge/IDestroyToolHandler"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/ISpecialResistance", "forge/ISpecialResistance"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/IHighlightHandler", "forge/IHighlightHandler"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/ForgeHooksClient", "forge/ForgeHooksClient"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/IConnectRedstone", "forge/IConnectRedstone"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/ICraftingHandler", "forge/ICraftingHandler"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/IMultipassRender", "forge/IMultipassRender"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/IOverrideReplace", "forge/IOverrideReplace"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/ITextureProvider", "forge/ITextureProvider"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/ArmorProperties", "forge/ArmorProperties"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/IBucketHandler", "forge/IBucketHandler"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/MinecraftForge", "forge/MinecraftForge"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/Configuration", "forge/Configuration"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/ISleepHandler", "forge/ISleepHandler"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/ISpecialArmor", "forge/ISpecialArmor"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/IUseItemFirst", "forge/IUseItemFirst"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/ForgeHooks", "forge/ForgeHooks"));
			mappings.add(makeLoaderLine("net/minecraft/src/forge/Property", "forge/Property"));
			
			FileUtil.writeTextFile(mappings, LOADER_MAPPINGS);
		}
	}
	
	private static void makeModMappings(String classPath, List<String> modClasses) {
		List<String> mappings = new ArrayList<>();
		mappings.add(toString("v1", "intermediary", "client", "server", "named"));
		modClasses.forEach(cl -> mappings.add(makeLoaderLine(cl, classPath + "/" + cl)));
		FileUtil.writeTextFile(mappings, MOD_MAPPINGS);
	}
	
	private static String makeLoaderLine(String from, String to) {
		return toString("CLASS", to, from, from, to);
	}
	
	/**
	 * Will make tree for specified mappings file.
	 * @param file mappings {@link File} in tiny format.
	 * @return
	 */
	private static TinyTree makeTree(File file) {
		TinyTree tree = null;
		try {
			FileReader reader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(reader);
			tree = TinyMappingFactory.loadWithDetection(bufferedReader);
			tree = wrapTree(tree);
		}
		catch (IOException exception) {
			exception.printStackTrace();
		}
		return tree;
	}
	
	/**
	 * Will create a mapping provider for specified tree.
	 * @param tree
	 * @return
	 */
	private static IMappingProvider createProvider(TinyTree tree) {
		FabricLauncher launcher = FabricLauncherBase.getLauncher();
		return TinyRemapperMappingsHelper.create(tree, "client", launcher.getTargetNamespace());
	}
	
	/**
	 * Will create remapper with specified trees.
	 * @param trees
	 * @return
	 */
	private static TinyRemapper makeRemapper(TinyTree... trees) {
		Builder builder = TinyRemapper
			.newRemapper()
			.renameInvalidLocals(true)
			.ignoreFieldDesc(false)
			.propagatePrivate(true)
			.ignoreConflicts(true);
		
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			builder.fixPackageAccess(true);
		}
		
		for (TinyTree tree: trees) {
			builder.withMappings(createProvider(tree));
		}
		
		TinyRemapper remapper = builder.build();
		remapper.readClassPath(MINECRAFT.toPath());
		return remapper;
	}
	
	/**
	 * Function from Fabric Loader (was with private access). Will wrap mapping tree into another one.
	 * @param mappings
	 * @return
	 */
	private static TinyTree wrapTree(TinyTree mappings) {
		return new TinyTree() {
			final String primaryNamespace = getMetadata().getNamespaces().get(0); //If the namespaces are empty we shouldn't exist
			
			private Optional<String> remap(String name, String namespace) {
				return Optional.ofNullable(getDefaultNamespaceClassMap().get(name)).map(mapping -> mapping.getRawName(namespace)).map(
					Strings::emptyToNull);
			}
			
			String remapDesc(String desc, String namespace) {
				Type type = Type.getType(desc);
				
				switch (type.getSort()) {
					case Type.ARRAY: {

						return desc.substring(0, type.getDimensions()) + remapDesc(type.getElementType().getDescriptor(), namespace);
					}
					
					case Type.OBJECT:
						return remap(type.getInternalName(), namespace).map(name -> 'L' + name + ';').orElse(desc);
					
					case Type.METHOD: {
						if ("()V".equals(desc)) return desc;
						
						StringBuilder stringBuilder = new StringBuilder("(");
						for (Type argumentType : type.getArgumentTypes()) {
							stringBuilder.append(remapDesc(argumentType.getDescriptor(), namespace));
						}
						
						Type returnType = type.getReturnType();
						if (returnType == Type.VOID_TYPE) {
							stringBuilder.append(")V");
						} else {
							stringBuilder.append(')').append(remapDesc(returnType.getDescriptor(), namespace));
						}
						
						return stringBuilder.toString();
					}
					
					default:
						return desc;
				}
			}
			
			private ClassDef wrap(ClassDef mapping) {
				return new ClassDef() {
					private final boolean common = getMetadata().getNamespaces().stream().skip(1).map(this::getRawName).allMatch(Strings::isNullOrEmpty);
					
					@Override
					public String getRawName(String namespace) {
						try {
							return mapping.getRawName(common ? primaryNamespace : namespace);
						} catch (ArrayIndexOutOfBoundsException e) {
							return ""; //No name for the namespace
						}
					}
					
					@Override
					public String getName(String namespace) {
						return mapping.getName(namespace);
					}
					
					@Override
					public String getComment() {
						return mapping.getComment();
					}
					
					@Override
					public Collection<MethodDef> getMethods() {
						return Collections2.transform(mapping.getMethods(), method -> new MethodDef() {
							@Override
							public String getRawName(String namespace) {
								try {
									return method.getRawName(namespace);
								} catch (ArrayIndexOutOfBoundsException e) {
									return ""; //No name for the namespace
								}
							}
							
							@Override
							public String getName(String namespace) {
								return method.getName(namespace);
							}
							
							@Override
							public String getComment() {
								return method.getComment();
							}
							
							@Override
							public String getDescriptor(String namespace) {
								String desc = method.getDescriptor(primaryNamespace);
								return primaryNamespace.equals(namespace) ? desc : remapDesc(desc, namespace);
							}
							
							@Override
							public Collection<ParameterDef> getParameters() {
								return method.getParameters();
							}
							
							@Override
							public Collection<LocalVariableDef> getLocalVariables() {
								return method.getLocalVariables();
							}
						});
					}
					
					@Override
					public Collection<FieldDef> getFields() {
						return Collections2.transform(mapping.getFields(), field -> new FieldDef() {
							@Override
							public String getRawName(String namespace) {
								try {
									return field.getRawName(namespace);
								} catch (ArrayIndexOutOfBoundsException e) {
									return ""; //No name for the namespace
								}
							}
							
							@Override
							public String getName(String namespace) {
								return field.getName(namespace);
							}
							
							@Override
							public String getComment() {
								return field.getComment();
							}
							
							@Override
							public String getDescriptor(String namespace) {
								String desc = field.getDescriptor(primaryNamespace);
								return primaryNamespace.equals(namespace) ? desc : remapDesc(desc, namespace);
							}
						});
					}
				};
			}
			
			@Override
			public TinyMetadata getMetadata() {
				return mappings.getMetadata();
			}
			
			@Override
			public Map<String, ClassDef> getDefaultNamespaceClassMap() {
				return Maps.transformValues(mappings.getDefaultNamespaceClassMap(), this::wrap);
			}
			
			@Override
			public Collection<ClassDef> getClasses() {
				return Collections2.transform(mappings.getClasses(), this::wrap);
			}
		};
	}
}
