package paulevs.betaloader.listeners;

import modloader.ModLoader;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.block.BlockBase;
import net.minecraft.item.ItemBase;
import net.modificationstation.stationapi.api.client.event.texture.TextureRegisterEvent;
import net.modificationstation.stationapi.api.event.mod.InitEvent;
import net.modificationstation.stationapi.api.registry.BlockRegistry;
import net.modificationstation.stationapi.api.registry.ItemRegistry;
import paulevs.betaloader.rendering.BLTexturesManager;
import paulevs.betaloader.utilities.FileDownloader;
import paulevs.betaloader.utilities.IDResolver;
import paulevs.betaloader.utilities.ModsStorage;
import paulevs.betaloader.utilities.RegistryUtil;

public class StartupListener {
	private boolean skipInit = false;
	
	/**
	 * Init event, will load configs, remap mods and init entries.
	 * @param event
	 */
	@EventListener
	public void onInit(InitEvent event) {
		if (!FileDownloader.load()) {
			System.out.println("Abort mod loading process");
			skipInit = true;
			return;
		}
		IDResolver.init();
		IDResolver.loadConfig();
		ModsStorage.process();
	}
	
	/**
	 * Will init modloader, load classes and make textures.
	 * @param event
	 */
	@EventListener(numPriority = 10000)
	public void registerTextures(TextureRegisterEvent event) {
		if (skipInit) {
			return;
		}
		ModLoader.init();
		BLTexturesManager.onTextureRegister();
		fixRegistryEntries();
		IDResolver.saveConfig();
		RegistryUtil.register();
	}
	
	private void fixRegistryEntries() {
		BlockRegistry.INSTANCE.forEach(entry -> {
			BlockBase block = entry.getValue();
			if (block.id < 256) {
				BlockBase fromID = BlockBase.BY_ID[block.id];
				if (fromID != null && block != fromID) {
					entry.setValue(fromID);
				}
			}
		});
		
		ItemRegistry.INSTANCE.forEach(entry -> {
			ItemBase item = entry.getValue();
			ItemBase fromID = ItemBase.byId[item.id];
			if (fromID != null && item != fromID) {
				entry.setValue(fromID);
			}
		});
	}
}
