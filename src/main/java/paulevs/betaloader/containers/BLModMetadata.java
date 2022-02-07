package paulevs.betaloader.containers;

import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.api.metadata.Person;
import net.fabricmc.loader.metadata.EntrypointMetadata;
import net.fabricmc.loader.metadata.LoaderModMetadata;
import net.fabricmc.loader.metadata.NestedJarEntry;
import net.fabricmc.loader.util.version.StringVersion;
import org.apache.logging.log4j.Logger;
import paulevs.betaloader.utilities.CacheStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BLModMetadata implements LoaderModMetadata {
	private final String iconPath;
	private final String modName;
	private final String modID;
	
	public BLModMetadata(String modName, String modID) {
		this.iconPath = CacheStorage.getCacheFile("icons").getAbsolutePath() + "/" + modID + ".png";
		this.modName = modName;
		this.modID = modID;
	}
	
	@Override
	public int getSchemaVersion() {
		return 0;
	}
	
	@Override
	public boolean loadsInEnvironment(EnvType type) {
		return true;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<NestedJarEntry> getJars() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<String> getMixinConfigs(EnvType type) {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public String getAccessWidener() {
		return null;
	}
	
	@Override
	public Map<String, String> getLanguageAdapterDefinitions() {
		return Maps.newHashMap();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<EntrypointMetadata> getEntrypoints(String type) {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Collection<String> getEntrypointKeys() {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public void emitFormatWarnings(Logger logger) {}
	
	@Override
	public String getType() {
		return "betaloader mod";
	}
	
	@Override
	public String getId() {
		return modID;
	}
	
	@Override
	public Version getVersion() {
		return new StringVersion("1.0.0");
	}
	
	@Override
	public ModEnvironment getEnvironment() {
		return ModEnvironment.UNIVERSAL;
	}
	
	@Override
	public Collection<ModDependency> getDepends() {
		return Collections.emptyList();
	}
	
	@Override
	public Collection<ModDependency> getRecommends() {
		return Collections.emptyList();
	}
	
	@Override
	public Collection<ModDependency> getSuggests() {
		return Collections.emptyList();
	}
	
	@Override
	public Collection<ModDependency> getConflicts() {
		return Collections.emptyList();
	}
	
	@Override
	public Collection<ModDependency> getBreaks() {
		return Collections.emptyList();
	}
	
	@Override
	public String getName() {
		return modName;
	}
	
	@Override
	public String getDescription() {
		return "Mod converted by BetaLoader";
	}
	
	@Override
	public Collection<Person> getAuthors() {
		return Collections.emptyList();
	}
	
	@Override
	public Collection<Person> getContributors() {
		return Collections.emptyList();
	}
	
	@Override
	public ContactInformation getContact() {
		return ContactInformation.EMPTY;
	}
	
	@Override
	public Collection<String> getLicense() {
		return Collections.emptyList();
	}
	
	@Override
	public Optional<String> getIconPath(int size) {
		return Optional.of(iconPath);//Optional.empty();
	}
	
	@Override
	public boolean containsCustomValue(String key) {
		return false;
	}
	
	@Override
	public CustomValue getCustomValue(String key) {
		return null;
	}
	
	@Override
	public Map<String, CustomValue> getCustomValues() {
		return Maps.newHashMap();
	}
}
