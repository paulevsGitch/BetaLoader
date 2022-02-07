package paulevs.betaloader.containers;

import net.fabricmc.loader.ModContainer;

import java.io.File;
import java.nio.file.Path;

public class BLModContainer extends ModContainer {
	public BLModContainer(String modName, String modID) {
		super(new BLModMetadata(modName, modID), null);
	}
	
	@Override
	public Path getPath(String file) {
		if (file.endsWith(".png")) {
			return new File(file).toPath();
		}
		return super.getPath(file);
	}
}
