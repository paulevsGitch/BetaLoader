package paulevs.betaloader.mixin.common;

import net.minecraft.block.BlockBase;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Living;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemBase;
import net.minecraft.item.ItemInstance;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import paulevs.betaloader.mixininterfaces.BLItemBase;
import paulevs.betaloader.mixininterfaces.BLPlayerBase;

@Mixin(ItemBase.class)
public class ItemBaseMixin implements BLItemBase {
	@Override
	public float getStrVsBlock(ItemInstance item, BlockBase block, int meta) {
		return this.getStrengthOnBlock(item, block);
	}
	
	@Shadow
	public float getStrengthOnBlock(ItemInstance item, BlockBase tile) {
		return 1.0F;
	}
}
