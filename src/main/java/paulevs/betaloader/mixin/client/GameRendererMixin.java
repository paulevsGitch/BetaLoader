package paulevs.betaloader.mixin.client;

import modloader.ModLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.sortme.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@Shadow
	private Minecraft minecraft;
	
	@Inject(method = "method_1844", at = @At("TAIL"))
	private void betaloader_onRenderTick(float delta, CallbackInfo ci) {
		ModLoader.OnTick(minecraft);
	}
}
