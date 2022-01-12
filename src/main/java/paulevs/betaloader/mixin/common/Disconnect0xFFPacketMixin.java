package paulevs.betaloader.mixin.common;

import net.minecraft.packet.AbstractPacket;
import net.minecraft.packet.misc.Disconnect0xFFPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.DataInputStream;

@Mixin(Disconnect0xFFPacket.class)
public abstract class Disconnect0xFFPacketMixin extends AbstractPacket {
	@Shadow
	public String reason;
	
	@Inject(method = "read", at = @At(value = "HEAD"), cancellable = true)
	private void betaloader_read(DataInputStream in, CallbackInfo info) {
		this.reason = readString(in, 1000);
		info.cancel();
	}
}
