package forge;

import net.minecraft.entity.EntityBase;
import net.minecraft.level.Level;

public interface ISpecialResistance {
    float getSpecialExplosionResistance(final Level level, final int i, final int j, final int k, final double d, final double e, final double f, final EntityBase entityBase);
}
