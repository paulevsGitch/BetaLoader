package forge;

import net.minecraft.item.ItemInstance;
import net.minecraft.level.Level;

public interface IBucketHandler {
    ItemInstance fillCustomBucket(final Level level, final int i, final int j, final int k);
}
