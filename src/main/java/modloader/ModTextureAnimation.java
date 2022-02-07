package modloader;

import net.minecraft.client.render.TextureBinder;
import org.lwjgl.opengl.GL11;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ModTextureAnimation extends TextureBinder {
	private final int tickRate;
	private final byte[][] images;
	private int index;
	private int ticks;

	public ModTextureAnimation(final int index, final int renderMode, final BufferedImage image, final int rate) {
		this(index, 1, renderMode, image, rate);
	}
	
	public ModTextureAnimation(final int index, final int textureSize, final int renderMode, BufferedImage image, final int rate) {
		super(index);
		this.index = 0;
		this.textureSize = textureSize;
		this.renderMode = renderMode;
		this.tickRate = rate;
		this.ticks = rate;
		this.bindTexture(ModLoader.getMinecraftInstance().textureManager);
		final int v1 = GL11.glGetTexLevelParameteri(3553, 0, 4096) / 16;
		final int v2 = GL11.glGetTexLevelParameteri(3553, 0, 4097) / 16;
		final int v3 = image.getWidth();
		final int v4 = image.getHeight();
		final int v5 = (int) Math.floor(v4 / v3);
		if (v5 <= 0) {
			throw new IllegalArgumentException("source has no complete images");
		}
		this.images = new byte[v5][];
		if (v3 != v1) {
			final BufferedImage v6 = new BufferedImage(v1, v2 * v5, 6);
			final Graphics2D v7 = v6.createGraphics();
			v7.drawImage(image, 0, 0, v1, v2 * v5, 0, 0, v3, v4, null);
			v7.dispose();
			image = v6;
		}
		for (int v8 = 0; v8 < v5; ++v8) {
			final int[] v9 = new int[v1 * v2];
			image.getRGB(0, v2 * v8, v1, v2, v9, 0, v1);
			this.images[v8] = new byte[v1 * v2 * 4];
			for (int v10 = 0; v10 < v9.length; ++v10) {
				final int v11 = v9[v10] >> 24 & 0xFF;
				final int v12 = v9[v10] >> 16 & 0xFF;
				final int v13 = v9[v10] >> 8 & 0xFF;
				final int v14 = v9[v10] >> 0 & 0xFF;
				this.images[v8][v10 * 4 + 0] = (byte) v12;
				this.images[v8][v10 * 4 + 1] = (byte) v13;
				this.images[v8][v10 * 4 + 2] = (byte) v14;
				this.images[v8][v10 * 4 + 3] = (byte) v11;
			}
		}
	}
	
	public void update() {
		if (this.ticks >= this.tickRate) {
			++this.index;
			if (this.index >= this.images.length) {
				this.index = 0;
			}
			this.grid = this.images[this.index];
			this.ticks = 0;
		}
		++this.ticks;
	}
}
