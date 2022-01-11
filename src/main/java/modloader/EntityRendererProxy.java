package modloader;

import net.minecraft.client.Minecraft;
import net.minecraft.sortme.GameRenderer;

public class EntityRendererProxy extends GameRenderer {
	private Minecraft game;
	
	public EntityRendererProxy(Minecraft minecraft) {
		super(minecraft);
		game = minecraft;
	}
	
	@Override
	public void method_1844(float delta) {
		super.method_1844(delta);
		ModLoader.OnTick(this.game);
	}
}
