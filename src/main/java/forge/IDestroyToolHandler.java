package forge;

import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.ItemInstance;

public interface IDestroyToolHandler {
    void onDestroyCurrentItem(final PlayerBase playerBase, final ItemInstance itemInstance);
}
