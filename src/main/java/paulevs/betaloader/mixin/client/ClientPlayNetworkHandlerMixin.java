package paulevs.betaloader.mixin.client;

import modloader.ModLoader;
import modloadermp.ModLoaderMp;
import modloadermp.NetClientHandlerEntity;
import net.minecraft.client.level.ClientLevel;
import net.minecraft.entity.EntityBase;
import net.minecraft.entity.Living;
import net.minecraft.entity.projectile.Arrow;
import net.minecraft.level.Level;
import net.minecraft.network.ClientPlayNetworkHandler;
import net.minecraft.packet.play.EntitySpawn0x17S2CPacket;
import net.minecraft.packet.play.OpenContainer0x64S2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow
	private ClientLevel level;
	
	@Inject(method = "onEntitySpawn", at = @At(value = "HEAD"), cancellable = true)
	private void betaloader_onEntitySpawn(EntitySpawn0x17S2CPacket packet, CallbackInfo info) {
		final double x = packet.x / 32.0;
		final double y = packet.y / 32.0;
		final double z = packet.z / 32.0;
		EntityBase entity = null;
		
		final NetClientHandlerEntity handlerEntity = ModLoaderMp.HandleNetClientHandlerEntities(packet.type);
		if (handlerEntity != null) {
			try {
				entity = handlerEntity.entityClass.getConstructor(Level.class, Double.TYPE, Double.TYPE, Double.TYPE).newInstance(this.level, x, y, z);
				if (handlerEntity.entityHasOwner) {
					final Field field = handlerEntity.entityClass.getField("owner");
					if (!EntityBase.class.isAssignableFrom(field.getType())) {
						throw new Exception(String.format("Entity's owner field must be of type Entity, but it is of type %s.", field.getType()));
					}
					final EntityBase entity1 = this.method_1645(packet.flag);
					if (entity1 == null) {
						ModLoaderMp.Log("Received spawn packet for entity with owner, but owner was not found.");
					}
					else {
						if (!field.getType().isAssignableFrom(entity1.getClass())) {
							throw new Exception(String.format("Tried to assign an entity of type %s to entity owner, which is of type %s.", entity1.getClass(), field.getType()));
						}
						field.set(entity, entity1);
					}
				}
			}
			catch (Exception exception) {
				ModLoader.getLogger().throwing("NetClientHandler", "handleVehicleSpawn", exception);
				ModLoader.ThrowException(String.format("Error initializing entity of type %s.", packet.type), exception);
				return;
			}
		}
		
		if (entity != null) {
			entity.clientX = packet.x;
			entity.clientY = packet.y;
			entity.clientZ = packet.z;
			entity.yaw = 0.0f;
			entity.pitch = 0.0f;
			entity.entityId = packet.entityId;
			this.level.method_1495(packet.entityId, entity);
			if (packet.flag > 0) {
				if (packet.type == 60) {
					final EntityBase entity2 = this.method_1645(packet.flag);
					if (entity2 instanceof Living) {
						((Arrow) entity).owner = (Living) entity2;
					}
				}
				entity.setVelocity(packet.field_1667 / 8000.0, packet.field_1668 / 8000.0, packet.field_1669 / 8000.0);
			}
			info.cancel();
		}
	}
	
	@Inject(method = "onOpenContainer", at = @At(value = "HEAD"), cancellable = true)
	private void betaloader_onOpenContainer(OpenContainer0x64S2CPacket packet, CallbackInfo info) {
		int type = packet.inventoryType;
		if (type < 0 || type > 3) {
			ModLoaderMp.HandleGUI(packet);
			info.cancel();
		}
	}
	
	@Shadow
	private EntityBase method_1645(int id) {
		return null;
	}
}
