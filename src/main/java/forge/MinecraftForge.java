package forge;

import net.minecraft.block.BlockBase;
import net.minecraft.item.ItemBase;
import net.minecraft.item.ItemInstance;
import net.minecraft.level.Level;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MinecraftForge {
    private static LinkedList<IBucketHandler> bucketHandlers;
    
    @Deprecated
    public static void registerCustomBucketHander(final IBucketHandler handler) {
        MinecraftForge.bucketHandlers.add(handler);
    }
    
    public static void registerCustomBucketHandler(final IBucketHandler handler) {
        MinecraftForge.bucketHandlers.add(handler);
    }
    
    public static void registerSleepHandler(final ISleepHandler handler) {
        ForgeHooks.sleepHandlers.add(handler);
    }
    
    public static void registerDestroyToolHandler(final IDestroyToolHandler handler) {
        ForgeHooks.destroyToolHandlers.add(handler);
    }
    
    public static void registerCraftingHandler(final ICraftingHandler handler) {
        ForgeHooks.craftingHandlers.add(handler);
    }
    
    public static ItemInstance fillCustomBucket(final Level w, final int i, final int j, final int k) {
        for (final IBucketHandler handler : MinecraftForge.bucketHandlers) {
            final ItemInstance stack = handler.fillCustomBucket(w, i, j, k);
            if (stack != null) {
                return stack;
            }
        }
        return null;
    }
    
    public static void setToolClass(final ItemBase tool, final String tclass, final int hlevel) {
        ForgeHooks.initTools();
        ForgeHooks.toolClasses.put(tool.id, Arrays.asList((Object[])new Serializable[] { (Serializable)tclass, (Serializable)hlevel }));
    }
    
    public static void setBlockHarvestLevel(final BlockBase bl, final int md, final String tclass, final int hlevel) {
        ForgeHooks.initTools();
        final List key = Arrays.asList((Object[])new Serializable[] { (Serializable)bl.id, (Serializable)md, (Serializable)tclass });
        ForgeHooks.toolHarvestLevels.put(key, hlevel);
        ForgeHooks.toolEffectiveness.add(key);
    }
    
    public static void removeBlockEffectiveness(final BlockBase bl, final int md, final String tclass) {
        ForgeHooks.initTools();
        final List key = Arrays.asList((Object[])new Serializable[] { (Serializable)bl.id, (Serializable)md, (Serializable)tclass });
        ForgeHooks.toolEffectiveness.remove(key);
    }
    
    public static void setBlockHarvestLevel(final BlockBase bl, final String tclass, final int hlevel) {
        ForgeHooks.initTools();
        for (int md = 0; md < 16; ++md) {
            final List key = Arrays.asList((Object[])new Serializable[] { (Serializable)bl.id, (Serializable)md, (Serializable)tclass });
            ForgeHooks.toolHarvestLevels.put(key, hlevel);
            ForgeHooks.toolEffectiveness.add(key);
        }
    }
    
    public static void removeBlockEffectiveness(final BlockBase bl, final String tclass) {
        ForgeHooks.initTools();
        for (int md = 0; md < 16; ++md) {
            final List key = Arrays.asList((Object[])new Serializable[] { (Serializable)bl.id, (Serializable)md, (Serializable)tclass });
            ForgeHooks.toolEffectiveness.remove(key);
        }
    }
    
    public static void addPickaxeBlockEffectiveAgainst(final BlockBase block) {
        setBlockHarvestLevel(block, "pickaxe", 0);
    }
    
    public static void killMinecraft(final String modname, final String msg) {
        throw new RuntimeException(modname + ": " + msg);
    }
    
    public static void versionDetect(final String modname, final int major, final int minor, final int revision) {
        if (major != 1) {
            killMinecraft(modname, new StringBuilder().append("MinecraftForge Major Version Mismatch, expecting ").append(major).append(".x.x").toString());
        }
        else if (minor != 0) {
            if (minor > 0) {
                killMinecraft(modname, new StringBuilder().append("MinecraftForge Too Old, need at least ").append(major).append(".").append(minor).append(".").append(revision).toString());
            }
            else {
                System.out.println(modname + ": MinecraftForge minor version mismatch, expecting " + major + "." + minor + ".x, may lead to unexpected behavior");
            }
        }
        else if (revision > 6) {
            killMinecraft(modname, new StringBuilder().append("MinecraftForge Too Old, need at least ").append(major).append(".").append(minor).append(".").append(revision).toString());
        }
    }
    
    public static void versionDetectStrict(final String modname, final int major, final int minor, final int revision) {
        if (major != 1) {
            killMinecraft(modname, new StringBuilder().append("MinecraftForge Major Version Mismatch, expecting ").append(major).append(".x.x").toString());
        }
        else if (minor != 0) {
            if (minor > 0) {
                killMinecraft(modname, new StringBuilder().append("MinecraftForge Too Old, need at least ").append(major).append(".").append(minor).append(".").append(revision).toString());
            }
            else {
                killMinecraft(modname, new StringBuilder().append("MinecraftForge minor version mismatch, expecting ").append(major).append(".").append(minor).append(".x").toString());
            }
        }
        else if (revision > 6) {
            killMinecraft(modname, new StringBuilder().append("MinecraftForge Too Old, need at least ").append(major).append(".").append(minor).append(".").append(revision).toString());
        }
    }
    
    static {
        MinecraftForge.bucketHandlers = (LinkedList<IBucketHandler>)new LinkedList();
    }
}
