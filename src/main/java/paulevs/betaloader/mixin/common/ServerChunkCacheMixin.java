package paulevs.betaloader.mixin.common;

import modloader.ModLoader;
import net.minecraft.level.Level;
import net.minecraft.level.chunk.ServerChunkCache;
import net.minecraft.level.source.LevelSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkCache.class)
public class ServerChunkCacheMixin {
	@Shadow
	private LevelSource levelSource;
	
	@Shadow
	private Level level;
	
	@Inject(method = "decorate", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/level/chunk/Chunk;method_885()V",
		shift = Shift.BEFORE
	))
	public void betaloader_decorateChunk(LevelSource levelSource, int chunkX, int chunkZ, CallbackInfo info) {
		ModLoader.PopulateChunk(this.levelSource, chunkX, chunkZ, level);
	}
}
