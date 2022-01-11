package paulevs.betaloader.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.net.URL;
import java.net.URLClassLoader;

@Mixin(value = URLClassLoader.class, remap = false)
public interface URLClassLoaderAccessor {
	@Invoker
	void callAddURL(URL url);
}
