package paulevs.betaloader.mixin.common;

import net.minecraft.block.BlockBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import paulevs.betaloader.mixininterfaces.BLBlockBase;
import paulevs.betaloader.utilities.IDResolver;
import paulevs.betaloader.utilities.ModsStorage;

@Mixin(BlockBase.class)
public class BlockBaseMixin implements BLBlockBase {
	@Shadow
	protected float hardness;
	
	@ModifyVariable(method = "Lnet/minecraft/block/BlockBase;<init>(ILnet/minecraft/block/material/Material;)V", at = @At("HEAD"), ordinal = 0)
	private static int betaloader_resolveBlockID(int id) {
		return IDResolver.getBlockID(ModsStorage.loadingMod, id);
	}
	
	@Override
	public float getHardness(int meta) {
		return hardness;
	}
}
