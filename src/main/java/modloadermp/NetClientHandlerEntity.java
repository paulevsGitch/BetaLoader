package modloadermp;

public class NetClientHandlerEntity {
	public boolean entityHasOwner;
	public Class entityClass;
	
	public NetClientHandlerEntity(final Class entityClass, final boolean entityHasOwner) {
		this.entityHasOwner = entityHasOwner;
		this.entityClass = entityClass;
	}
}
