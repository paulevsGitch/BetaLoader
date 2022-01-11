package paulevs.betaloader.utilities;

public interface IBlockRenderer {
	static void setRedstoneColors(float[][] colors) {
		if (colors.length != 16) {
			throw new IllegalArgumentException("Must be 16 colors.");
		}
		for (int v1 = 0; v1 < colors.length; ++v1) {
			if (colors[v1].length != 3) {
				throw new IllegalArgumentException("Must be 3 channels in a color.");
			}
		}
		BlockRendererData.redstoneColors = colors;
	}
}
