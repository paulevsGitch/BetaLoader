package forge;

import net.minecraft.block.BlockBase;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.inventory.InventoryBase;
import net.minecraft.item.ItemBase;
import net.minecraft.item.ItemInstance;
import net.minecraft.util.SleepStatus;
import paulevs.betaloader.mixininterfaces.BLBlockBase;
import paulevs.betaloader.mixininterfaces.BLPlayerBase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class ForgeHooks {
    static LinkedList<ICraftingHandler> craftingHandlers;
    static LinkedList<IDestroyToolHandler> destroyToolHandlers;
    static LinkedList<ISleepHandler> sleepHandlers;
    public static final int majorVersion = 1;
    public static final int minorVersion = 0;
    public static final int revisionVersion = 6;
    static boolean toolInit;
    static HashMap<Integer, List<Object>> toolClasses;
    static HashMap<List<Object>, Integer> toolHarvestLevels;
    static HashSet<List<Object>> toolEffectiveness;
    
    public static void onTakenFromCrafting(final PlayerBase player, final ItemInstance ist, final InventoryBase craftMatrix) {
        for (final ICraftingHandler handler : ForgeHooks.craftingHandlers) {
            handler.onTakenFromCrafting(player, ist, craftMatrix);
        }
    }
    
    public static void onDestroyCurrentItem(final PlayerBase player, final ItemInstance orig) {
        for (final IDestroyToolHandler handler : ForgeHooks.destroyToolHandlers) {
            handler.onDestroyCurrentItem(player, orig);
        }
    }
    
    public static SleepStatus sleepInBedAt(final PlayerBase player, final int i, final int j, final int k) {
        for (final ISleepHandler handler : ForgeHooks.sleepHandlers) {
            final SleepStatus status = handler.sleepInBedAt(player, i, j, k);
            if (status != null) {
                return status;
            }
        }
        return null;
    }
    
    public static boolean canHarvestBlock(final BlockBase block, final PlayerBase player, final int meta) {
        if (block.material.doesRequireTool()) {
            return true;
        }
        final ItemInstance itemstack = player.inventory.getHeldItem();
        if (itemstack == null) {
            return false;
        }
        final List<Object> tc = ForgeHooks.toolClasses.get(itemstack.itemId);
        if (tc == null) {
            return itemstack.isEffectiveOn(block);
        }
        final Object[] ta = tc.toArray();
        final String cls = (String)ta[0];
        final int hvl = (int)ta[1];
        final Integer bhl = ForgeHooks.toolHarvestLevels.get(Arrays.asList(block.id, meta, cls));
        if (bhl == null) {
            return itemstack.isEffectiveOn(block);
        }
        return bhl <= hvl && itemstack.isEffectiveOn(block);
    }
    
    public static float blockStrength(final BlockBase block, final PlayerBase player, final int meta) {
        final float bh = ((BLBlockBase) block).getHardness(meta);
        if (bh < 0.0f) {
            return 0.0f;
        }
        if (!canHarvestBlock(block, player, meta)) {
            return 1.0f / bh / 100.0f;
        }
        return ((BLPlayerBase) player).getCurrentPlayerStrVsBlock(block, meta) / bh / 30.0f;
    }
    
    public static boolean isToolEffective(final ItemInstance item, final BlockBase block, final int meta) {
        final List<Object> tc = ForgeHooks.toolClasses.get(item.itemId);
        if (tc == null) {
            return false;
        }
        final Object[] ta = tc.toArray();
        final String cls = (String) ta[0];
        return ForgeHooks.toolEffectiveness.contains(Arrays.asList(block.id, meta, cls));
    }
    
    static void initTools() {
        if (ForgeHooks.toolInit) {
            return;
        }
        ForgeHooks.toolInit = true;
        MinecraftForge.setToolClass(ItemBase.woodPickaxe, "pickaxe", 0);
        MinecraftForge.setToolClass(ItemBase.stonePickaxe, "pickaxe", 1);
        MinecraftForge.setToolClass(ItemBase.ironPickaxe, "pickaxe", 2);
        MinecraftForge.setToolClass(ItemBase.goldPickaxe, "pickaxe", 0);
        MinecraftForge.setToolClass(ItemBase.diamondPickaxe, "pickaxe", 3);
        MinecraftForge.setToolClass(ItemBase.woodAxe, "axe", 0);
        MinecraftForge.setToolClass(ItemBase.stoneAxe, "axe", 1);
        MinecraftForge.setToolClass(ItemBase.ironAxe, "axe", 2);
        MinecraftForge.setToolClass(ItemBase.goldAxe, "axe", 0);
        MinecraftForge.setToolClass(ItemBase.diamondAxe, "axe", 3);
        MinecraftForge.setToolClass(ItemBase.woodShovel, "shovel", 0);
        MinecraftForge.setToolClass(ItemBase.stoneShovel, "shovel", 1);
        MinecraftForge.setToolClass(ItemBase.ironShovel, "shovel", 2);
        MinecraftForge.setToolClass(ItemBase.goldShovel, "shovel", 0);
        MinecraftForge.setToolClass(ItemBase.diamondShovel, "shovel", 3);
        MinecraftForge.setBlockHarvestLevel(BlockBase.OBSIDIAN, "pickaxe", 3);
        MinecraftForge.setBlockHarvestLevel(BlockBase.DIAMOND_ORE, "pickaxe", 2);
        MinecraftForge.setBlockHarvestLevel(BlockBase.DIAMOND_BLOCK, "pickaxe", 2);
        MinecraftForge.setBlockHarvestLevel(BlockBase.GOLD_ORE, "pickaxe", 2);
        MinecraftForge.setBlockHarvestLevel(BlockBase.GOLD_BLOCK, "pickaxe", 2);
        MinecraftForge.setBlockHarvestLevel(BlockBase.IRON_ORE, "pickaxe", 1);
        MinecraftForge.setBlockHarvestLevel(BlockBase.IRON_BLOCK, "pickaxe", 1);
        MinecraftForge.setBlockHarvestLevel(BlockBase.LAPIS_LAZULI_ORE, "pickaxe", 1);
        MinecraftForge.setBlockHarvestLevel(BlockBase.LAPIS_LAZULI_BLOCK, "pickaxe", 1);
        MinecraftForge.setBlockHarvestLevel(BlockBase.REDSTONE_ORE, "pickaxe", 2);
        MinecraftForge.setBlockHarvestLevel(BlockBase.REDSTONE_ORE_LIT, "pickaxe", 2);
        MinecraftForge.removeBlockEffectiveness(BlockBase.REDSTONE_ORE, "pickaxe");
        MinecraftForge.removeBlockEffectiveness(BlockBase.REDSTONE_ORE_LIT, "pickaxe");
        final BlockBase[] pickeff = new BlockBase[] { BlockBase.COBBLESTONE, BlockBase.DOUBLE_STONE_SLAB, BlockBase.STONE_SLAB, BlockBase.STONE, BlockBase.SANDSTONE, BlockBase.MOSSY_COBBLESTONE, BlockBase.IRON_ORE, BlockBase.IRON_BLOCK, BlockBase.COAL_ORE, BlockBase.GOLD_BLOCK, BlockBase.GOLD_ORE, BlockBase.DIAMOND_ORE, BlockBase.DIAMOND_BLOCK, BlockBase.ICE, BlockBase.NETHERRACK, BlockBase.LAPIS_LAZULI_ORE, BlockBase.LAPIS_LAZULI_BLOCK };
        for (final BlockBase bl : pickeff) {
            MinecraftForge.setBlockHarvestLevel(bl, "pickaxe", 0);
        }
    }
    
    static {
        ForgeHooks.craftingHandlers = new LinkedList<>();
        ForgeHooks.destroyToolHandlers = new LinkedList<>();
        ForgeHooks.sleepHandlers = new LinkedList<>();
        System.out.printf("MinecraftForge V%d.%d.%d Initialized\n",majorVersion,minorVersion,revisionVersion);
        ForgeHooks.toolInit = false;
        ForgeHooks.toolClasses = new HashMap<>();
        ForgeHooks.toolHarvestLevels = new HashMap<>();
        ForgeHooks.toolEffectiveness = new HashSet<>();
    }
}
