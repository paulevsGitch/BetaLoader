package paulevs.betaloader.mixin.common;

import net.minecraft.entity.EntityRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRegistry.class)
public interface EntityRegistryAccessor {
	@Invoker
	static void callRegister(Class entityClass, String name, int i) {
		throw new AssertionError("@Invoker dummy body called");
	}
}
