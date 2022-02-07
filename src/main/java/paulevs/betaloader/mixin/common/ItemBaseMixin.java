package paulevs.betaloader.mixin.common;

import net.minecraft.block.BlockBase;
import net.minecraft.item.ItemBase;
import net.minecraft.item.ItemInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import paulevs.betaloader.mixininterfaces.BLItemBase;
import paulevs.betaloader.utilities.IDResolver;
import paulevs.betaloader.utilities.ModsStorage;

@Mixin(ItemBase.class)
public class ItemBaseMixin implements BLItemBase {
	@ModifyVariable(method = "<init>(I)V", at = @At("HEAD"), ordinal = 0)
	private static int betaloader_resolveItemID(int id) {
		return IDResolver.getItemID(ModsStorage.loadingMod, id);
	}
	
	@Override
	public float getStrVsBlock(ItemInstance item, BlockBase block, int meta) {
		return this.getStrengthOnBlock(item, block);
	}
	
	@Shadow
	public float getStrengthOnBlock(ItemInstance item, BlockBase tile) {
		return 1.0F;
	}
}
