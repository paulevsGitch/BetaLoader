package paulevs.betaloader.mixin.common;

import net.minecraft.block.BlockBase;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Living;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemInstance;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import paulevs.betaloader.mixininterfaces.BLItemBase;
import paulevs.betaloader.mixininterfaces.BLPlayerBase;

@Mixin(PlayerBase.class)
public class PlayerBaseMixin extends Living implements BLPlayerBase {
	@Shadow
	public PlayerInventory inventory;
	
	public PlayerBaseMixin(Level arg) {
		super(arg);
	}
	
	@Override
	public float getCurrentPlayerStrVsBlock(BlockBase block, int meta) {
		float strength = 1.0f;
		ItemInstance itemInstance5 = this.inventory.getHeldItem();
		if (itemInstance5 != null) {
			strength = BLItemBase.class.cast(itemInstance5.getType()).getStrVsBlock(itemInstance5, block, meta);
		}
		if (this.isInFluid(Material.WATER)) {
			strength /= 5.0f;
		}
		if (!this.onGround) {
			strength /= 5.0f;
		}
		return strength;
	}
}
