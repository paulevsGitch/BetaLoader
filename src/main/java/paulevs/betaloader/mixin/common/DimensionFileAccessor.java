package paulevs.betaloader.mixin.common;

import net.minecraft.level.dimension.DimensionFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;

@Mixin(DimensionFile.class)
public interface DimensionFileAccessor {
	@Invoker
	File callGetParentFolder();
}
