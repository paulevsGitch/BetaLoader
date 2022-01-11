package paulevs.betaloader.rendering;

public class BlockRendererData {
	public static boolean cfgGrassFix; // Equal to field_67 in BlockRenderer
	public static float[][] redstoneColors;
	
	static {
		cfgGrassFix = true;
		redstoneColors = new float[16][];
		for (int i = 0; i < redstoneColors.length; ++i) {
			float delta = i / 15.0f;
			final float r = delta * 0.6f + 0.4f;
			if (i == 0) {
				delta = 0.0f;
			}
			float g = delta * delta * 0.7f - 0.5f;
			float b = delta * delta * 0.6f - 0.7f;
			if (g < 0.0f) {
				g = 0.0f;
			}
			if (b < 0.0f) {
				b = 0.0f;
			}
			redstoneColors[i] = new float[] { r, g, b };
		}
	}
}
