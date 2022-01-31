package paulevs.betaloader.mixin.common;

import net.minecraft.block.BlockBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockBase.class)
public class BlockBaseMixin {
	@Final
	@Shadow
	public static BlockBase[] BY_ID;
	
	@ModifyVariable(method = "Lnet/minecraft/block/BlockBase;<init>(ILnet/minecraft/block/material/Material;)V", at = @At("HEAD"), ordinal = 0)
	private static int betaloader_resolveIDs(int id) {
		if (BY_ID[id] != null) {
			System.out.print("Block id conflict resolving, from [" + id);
			while (id < 255 && BY_ID[id] != null) {
				id++;
			}
			System.out.println("] to [" + id + "]");
		}
		return id;
	}
}
