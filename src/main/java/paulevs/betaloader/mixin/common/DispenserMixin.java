package paulevs.betaloader.mixin.common;

import net.minecraft.block.Dispenser;
import net.minecraft.item.ItemInstance;
import net.minecraft.level.Level;
import net.minecraft.tileentity.TileEntityDispenser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import modloader.ModLoader;

import java.util.Random;

@Mixin(Dispenser.class)
public class DispenserMixin {
	private boolean modloader_dispensed;
	private Level modloader_level;
	private double modloader_posX;
	private double modloader_posY;
	private double modloader_posZ;
	private int modloader_velX;
	private int modloader_velZ;
	
	@Inject(
		method = "dispense",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/tileentity/TileEntityDispenser;getItemToDispense()Lnet/minecraft/item/ItemInstance;",
			shift = Shift.BEFORE
		),
		locals = LocalCapture.CAPTURE_FAILSOFT)
	private void modloader_dispense(Level level, int x, int y, int z, Random arg4, CallbackInfo info, int var6, int var9, int var10, TileEntityDispenser var11) {
		modloader_level = level;
		modloader_posX = x + var9 * 0.6 + 0.5;
		modloader_posY = y + 0.5;
		modloader_posZ = z + var10 * 0.6 + 0.5;
		modloader_velX = var9;
		modloader_velZ = var10;
	}
	
	@ModifyVariable(method = "dispense", at = @At("STORE"), name = "var12")
	private ItemInstance injected(ItemInstance item) {
		modloader_dispensed = ModLoader.DispenseEntity(
			modloader_level,
			modloader_posX,
			modloader_posY,
			modloader_posZ,
			modloader_velX,
			modloader_velZ,
			item
		);
		return item;
	}
	
	@Inject(
		method = "dispense",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/level/Level;spawnEntity(Lnet/minecraft/entity/EntityBase;)Z",
			shift = Shift.BEFORE
		),
		cancellable = true)
	private void modloader_cancelDispense(Level level, int x, int y, int z, Random arg4, CallbackInfo info) {
		if (modloader_dispensed) {
			level.playLevelEvent(2000, x, y, z, modloader_velX + 1 + (modloader_velZ + 1) * 3);
			info.cancel();
		}
	}
}
