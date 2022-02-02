package forge;

import net.minecraft.level.Level;

public interface IOverrideReplace {
    boolean canReplaceBlock(final Level level, final int i, final int j, final int k, final int l);
    
    boolean getReplacedSuccess();
}
