package paulevs.betaloader.mixininterfaces;

import net.minecraft.block.BlockBase;
import net.minecraft.item.ItemInstance;

public interface BLItemBase {
	float getStrVsBlock(ItemInstance item, BlockBase block, int meta);
}
