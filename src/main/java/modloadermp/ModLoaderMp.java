package modloadermp;

import java.util.List;
import modloader.BaseMod;
import modloader.ModLoader;
import net.minecraft.client.gui.screen.ScreenBase;
import net.minecraft.client.level.ClientLevel;
import net.minecraft.entity.EntityBase;
import net.minecraft.level.Level;
import net.minecraft.packet.AbstractPacket;
import net.minecraft.packet.play.OpenContainer0x64S2CPacket;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ModLoaderMp {
	public static final String NAME = "ModLoaderMP";
	public static final String VERSION = "Beta 1.7.3 unofficial";
	private static boolean hasInit = false;
	private static boolean packet230Received = false;
	private static Map<Integer, NetClientHandlerEntity> netClientHandlerEntityMap = new HashMap<>();
	private static Map<Integer, BaseModMp> guiModMap = new HashMap<>();
	
	public static void Init() {
		if (!ModLoaderMp.hasInit) {
			init();
		}
	}
	
	public static void HandleAllPackets(final Packet230ModLoader packet) {
		if (!ModLoaderMp.hasInit) {
			init();
		}
		ModLoaderMp.packet230Received = true;
		if (packet.modId == NAME.hashCode()) {
			switch (packet.packetType) {
				case 0: {
					handleModCheck(packet);
					break;
				}
				case 1: {
					handleTileEntityPacket(packet);
					break;
				}
			}
		}
		else if (packet.modId == "Spawn".hashCode()) {
			final NetClientHandlerEntity netclienthandlerentity = HandleNetClientHandlerEntities(packet.packetType);
			if (netclienthandlerentity != null && ISpawnable.class.isAssignableFrom(netclienthandlerentity.entityClass)) {
				try {
					final EntityBase entity = netclienthandlerentity.entityClass.getConstructor(Level.class).newInstance(ModLoader.getMinecraftInstance().level);
					((ISpawnable) entity).spawn(packet);
					((ClientLevel) ModLoader.getMinecraftInstance().level).method_1495(entity.entityId, entity);
				}
				catch (Exception e) {
					ModLoader.getLogger().throwing("ModLoader", "handleCustomSpawn", e);
					ModLoader.ThrowException(String.format("Error initializing entity of type %s.", packet.packetType), e);
				}
			}
		}
		else {
			for (BaseMod basemod : ModLoader.getLoadedMods()) {
				if (basemod instanceof BaseModMp) {
					final BaseModMp basemodmp = (BaseModMp) basemod;
					if (basemodmp.getId() == packet.modId) {
						basemodmp.HandlePacket(packet);
						break;
					}
				}
			}
		}
	}
	
	public static NetClientHandlerEntity HandleNetClientHandlerEntities(final int aInteger1) {
		if (!ModLoaderMp.hasInit) {
			init();
		}
		if (ModLoaderMp.netClientHandlerEntityMap.containsKey(aInteger1)) {
			return ModLoaderMp.netClientHandlerEntityMap.get(aInteger1);
		}
		return null;
	}
	
	public static void SendPacket(final BaseModMp packet, final Packet230ModLoader v1) {
		if (!ModLoaderMp.hasInit) {
			init();
		}
		if (packet == null) {
			final IllegalArgumentException e = new IllegalArgumentException("baseModMp cannot be null.");
			ModLoader.getLogger().throwing(NAME, "SendPacket", e);
			ModLoader.ThrowException("baseModMp cannot be null.", e);
		}
		else {
			v1.modId = packet.getId();
			sendPacket(v1);
		}
	}
	
	public static void RegisterGUI(final BaseModMp aBaseModMp1, final int aInteger2) {
		if (!ModLoaderMp.hasInit) {
			init();
		}
		if (ModLoaderMp.guiModMap.containsKey(aInteger2)) {
			Log("RegisterGUI error: inventoryType already registered.");
		}
		else {
			ModLoaderMp.guiModMap.put(aInteger2, aBaseModMp1);
		}
	}
	
	public static void HandleGUI(final OpenContainer0x64S2CPacket packet) {
		if (!ModLoaderMp.hasInit) {
			init();
		}
		final BaseModMp basemodmp = ModLoaderMp.guiModMap.get(packet.inventoryType);
		final ScreenBase guiScreen = basemodmp.HandleGUI(packet.inventoryType);
		if (guiScreen != null) {
			ModLoader.OpenGUI(ModLoader.getMinecraftInstance().player, guiScreen);
			ModLoader.getMinecraftInstance().player.container.currentContainerId = packet.containerId;
		}
	}
	
	public static void RegisterNetClientHandlerEntity(final Class<? extends EntityBase> aClass1, final int aInteger2) {
		RegisterNetClientHandlerEntity(aClass1, false, aInteger2);
	}
	
	public static void RegisterNetClientHandlerEntity(final Class<? extends EntityBase> aClass1, final boolean aBoolean2, int aInteger3) {
		if (!ModLoaderMp.hasInit) {
			init();
		}
		if (aInteger3 > 255) {
			Log("RegisterNetClientHandlerEntity error: entityId cannot be greater than 255.");
		}
		else if (ModLoaderMp.netClientHandlerEntityMap.containsKey(aInteger3)) {
			Log("RegisterNetClientHandlerEntity error: entityId already registered.");
		}
		else {
			if (aInteger3 > 127) {
				aInteger3 -= 256;
			}
			ModLoaderMp.netClientHandlerEntityMap.put(aInteger3, new NetClientHandlerEntity(aClass1, aBoolean2));
		}
	}
	
	public static void SendKey(final BaseModMp v1, final int v2) {
		if (!ModLoaderMp.hasInit) {
			init();
		}
		if (v1 == null) {
			final IllegalArgumentException e = new IllegalArgumentException("baseModMp cannot be null.");
			ModLoader.getLogger().throwing(NAME, "SendKey", e);
			ModLoader.ThrowException("baseModMp cannot be null.", e);
		}
		else {
			final Packet230ModLoader packet230modloader = new Packet230ModLoader();
			packet230modloader.modId = NAME.hashCode();
			packet230modloader.packetType = 1;
			packet230modloader.dataInt = new int[] {v1.getId(), v2};
			sendPacket(packet230modloader);
		}
	}
	
	public static void Log(final String aString1) {
		System.out.println(aString1);
		ModLoader.getLogger().fine(aString1);
	}
	
	private static void init() {
		ModLoaderMp.hasInit = true;
		try {
			Method method = null;
			String[] possibleMethodNames = {"a", "addIdClassMapping", "register", "method_800", "method_1177"};
			for (String methodName : possibleMethodNames) {
				try {
					method = AbstractPacket.class.getDeclaredMethod(
							methodName,
							Integer.TYPE, Boolean.TYPE, Boolean.TYPE, Class.class
					);
					break;
				}
				catch (NoSuchMethodException ignored) {
				}
			}
			if (method == null) {
				throw new NoSuchMethodException("AbstractPacket#register method cannot be found.");
			}
			method.setAccessible(true);
			method.invoke(null, 230, true, true, Packet230ModLoader.class);
		}
		catch (IllegalAccessException | SecurityException | NoSuchMethodException | InvocationTargetException | IllegalArgumentException e) {
			ModLoader.getLogger().throwing(NAME, "init", e);
			ModLoader.ThrowException("An impossible error has occurred!", e);
		}
		Log(NAME + " " + VERSION + " Initialized");
	}
	
	private static void handleModCheck(final Packet230ModLoader packet) {
		List<BaseMod> mods = ModLoader.getLoadedMods();
		final Packet230ModLoader packet2 = new Packet230ModLoader();
		packet2.modId = NAME.hashCode();
		packet2.packetType = 0;
		packet2.dataString = new String[mods.size()];
		for (int i = 0; i < mods.size(); ++i) {
			packet2.dataString[i] = mods.get(i).toString();
		}
		sendPacket(packet2);
	}
	
	private static void handleTileEntityPacket(final Packet230ModLoader packet) {
		if (packet.dataInt == null || packet.dataInt.length < 5) {
			Log("Bad TileEntityPacket received.");
		}
		else {
			final int i = packet.dataInt[0];
			final int j = packet.dataInt[1];
			final int k = packet.dataInt[2];
			final int l = packet.dataInt[3];
			final int i2 = packet.dataInt[4];
			final int[] ai = new int[packet.dataInt.length - 5];
			System.arraycopy(packet.dataInt, 5, ai, 0, packet.dataInt.length - 5);
			final float[] af = packet.dataFloat;
			final String[] as = packet.dataString;
			for (BaseMod basemod : ModLoader.getLoadedMods()) {
				if (basemod instanceof BaseModMp) {
					final BaseModMp basemodmp = (BaseModMp) basemod;
					if (basemodmp.getId() == i) {
						basemodmp.HandleTileEntityPacket(j, k, l, i2, ai, af, as);
						break;
					}
				}
			}
		}
	}
	
	private static void sendPacket(final Packet230ModLoader packet) {
		if (packet230Received && ModLoader.getMinecraftInstance().level != null && ModLoader.getMinecraftInstance().level.isServerSide) {
			ModLoader.getMinecraftInstance().getNetworkHandler().sendPacket(packet);
		}
	}
	
	public static BaseModMp GetModInstance(final Class<? extends BaseModMp> v1) {
		for (BaseMod basemod : ModLoader.getLoadedMods()) {
			if (basemod instanceof BaseModMp) {
				final BaseModMp basemodmp = (BaseModMp) basemod;
				if (v1.isInstance(basemodmp)) {
					return basemodmp;
				}
			}
		}
		return null;
	}

}
