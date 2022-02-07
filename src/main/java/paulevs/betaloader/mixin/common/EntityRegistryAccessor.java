package paulevs.betaloader.mixin.common;

import net.minecraft.entity.EntityBase;
import net.minecraft.entity.EntityRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(EntityRegistry.class)
public interface EntityRegistryAccessor {
	@Invoker
	static void callRegister(Class<? extends EntityBase> entityClass, String name, int i) {
		throw new AssertionError("@Invoker dummy body called");
	}

	@Accessor("STRING_ID_TO_CLASS")
	static Map<String, Class<? extends EntityBase>> getStringToClassMap() {
		throw new AssertionError("@Accessor dummy body called");
	}
}
