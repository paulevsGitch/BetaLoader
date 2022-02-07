package forge;

import net.minecraft.block.BlockBase;
import net.minecraft.item.ItemBase;
import net.minecraft.item.ItemInstance;
import net.minecraft.level.Level;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MinecraftForge {
    private static LinkedList<IBucketHandler> bucketHandlers;
    
    @Deprecated
    public static void registerCustomBucketHander(final IBucketHandler handler) {
        registerCustomBucketHandler(handler);
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
    
    public static void setToolClass(final ItemBase tool, final String tClass, final int hLevel) {
        ForgeHooks.initTools();
        ForgeHooks.toolClasses.put(tool.id, Arrays.asList(tClass, hLevel));
    }
    
    public static void setBlockHarvestLevel(final BlockBase bl, final int md, final String tClass, final int hLevel) {
        ForgeHooks.initTools();
        final List<Object> key = Arrays.asList(bl.id, md, tClass);
        ForgeHooks.toolHarvestLevels.put(key, hLevel);
        ForgeHooks.toolEffectiveness.add(key);
    }
    
    public static void removeBlockEffectiveness(final BlockBase bl, final int md, final String tClass) {
        ForgeHooks.initTools();
        final List<Object> key = Arrays.asList(bl.id, md, tClass);
        ForgeHooks.toolEffectiveness.remove(key);
    }
    
    public static void setBlockHarvestLevel(final BlockBase bl, final String tClass, final int hLevel) {
        ForgeHooks.initTools();
        for (int md = 0; md < 16; ++md) {
            final List<Object> key = Arrays.asList(bl.id, md, tClass);
            ForgeHooks.toolHarvestLevels.put(key, hLevel);
            ForgeHooks.toolEffectiveness.add(key);
        }
    }
    
    public static void removeBlockEffectiveness(final BlockBase bl, final String tClass) {
        ForgeHooks.initTools();
        for (int md = 0; md < 16; ++md) {
            final List<Object> key = Arrays.asList(bl.id, md, tClass);
            ForgeHooks.toolEffectiveness.remove(key);
        }
    }
    
    public static void addPickaxeBlockEffectiveAgainst(final BlockBase block) {
        setBlockHarvestLevel(block, "pickaxe", 0);
    }
    
    public static void killMinecraft(final String modName, final String msg) {
        throw new RuntimeException(modName + ": " + msg);
    }
    
    public static void versionDetect(final String modName, final int major, final int minor, final int revision) {
        if (major != 1) {
            killMinecraft(modName, "MinecraftForge Major Version Mismatch, expecting " + major + ".x.x");
        }
        else if (minor != 0) {
            if (minor > 0) {
                killMinecraft(modName, "MinecraftForge Too Old, need at least " + major + "." + minor + "." + revision);
            }
            else {
                System.out.println(modName + ": MinecraftForge minor version mismatch, expecting " + major + "." + minor + ".x, may lead to unexpected behavior");
            }
        }
        else if (revision > 6) {
            killMinecraft(modName, "MinecraftForge Too Old, need at least " + major + "." + minor + "." + revision);
        }
    }
    
    public static void versionDetectStrict(final String modName, final int major, final int minor, final int revision) {
        if (major != 1) {
            killMinecraft(modName, "MinecraftForge Major Version Mismatch, expecting " + major + ".x.x");
        }
        else if (minor != 0) {
            if (minor > 0) {
                killMinecraft(modName, "MinecraftForge Too Old, need at least " + major + "." + minor + "." + revision);
            }
            else {
                killMinecraft(modName, "MinecraftForge minor version mismatch, expecting " + major + "." + minor + ".x");
            }
        }
        else if (revision > 6) {
            killMinecraft(modName, "MinecraftForge Too Old, need at least " + major + "." + minor + "." + revision);
        }
    }
    
    static {
        MinecraftForge.bucketHandlers = new LinkedList<>();
    }
}
