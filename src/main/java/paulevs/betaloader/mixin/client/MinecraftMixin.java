package paulevs.betaloader.mixin.client;

import modloader.ModLoader;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulevs.betaloader.utilities.ModsStorage;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "init", at = @At(
		value = "INVOKE_STRING",
		target = "Lnet/minecraft/client/Minecraft;printOpenGLError(Ljava/lang/String;)V",
		args = "ldc=Post startup")
	)
	private void modloader_onInit(CallbackInfo info) {
		if (ModsStorage.loadJavassist()) {
			ModsStorage.process();
			ModLoader.onMinecraftInit();
		}
		else {
			System.out.println("Abort mod loading process");
		}
	}
}
