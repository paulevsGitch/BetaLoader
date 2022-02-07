package paulevs.betaloader.mixin.client;

import modloader.BaseMod;
import modloader.ModLoader;
import net.minecraft.applet.GameStartupErrorPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameStartupErrorPanel.class)
public class GameStartupErrorPanelMixin {
	@ModifyVariable(method = "<init>*", at = @At("STORE"), name = "var6")
	private String betaloader_errorPanelText(String test) {
		StringBuilder testBuilder = new StringBuilder(test);
		testBuilder.append("Mods loaded: ").append(ModLoader.getLoadedMods().size() + 1).append('\n')
				.append(ModLoader.VERSION).append('\n');
		for (BaseMod baseMod : ModLoader.getLoadedMods()) {
			testBuilder.append(baseMod.getClass().getName()).append(' ').append(baseMod.Version()).append('\n');
		}
		test = testBuilder.append('\n').toString();
		return test;
	}
}
