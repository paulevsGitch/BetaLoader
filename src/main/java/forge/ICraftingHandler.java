package forge;

import net.minecraft.entity.player.PlayerBase;
import net.minecraft.inventory.InventoryBase;
import net.minecraft.item.ItemInstance;

public interface ICraftingHandler {
    void onTakenFromCrafting(final PlayerBase playerBase, final ItemInstance itemInstance, final InventoryBase inventoryBase);
}
