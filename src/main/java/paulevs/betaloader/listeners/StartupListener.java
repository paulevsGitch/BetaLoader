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
import paulevs.betaloader.utilities.ModsStorage;

public class StartupListener {
	@EventListener
	public void init(InitEvent event) {
		if (!FileDownloader.load()) {
			System.out.println("Abort mod loading process");
			return;
		}
		ModsStorage.process();
	}
	
	@EventListener
	public void registerTextures(TextureRegisterEvent event) {
		ModLoader.init();
		BLTexturesManager.onTextureRegister();
		fixRegistryEntries();
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
