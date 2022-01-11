package paulevs.betaloader.mixin.common;

import net.minecraft.tileentity.TileEntityBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TileEntityBase.class)
public interface TileEntityBaseAccessor {
	@Invoker
	static void callRegister(Class tileEntityClass, String tileEntityID) {
		throw new AssertionError("@Invoker dummy body called");
	}
}
