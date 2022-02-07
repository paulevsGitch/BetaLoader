package paulevs.betaloader.utilities;

import com.google.common.collect.Maps;
import net.minecraft.block.BlockBase;
import net.minecraft.item.Block;
import net.minecraft.item.ItemBase;
import net.modificationstation.stationapi.api.registry.BlockRegistry;
import net.modificationstation.stationapi.api.registry.Identifier;
import net.modificationstation.stationapi.api.registry.ItemRegistry;
import net.modificationstation.stationapi.api.registry.ModID;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegistryUtil {
	private static final Map<ModID, List<Character>> BLOCKS = Maps.newHashMap();
	private static final Map<ModID, List<Character>> ITEMS = Maps.newHashMap();
	
	public static void addBlock(ModID mod, char id) {
		BLOCKS.computeIfAbsent(mod, i -> new ArrayList<>(8)).add(id);
	}
	
	public static void addItem(ModID mod, char id) {
		ITEMS.computeIfAbsent(mod, i -> new ArrayList<>(8)).add(id);
	}
	
	public static void register() {
		BLOCKS.forEach((mod, blocks) -> {
			blocks.forEach(blockID -> {
				BlockBase block = BlockBase.BY_ID[blockID];
				String name = block.getTranslationKey();
				name = name.substring(name.indexOf('.') + 1).toLowerCase(Locale.ROOT).replace(' ', '_');
				Identifier id = Identifier.of(mod, name);
				BlockRegistry.INSTANCE.register(id, block);
				ItemBase item = ItemBase.byId[blockID];
				if (item == null) {
					item = new Block(blockID - 256);
				}
				ItemRegistry.INSTANCE.register(Identifier.of(mod, name), item);
				System.out.println("Registering block " + (int) blockID + " (" + block + ") as " + id);
			});
		});
		
		ITEMS.forEach((mod, items) -> {
			items.forEach(itemID -> {
				ItemBase item = ItemBase.byId[itemID];
				String name = item.getTranslationKey();
				name = name.substring(name.indexOf('.') + 1).toLowerCase(Locale.ROOT).replace(' ', '_');
				Identifier id = Identifier.of(mod, name);
				ItemRegistry.INSTANCE.register(Identifier.of(mod, name), item);
				System.out.println("Registering item " + (int) itemID + " (" + item + ") as " + id);
			});
		});
	}
}
