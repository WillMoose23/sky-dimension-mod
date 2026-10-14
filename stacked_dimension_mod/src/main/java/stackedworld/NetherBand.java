package stackedworld;

import net.minecraft.world.level.LevelHeightAccessor;
import stackedworld.lighting.StackedSkylightBands;

public final class NetherBand {
	public static final int VANILLA_HEIGHT = 128;
	public static final int VANILLA_SEA_LEVEL = 32;
	public static final int MONSTER_LIGHT = 7;
	/**
	 * Client-only floor for block light in the nether band. Level 4 matches the
	 * brightness of vanilla nether ambient light under the overworld lightmap.
	 * Stored light and mob checks stay at the real value.
	 */
	public static final int VISUAL_BLOCK_LIGHT = 4;

	private NetherBand() {
	}

	public static boolean isColumn(LevelHeightAccessor level) {
		return StackedSkylightBands.isStackedColumn(level);
	}

	public static boolean contains(LevelHeightAccessor level, double y) {
		return isColumn(level) && y >= Bands.MIN_Y && y < Bands.OVERWORLD_FLOOR;
	}

	public static int shiftVanillaY(int vanillaY) {
		return vanillaY + Bands.MIN_Y;
	}
}
