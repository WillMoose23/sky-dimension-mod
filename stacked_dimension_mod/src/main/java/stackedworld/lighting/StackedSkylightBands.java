package stackedworld.lighting;

import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import stackedworld.Bands;

public final class StackedSkylightBands {
	public static final int COLUMN_HEIGHT = Bands.SKY_MAX - Bands.MIN_Y + 1;

	private StackedSkylightBands() {
	}

	public static boolean matchesColumn(int minY, int height) {
		return minY == Bands.MIN_Y && height == COLUMN_HEIGHT;
	}

	public static boolean isStackedColumn(LevelHeightAccessor level) {
		return level != null && matchesColumn(level.getMinY(), level.getHeight());
	}

	/**
	 * The upper block of this downward edge is still in the sky band.
	 * That includes the floor between y=321 and y=320 and every island roof above it.
	 * Those edges must not become the overworld's skylight roof.
	 */
	public static boolean isSkyRoofEdge(int topY, int bottomY) {
		return topY >= Bands.SKY_MIN && bottomY < topY;
	}

	public static boolean descendsIntoOverworld(int fromY, int toY) {
		return fromY >= Bands.SKY_MIN && toY < Bands.SKY_MIN;
	}

	/**
	 * The two cells sit on opposite sides of the sky floor, so light must not cross between them.
	 */
	public static boolean crossesSkyFloor(int fromY, int toY) {
		return (fromY >= Bands.SKY_MIN) != (toY >= Bands.SKY_MIN);
	}

	public static boolean isEdgeOccluded(BlockState topState, BlockState bottomState) {
		if (bottomState.getLightDampening() != 0) {
			return true;
		}

		VoxelShape topShape = LightEngine.getOcclusionShape(topState, Direction.DOWN);
		VoxelShape bottomShape = LightEngine.getOcclusionShape(bottomState, Direction.UP);
		return Shapes.faceShapeOccludes(topShape, bottomShape);
	}
}
