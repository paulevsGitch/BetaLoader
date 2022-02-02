package forge;

import modloader.ModLoader;
import net.minecraft.block.BlockBase;
import net.minecraft.client.render.block.BlockRenderer;

public class MinecraftForgeClient {
    public static void registerHighlightHandler(final IHighlightHandler handler) {
        ForgeHooksClient.highlightHandlers.add(handler);
    }
    
    public static void bindTexture(final String name, final int sub) {
        ForgeHooksClient.bindTexture(name, sub);
    }
    
    public static void bindTexture(final String name) {
        ForgeHooksClient.bindTexture(name, 0);
    }
    
    public static void unbindTexture() {
        ForgeHooksClient.unbindTexture();
    }
    
    public static void preloadTexture(final String texture) {
        ModLoader.getMinecraftInstance().textureManager.getTextureId(texture);
    }
    
    public static void renderBlock(final BlockRenderer rb, final BlockBase bl, final int i, final int j, final int k) {
        ForgeHooksClient.beforeBlockRender(bl, rb);
        rb.render(bl, i, j, k);
        ForgeHooksClient.afterBlockRender(bl, rb);
    }
    
    public static int getRenderPass() {
        return ForgeHooksClient.renderPass;
    }
}
