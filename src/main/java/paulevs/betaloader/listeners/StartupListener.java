package paulevs.betaloader.listeners;

import modloader.ModLoader;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.client.event.texture.TextureRegisterEvent;
import net.modificationstation.stationapi.api.event.mod.InitEvent;
import net.modificationstation.stationapi.api.event.mod.PostInitEvent;
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
		//ModLoader.init();
	}
	
	@EventListener
	public void registerTextures(TextureRegisterEvent event) {
		ModLoader.init();
		BLTexturesManager.onTextureRegister();
	}
}
