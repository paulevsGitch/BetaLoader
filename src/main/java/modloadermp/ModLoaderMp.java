package modloadermp;

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
	private static boolean hasInit;
	private static boolean packet230Received;
	private static Map<Integer, NetClientHandlerEntity> netClientHandlerEntityMap;
	private static Map<Integer, BaseModMp> guiModMap;
	
	public static void Init() {
		if (!ModLoaderMp.hasInit) {
			init();
		}
	}
	
	public static void HandleAllPackets(final Packet230ModLoader r) {
		if (!ModLoaderMp.hasInit) {
			init();
		}
		ModLoaderMp.packet230Received = true;
		if (r.modId == "ModLoaderMP".hashCode()) {
			switch (r.packetType) {
				case 0: {
					handleModCheck(r);
					break;
				}
				case 1: {
					handleTileEntityPacket(r);
					break;
				}
			}
		}
		else if (r.modId == "Spawn".hashCode()) {
			final NetClientHandlerEntity netclienthandlerentity = HandleNetClientHandlerEntities(r.packetType);
			if (netclienthandlerentity != null && ISpawnable.class.isAssignableFrom(netclienthandlerentity.entityClass)) {
				try {
					final EntityBase entity = netclienthandlerentity.entityClass.getConstructor(Level.class).newInstance(ModLoader.getMinecraftInstance().level);
					((ISpawnable) entity).spawn(r);
					((ClientLevel) ModLoader.getMinecraftInstance().level).method_1495(entity.entityId, entity);
				}
				catch (Exception exception) {
					ModLoader.getLogger().throwing("ModLoader", "handleCustomSpawn", exception);
					ModLoader.ThrowException(String.format("Error initializing entity of type %s.", r.packetType), exception);
				}
			}
		}
		else {
			for (int i = 0; i < ModLoader.getLoadedMods().size(); ++i) {
				final BaseMod basemod = ModLoader.getLoadedMods().get(i);
				if (basemod instanceof BaseModMp) {
					final BaseModMp basemodmp = (BaseModMp) basemod;
					if (basemodmp.getId() == r.modId) {
						basemodmp.HandlePacket(r);
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
	
	public static void SendPacket(final BaseModMp aPacket230ModLoader2, final Packet230ModLoader v1) {
		if (!ModLoaderMp.hasInit) {
			init();
		}
		if (aPacket230ModLoader2 == null) {
			final IllegalArgumentException illegalargumentexception = new IllegalArgumentException("baseModMp cannot be null.");
			ModLoader.getLogger().throwing("ModLoaderMp", "SendPacket", illegalargumentexception);
			ModLoader.ThrowException("baseModMp cannot be null.", illegalargumentexception);
		}
		else {
			v1.modId = aPacket230ModLoader2.getId();
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
		final ScreenBase guiscreen = basemodmp.HandleGUI(packet.inventoryType);
		if (guiscreen != null) {
			ModLoader.OpenGUI(ModLoader.getMinecraftInstance().player, guiscreen);
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
			final IllegalArgumentException illegalargumentexception = new IllegalArgumentException(
				"baseModMp cannot be null.");
			ModLoader.getLogger().throwing("ModLoaderMp", "SendKey", illegalargumentexception);
			ModLoader.ThrowException("baseModMp cannot be null.", illegalargumentexception);
		}
		else {
			final Packet230ModLoader packet230modloader = new Packet230ModLoader();
			packet230modloader.modId = "ModLoaderMP".hashCode();
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
		catch (IllegalAccessException | SecurityException | NoSuchMethodException | InvocationTargetException | IllegalArgumentException illegalaccessexception) {
			ModLoader.getLogger().throwing("ModLoaderMp", "init", illegalaccessexception);
			ModLoader.ThrowException("An impossible error has occurred!", illegalaccessexception);
		}
		Log("ModLoaderMP Beta 1.7.3 unofficial Initialized");
	}
	
	private static void handleModCheck(final Packet230ModLoader packet230modloader) {
		final Packet230ModLoader packet230modloader2 = new Packet230ModLoader();
		packet230modloader2.modId = "ModLoaderMP".hashCode();
		packet230modloader2.packetType = 0;
		packet230modloader2.dataString = new String[ModLoader.getLoadedMods().size()];
		for (int i = 0; i < ModLoader.getLoadedMods().size(); ++i) {
			packet230modloader2.dataString[i] = ModLoader.getLoadedMods().get(i).toString();
		}
		sendPacket(packet230modloader2);
	}
	
	private static void handleTileEntityPacket(final Packet230ModLoader v1) {
		if (v1.dataInt == null || v1.dataInt.length < 5) {
			Log("Bad TileEntityPacket received.");
		}
		else {
			final int i = v1.dataInt[0];
			final int j = v1.dataInt[1];
			final int k = v1.dataInt[2];
			final int l = v1.dataInt[3];
			final int i2 = v1.dataInt[4];
			final int[] ai = new int[v1.dataInt.length - 5];
			System.arraycopy(v1.dataInt, 5, ai, 0, v1.dataInt.length - 5);
			final float[] af = v1.dataFloat;
			final String[] as = v1.dataString;
			for (int j2 = 0; j2 < ModLoader.getLoadedMods().size(); ++j2) {
				final BaseMod basemod = ModLoader.getLoadedMods().get(j2);
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
	
	private static void sendPacket(final Packet230ModLoader aPacket230ModLoader1) {
		if (packet230Received && ModLoader.getMinecraftInstance().level != null && ModLoader.getMinecraftInstance().level.isServerSide) {
			ModLoader.getMinecraftInstance().getNetworkHandler().sendPacket(aPacket230ModLoader1);
		}
	}
	
	public static BaseModMp GetModInstance(final Class v1) {
		for (int i = 0; i < ModLoader.getLoadedMods().size(); ++i) {
			final BaseMod basemod = ModLoader.getLoadedMods().get(i);
			if (basemod instanceof BaseModMp) {
				final BaseModMp basemodmp = (BaseModMp) basemod;
				if (v1.isInstance(basemodmp)) {
					return (BaseModMp) ModLoader.getLoadedMods().get(i);
				}
			}
		}
		return null;
	}
	
	private ModLoaderMp() {
	}
	
	static {
		ModLoaderMp.hasInit = false;
		ModLoaderMp.packet230Received = false;
		ModLoaderMp.netClientHandlerEntityMap = new HashMap<>();
		ModLoaderMp.guiModMap = new HashMap<>();
	}
}
