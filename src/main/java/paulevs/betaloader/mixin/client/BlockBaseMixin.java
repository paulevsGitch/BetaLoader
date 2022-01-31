package paulevs.betaloader.mixin.client;

import net.minecraft.block.BlockBase;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import net.modificationstation.stationapi.api.client.texture.atlas.CustomAtlasProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockBase.class)
public class BlockBaseMixin /*implements CustomAtlasProvider*/ {
	/*private Atlas betaloader_atlas = Atlases.getTerrain();
	
	@Override
	public Atlas getAtlas() {
		System.out.println("Override!");
		return betaloader_atlas;
	}*/
}
