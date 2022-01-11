package paulevs.betaloader.utilities;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CacheStorage {
	private static final File CACHE_FOLDER = new File(FabricLoader.getInstance().getGameDir().toString(), "betaloader");
	private static final Map<String, File> CACHE_FILES = new HashMap<>();
	
	/**
	 * Get file or directory from cache folder by name.
	 * @param name {@link String} name of file or directory. Example: "config.json" (file), "mods" (folder).
	 * @return {@link File}
	 */
	public static File getCacheFile(String name) {
		return CACHE_FILES.computeIfAbsent(name, id -> new File(CACHE_FOLDER, name));
	}
}
