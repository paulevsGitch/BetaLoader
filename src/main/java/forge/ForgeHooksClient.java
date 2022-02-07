package forge;

import modloader.ModLoader;
import net.minecraft.block.BlockBase;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.ItemInstance;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

// TODO do something with this hooks
public class ForgeHooksClient {
    static LinkedList<IHighlightHandler> highlightHandlers;
    static HashMap<List<Object>, Tessellator> tessellators;
    static HashMap<String, Integer> textures;
    static boolean inWorld;
    static HashSet<List<Object>> renderTextureTest;
    static ArrayList<List<Object>> renderTextureList;
    static int renderPass;
    
    public static boolean onBlockHighlight(final WorldRenderer renderglobal, final PlayerBase player, final HitResult mop, final int i, final ItemInstance itemstack, final float f) {
        for (final IHighlightHandler handler : ForgeHooksClient.highlightHandlers) {
            if (handler.onBlockHighlight(renderglobal, player, mop, i, itemstack, f)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean canRenderInPass(final BlockBase block, final int pass) {
        if (block instanceof IMultipassRender) {
            final IMultipassRender impr = (IMultipassRender)block;
            return impr.canRenderInPass(pass);
        }
        return pass == block.getRenderPass();
    }
    
    // TODO do something with tesselators
    protected static void bindTessellator(final int tex, final int sub) {
        /*final List key = Arrays.asList((Object[])new Integer[] { tex, sub });
        Tessellator t;
        if (!ForgeHooksClient.tessellators.containsKey(key)) {
            t = new Tessellator();
            ForgeHooksClient.tessellators.put(key, t);
        }
        else {
            t = (Tessellator)ForgeHooksClient.tessellators.get(key);
        }
        if (ForgeHooksClient.inWorld && !ForgeHooksClient.renderTextureTest.contains(key)) {
            ForgeHooksClient.renderTextureTest.add(key);
            ForgeHooksClient.renderTextureList.add(key);
            t.start();
            t.setOffset(Tessellator.firstInstance.xOffset, Tessellator.firstInstance.yOffset, Tessellator.firstInstance.zOffset);
        }
        Tessellator.INSTANCE = t;*/
    }
    
    // TODO manage texture binding
    protected static void bindTexture(final String name, final int sub) {
        /*int n;
        if (!ForgeHooksClient.textures.containsKey(name)) {
            n = ModLoader.getMinecraftInstance().textureManager.getTextureId(name);
            ForgeHooksClient.textures.put(name, n);
        }
        else {
            n = (int)ForgeHooksClient.textures.get(name);
        }
        if (!ForgeHooksClient.inWorld) {
            Tessellator.INSTANCE = Tessellator.firstInstance;
            GL11.glBindTexture(3553, n);
            return;
        }
        bindTessellator(n, sub);*/
    }
    
    // TODO manage textures unbinding
    protected static void unbindTexture() {
        /*Tessellator.INSTANCE = Tessellator.firstInstance;
        if (!ForgeHooksClient.inWorld) {
            GL11.glBindTexture(3553, ModLoader.getMinecraftInstance().textureManager.getTextureId("/terrain.png"));
        }*/
    }
    
    public static void beforeRenderPass(final int pass) {
        /*ForgeHooksClient.renderPass = pass;
        Tessellator.INSTANCE = Tessellator.firstInstance;
        Tessellator.renderingWorldRenderer = true;
        GL11.glBindTexture(3553, ModLoader.getMinecraftInstance().textureManager.getTextureId("/terrain.png"));
        ForgeHooksClient.renderTextureTest.clear();
        ForgeHooksClient.renderTextureList.clear();
        ForgeHooksClient.inWorld = true;*/
    }
    
    public static void afterRenderPass(final int pass) {
        /*ForgeHooksClient.renderPass = -1;
        ForgeHooksClient.inWorld = false;
        for (final List l : ForgeHooksClient.renderTextureList) {
            final Integer[] tn = (Integer[])l.toArray();
            GL11.glBindTexture(3553, (int)tn[0]);
            final Tessellator t = (Tessellator)ForgeHooksClient.tessellators.get(l);
            t.draw();
        }
        GL11.glBindTexture(3553, ModLoader.getMinecraftInstance().textureManager.getTextureId("/terrain.png"));
        Tessellator.INSTANCE = Tessellator.firstInstance;
        Tessellator.renderingWorldRenderer = false;*/
    }
    
    public static void beforeBlockRender(final BlockBase block, final BlockRenderer renderblocks) {
        /*if (block instanceof ITextureProvider && renderblocks.textureOverride == -1) {
            final ITextureProvider itp = (ITextureProvider)block;
            bindTexture(itp.getTextureFile(), 0);
        }*/
    }
    
    public static void afterBlockRender(final BlockBase block, final BlockRenderer renderblocks) {
        /*if (block instanceof ITextureProvider && renderblocks.textureOverride == -1) {
            unbindTexture();
        }*/
    }
    
    public static void overrideTexture(final Object o) {
        if (o instanceof ITextureProvider) {
            GL11.glBindTexture(3553, ModLoader.getMinecraftInstance().textureManager.getTextureId(((ITextureProvider)o).getTextureFile()));
        }
    }
    
    static {
        ForgeHooksClient.highlightHandlers = new LinkedList<>();
        ForgeHooksClient.tessellators = new HashMap<>();
        ForgeHooksClient.textures = new HashMap<>();
        ForgeHooksClient.inWorld = false;
        ForgeHooksClient.renderTextureTest = new HashSet<>();
        ForgeHooksClient.renderTextureList = new ArrayList<>();
        ForgeHooksClient.renderPass = -1;
    }
}
