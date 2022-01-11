package paulevs.betaloader.rendering;

public interface IBlockRenderer {
	/**
	 * Restores function from original ModLoader. Used to set custom Redstone colors.
	 * @param colors float array of arrays of colors. First index is in range [0 - 15], which is
	 * redstone meta. Second index is in range [0 - 2] and stores color values in R G B format (in same order).
	 */
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
