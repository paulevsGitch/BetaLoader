package paulevs.betaloader.mixin.common;

import net.minecraft.level.Level;
import net.minecraft.level.LevelProperties;
import net.minecraft.level.dimension.Dimension;
import net.minecraft.level.dimension.DimensionData;
import net.minecraft.level.dimension.DimensionFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(Level.class)
public class LevelMixin {
	public LevelProperties modloader_LevelProperties;
	
	@Shadow
	protected LevelProperties properties;
	
	@Inject(method = "Lnet/minecraft/level/Level;<init>(Lnet/minecraft/level/dimension/DimensionData;Ljava/lang/String;Lnet/minecraft/level/dimension/Dimension;J)V", at = @At(value = "TAIL"))
	private void betaloader_onInit1(DimensionData dimensionData, String name, Dimension dimension, long seed, CallbackInfo info) {
		modloader_LevelProperties = properties;
	}
	
	@Inject(method = "Lnet/minecraft/level/Level;<init>(Lnet/minecraft/level/Level;Lnet/minecraft/level/dimension/Dimension;)V", at = @At(value = "TAIL"))
	private void betaloader_onInit2(Level level, Dimension dimension, CallbackInfo info) {
		modloader_LevelProperties = properties;
	}
	
	@Inject(method = "Lnet/minecraft/level/Level;<init>(Lnet/minecraft/level/dimension/DimensionData;Ljava/lang/String;JLnet/minecraft/level/dimension/Dimension;)V", at = @At(value = "TAIL"))
	private void betaloader_onInit3(DimensionData arg, String name, long seed, Dimension dimension, CallbackInfo info) {
		modloader_LevelProperties = properties;
	}
}
