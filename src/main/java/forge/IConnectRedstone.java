package forge;

import net.minecraft.level.BlockView;

public interface IConnectRedstone {
    boolean canConnectRedstone(final BlockView blockView, final int i, final int j, final int k, final int l);
}
