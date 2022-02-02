package forge;

import net.minecraft.level.Level;

public interface IBlockSecondaryProperties {
    boolean isBlockNormalCube(final Level level, final int i, final int j, final int k);
    
    boolean isBlockReplaceable(final Level level, final int i, final int j, final int k);
    
    boolean isBlockBurning(final Level level, final int i, final int j, final int k);
    
    boolean isAirBlock(final Level level, final int i, final int j, final int k);
}
