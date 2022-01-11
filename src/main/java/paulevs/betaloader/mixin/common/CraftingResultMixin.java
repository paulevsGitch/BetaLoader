package paulevs.betaloader.mixin.common;

import modloader.ModLoader;
import net.minecraft.container.slot.CraftingResult;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.ItemInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingResult.class)
public class CraftingResultMixin {
	@Shadow
	private PlayerBase player;
	
	@Inject(method = "onCrafted", at = @At(value = "HEAD"))
	private void modloader_onCrafted(ItemInstance stack, CallbackInfo info) {
		ModLoader.TakenFromCrafting(player, stack);
	}
}
