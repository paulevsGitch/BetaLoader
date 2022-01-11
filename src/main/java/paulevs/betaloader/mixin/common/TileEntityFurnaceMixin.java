package paulevs.betaloader.mixin.common;

import modloader.ModLoader;
import net.minecraft.item.ItemInstance;
import net.minecraft.tileentity.TileEntityFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TileEntityFurnace.class)
public class TileEntityFurnaceMixin {
	@Inject(method = "getFuelTime", at = @At(value = "HEAD"), cancellable = true)
	private void betaloader_getFuelTime(ItemInstance stack, CallbackInfoReturnable<Integer> info) {
		if (stack != null) {
			int fuel = ModLoader.AddAllFuel(stack.getType().id);
			if (fuel > 0) {
				info.setReturnValue(fuel);
			}
		}
	}
}
