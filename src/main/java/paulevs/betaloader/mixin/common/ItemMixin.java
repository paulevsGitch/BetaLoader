package paulevs.betaloader.mixin.common;

import modloader.ModLoader;
import net.minecraft.entity.Item;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.ItemInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public class ItemMixin {
	@Shadow
	public ItemInstance item;
	
	@Inject(method = "onPlayerCollision", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/level/Level;playSound(Lnet/minecraft/entity/EntityBase;Ljava/lang/String;FF)V",
		shift = Shift.BEFORE
	))
	public void onPlayerCollision(PlayerBase player, CallbackInfo ci) {
		ModLoader.OnItemPickup(player, item);
	}
}
