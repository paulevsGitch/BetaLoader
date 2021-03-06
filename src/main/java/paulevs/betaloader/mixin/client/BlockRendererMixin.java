package paulevs.betaloader.mixin.client;

import modloader.ModLoader;
import net.minecraft.block.BlockBase;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.level.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import paulevs.betaloader.rendering.BlockRendererData;
import paulevs.betaloader.rendering.IBlockRenderer;

@Mixin(BlockRenderer.class)
public class BlockRendererMixin implements IBlockRenderer {
	private float betaloader_brightness;
	private int betaloader_meta;
	
	@Shadow
	private BlockView blockView;
	
	@Inject(method = "render", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/block/BlockBase;updateBoundingBox(Lnet/minecraft/level/BlockView;III)V",
		shift = Shift.BEFORE
	), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	private void betaloader_renderBlock(BlockBase block, int blockX, int blockY, int blockZ, CallbackInfoReturnable<Boolean> info, int renderType) {
		if (renderType > 17) {
			BlockRenderer renderer = BlockRenderer.class.cast(this);
			info.setReturnValue(ModLoader.RenderWorldBlock(renderer, blockView, blockX, blockY, blockZ, block, renderType));
		}
	}
	
	@Inject(method = "renderRedstoneDust", at = @At(value = "HEAD"))
	private void betaloader_renderRedstoneDust(BlockBase block, int x, int y, int z, CallbackInfoReturnable<Boolean> info) {
		betaloader_brightness = block.getBrightness(blockView, x, y, z);
		betaloader_meta = blockView.getTileMeta(x, y, z);
	}
	
	@ModifyArgs(method = "renderRedstoneDust", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/render/Tessellator;colour(FFF)V"
	))
	private void betaloader_colorRedstoneDust(Args args) {
		float[] rgb = BlockRendererData.redstoneColors[betaloader_meta];
		args.set(0, rgb[0] * betaloader_brightness);
		args.set(1, rgb[1] * betaloader_brightness);
		args.set(2, rgb[2] * betaloader_brightness);
	}
}
