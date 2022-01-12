package paulevs.betaloader.mixin.client;

import net.minecraft.client.gui.screen.Disconnecting;
import net.minecraft.client.gui.screen.ScreenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Disconnecting.class)
public abstract class DisconnectingMixin extends ScreenBase {
	@Shadow
	private String line1;
	@Shadow
	private String line2;
	
	@Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
	private void betaloader_renderDisconnecting(int mouseX, int mouseY, float delta, CallbackInfo info) {
		this.renderBackground();
		this.drawTextWithShadowCentred(this.textManager, this.line1, this.width / 2, this.height / 2 - 50, 16777215);
		final String[] as = this.line2.split("\n");
		for (int k = 0; k < as.length; ++k) {
			this.drawTextWithShadowCentred(this.textManager, as[k], this.width / 2, this.height / 2 - 10 + k * 10, 16777215);
		}
		super.render(mouseX, mouseY, delta);
		info.cancel();
	}
}
