package forge;

import net.minecraft.entity.player.PlayerBase;
import net.minecraft.util.SleepStatus;

public interface ISleepHandler {
    SleepStatus sleepInBedAt(final PlayerBase playerBase, final int i, final int j, final int k);
}
