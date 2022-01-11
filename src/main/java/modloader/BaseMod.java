package modloader;

import net.minecraft.block.BlockBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ScreenBase;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.block.BlockRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.EntityBase;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.item.ItemInstance;
import net.minecraft.level.BlockView;
import net.minecraft.level.Level;

import java.util.Map;
import java.util.Random;

public abstract class BaseMod {
	/**
	 * Used for adding new sources of fuel to the furnace.
	 * @param id
	 * @return
	 */
	public int AddFuel(int id) {
		return 0;
	}
	
	/**
	 * Used to add entity renderers.
	 * @param rendererMap
	 */
	public void AddRenderer(Map<Class<? extends EntityBase>, ? extends EntityRenderer> rendererMap) {}
	
	/**
	 * Dispenses the entity associated with the selected item.
	 * @param level
	 * @param x
	 * @param y
	 * @param z
	 * @param xVel
	 * @param yVel
	 * @param item
	 * @return
	 */
	public boolean DispenseEntity(Level level, double x, double y, double z, int xVel, int yVel, ItemInstance item) {
		return false;
	}
	
	/**
	 * Used for generating new blocks (veins) in Nether.
	 * @param level
	 * @param random
	 * @param chunkX
	 * @param chunkZ
	 */
	public void GenerateNether(Level level, Random random, int chunkX, int chunkZ) {}
	
	/**
	 * Used for generating new blocks (veins) on the surface world.
	 * @param level
	 * @param random
	 * @param chunkX
	 * @param chunkZ
	 */
	public void GenerateSurface(Level level, Random random, int chunkX, int chunkZ) {}
	
	/**
	 * This method will be called when the register key has been pressed, or held down.
	 * @param keyBinding
	 */
	public void KeyboardEvent(KeyBinding keyBinding) {}
	
	/**
	 * Called after all mods are loaded.
	 */
	public void ModsLoaded() {}
	
	/**
	 * Called when enabled, and in game.
	 * @param minecraft
	 * @return
	 */
	public boolean OnTickInGame(Minecraft minecraft) {
		return false;
	}
	
	/**
	 * Called when enabled, and a GUI is open.
	 * @param minecraft
	 * @param screenBase
	 * @return
	 */
	public boolean OnTickInGUI(Minecraft minecraft, ScreenBase screenBase) {
		return false;
	}
	
	/**
	 * Used for registering animations for items and blocks.
	 * @param minecraft
	 */
	public void RegisterAnimation(Minecraft minecraft) {}
	
	/**
	 * Renders a block in inventory.
	 * @param blockRenderer
	 * @param blockBase
	 * @param meta
	 * @param modelID
	 */
	public void RenderInvBlock(BlockRenderer blockRenderer, BlockBase blockBase, int meta, int modelID) {}
	
	/**
	 * Renders a block in the world.
	 * @param blockRenderer
	 * @param blockView
	 * @param x
	 * @param y
	 * @param z
	 * @param blockBase
	 * @param modelID
	 * @return
	 */
	public boolean RenderWorldBlock(BlockRenderer blockRenderer, BlockView blockView, int x, int y, int z, BlockBase blockBase, int modelID) {
		return false;
	}
	
	/**
	 * Is called when an item is picked up from crafting result slot.
	 * @param playerBase
	 * @param itemInstance
	 */
	public void TakenFromCrafting(PlayerBase playerBase, ItemInstance itemInstance) {}
	
	/**
	 * Is called when an item is picked up from furnace result slot.
	 * @param playerBase
	 * @param itemInstance
	 */
	public void TakenFromFurnace(PlayerBase playerBase, ItemInstance itemInstance) {}
	
	/**
	 * Is called when an item is picked up from the world.
	 * @param playerBase
	 * @param itemInstance
	 */
	public void OnItemPickup(PlayerBase playerBase, ItemInstance itemInstance) {}
	
	@Override
	public String toString() {
		return getClass().getName() + " " + Version();
	}
	
	/**
	 * Required override that informs users the version of this mod.
	 * @return
	 */
	public abstract String Version();
}
