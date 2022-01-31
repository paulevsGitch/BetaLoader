package paulevs.betaloader.rendering;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.render.TextureBinder;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlas;
import net.modificationstation.stationapi.api.client.texture.atlas.Atlases;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class BLTexturesManager {
	private static final Queue<Character> VANILLA_ITEMS = new LinkedList<>();
	private static final Queue<Character> VANILLA_BLOCKS = new LinkedList<>();
	private static final Map<Character, String> REGISTERED_BLOCKS = Maps.newHashMap();
	private static final Map<Character, String> REGISTERED_ITEMS = Maps.newHashMap();
	private static final List<TextureBinder> ANIMATIONS = Lists.newArrayList();
	private static final BufferedImage EMPTY = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
	
	public static int getBlockTexture(String name) {
		Character c = VANILLA_BLOCKS.poll();
		if (c != null) {
			REGISTERED_BLOCKS.put(c, name);
			return c.charValue();
		}
		throw new RuntimeException("Impossible to register block texture, no more free space in vanilla atlas!");
	}
	
	public static int getItemTexture(String name) {
		Character c = VANILLA_ITEMS.poll();
		if (c != null) {
			REGISTERED_ITEMS.put(c, name);
			return c.charValue();
		}
		throw new RuntimeException("Impossible to register block texture, no more free space in vanilla atlas!");
	}
	
	public static void setBlockTexture(int index, String name) {
		REGISTERED_BLOCKS.put((char) index, name);
	}
	
	public static void setItemTexture(int index, String name) {
		REGISTERED_ITEMS.put((char) index, name);
	}
	
	public static void addAnimation(TextureBinder animation) {
		ANIMATIONS.add(animation);
	}
	
	public static void onTextureRegister() {
		addTexturesToAtlas(Atlases.getTerrain(), REGISTERED_BLOCKS);
		addTexturesToAtlas(Atlases.getGuiItems(), REGISTERED_ITEMS);
	}
	
	private static void addTexturesToAtlas(Atlas atlas, Map<Character, String> textures) {
		atlas.bindAtlas();
		int[] rgb = new int[256];
		ByteBuffer buffer = ByteBuffer.allocateDirect(rgb.length << 2);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		textures.forEach((id, path) -> {
			BufferedImage texture = loadTexture(path);
			if (texture.getWidth() != 16 || texture.getHeight() != 16) {
				System.out.println("Texture " + path + " have wrong dimensions: " + texture.getWidth() + " " + texture.getHeight());
				BufferedImage rescaled = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
				Graphics g = rescaled.getGraphics();
				g.drawImage(texture, 0, 0, null);
				texture = rescaled;
			}
			texture.getRGB(0, 0, 16, 16, rgb, 0, 16);
			for (int i = 0; i < 256; i++) {
				buffer.putInt(rgb[i]);
			}
			buffer.rewind();
			int px = (id & 15) << 4;
			int py = id & 0xFFFFFFF0;
			GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, px, py, 16, 16, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buffer);
		});
	}
	
	private static BufferedImage loadTexture(String path) {
		InputStream stream = BLTexturesManager.class.getResourceAsStream(path);
		if (stream != null) {
			try {
				BufferedImage img = ImageIO.read(stream);
				stream.close();
				return img;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return EMPTY;
	}
	
	static {
		String availableItems = "1111111111111111111111111111111111111101111111011111111111111001" +
								"1111111111111111111111111110111111111001100000111111100000000011" +
								"1111100110000011000000010000001100000001000000110000000000000011" +
								"0000000000000000000000000000000000000000000000001100000000000000";
		String availableBlock = "1111111111111111111111111111110111111111111111111111110111111111" +
								"1111111111110001111110111111111111110011111111101111111111111000" +
								"1111111100001000111101111000000011111100000000001111110000000000" +
								"1111000000000111111000000000001101000000000001111111111111000011";
		
		for (char i = 0; i < 256; i++) {
			if (availableItems.charAt(i) == '0') {
				VANILLA_ITEMS.offer(i);
			}
			if (availableBlock.charAt(i) == '0') {
				VANILLA_BLOCKS.offer(i);
			}
		}
		
		Graphics g = EMPTY.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 8, 8);
		g.fillRect(8, 8, 8, 8);
		g.setColor(Color.MAGENTA);
		g.fillRect(0, 8, 8, 8);
		g.fillRect(8, 0, 8, 8);
	}
}
