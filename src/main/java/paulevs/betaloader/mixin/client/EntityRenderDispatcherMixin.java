package paulevs.betaloader.mixin.client;

import modloader.ModLoader;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.EntityBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@Shadow
	private Map<Class<? extends EntityBase>, EntityRenderer> renderers;
	
	@Inject(method = "<init>*", at = @At(value = "TAIL"))
	private void betaloader_onEntityRenderInit(CallbackInfo info) {
		Map<Class<? extends EntityBase>, EntityRenderer> rendererMap = new HashMap<>();
		ModLoader.AddAllRenderers(rendererMap);
		EntityRenderDispatcher dispatcher = EntityRenderDispatcher.class.cast(this);
		rendererMap.values().forEach(renderer -> renderer.setDispatcher(dispatcher));
		renderers.putAll(rendererMap);
	}
}
