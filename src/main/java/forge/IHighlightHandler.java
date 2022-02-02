package forge;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.ItemInstance;
import net.minecraft.util.hit.HitResult;

public interface IHighlightHandler {
    boolean onBlockHighlight(final WorldRenderer worldRenderer, final PlayerBase playerBase, final HitResult hitResult, final int i, final ItemInstance itemInstance, final float f);
}
