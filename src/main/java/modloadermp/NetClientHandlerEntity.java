package modloadermp;

import net.minecraft.entity.EntityBase;

public class NetClientHandlerEntity {
	public boolean entityHasOwner;
	public Class<? extends EntityBase> entityClass;
	
	public NetClientHandlerEntity(final Class<? extends EntityBase> entityClass, final boolean entityHasOwner) {
		this.entityHasOwner = entityHasOwner;
		this.entityClass = entityClass;
	}
}
