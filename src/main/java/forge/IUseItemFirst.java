package forge;

import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.ItemInstance;
import net.minecraft.level.Level;

public interface IUseItemFirst {
    boolean onItemUseFirst(final ItemInstance itemInstance, final PlayerBase playerBase, final Level level, final int i, final int j, final int k, final int l);
}
