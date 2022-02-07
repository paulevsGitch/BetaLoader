package modloadermp;

import modloader.BaseMod;
import net.minecraft.client.gui.screen.ScreenBase;

public abstract class BaseModMp extends BaseMod {
	public final int getId() {
		return this.toString().hashCode();
	}
	
	public void ModsLoaded() {
		ModLoaderMp.Init();
	}
	
	public void HandlePacket(final Packet230ModLoader packet) {}
	
	public void HandleTileEntityPacket(final int i, final int j, final int k, final int l, final int[] ai, final float[] af, final String[] as) {}
	
	public ScreenBase HandleGUI(final int invType) {
		return null;
	}
}
