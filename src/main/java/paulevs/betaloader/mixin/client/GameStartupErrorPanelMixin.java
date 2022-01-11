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
		test += "Mods loaded: " + (ModLoader.getLoadedMods().size() + 1) + "\n";
		test += "ModLoader Beta 1.7.3" + "\n";
		for (BaseMod baseMod : ModLoader.getLoadedMods()) {
			test += baseMod.getClass().getName() + " " + baseMod.Version() + "\n";
		}
		test += "\n";
		return test;
	}
}
