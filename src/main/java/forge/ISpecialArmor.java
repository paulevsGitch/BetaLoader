package forge;

import net.minecraft.entity.player.PlayerBase;

public interface ISpecialArmor {
    ArmorProperties getProperties(final PlayerBase playerBase, final int i, final int j);
}
