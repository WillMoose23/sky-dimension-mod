package stackedworld.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import stackedworld.Bands;

public final class ColumnSurfaces {
	private ColumnSurfaces() {
	}

	public static int structureSurface(
		NoiseBasedChunkGenerator generator,
		int x,
		int z,
		Heightmap.Types type,
		LevelHeightAccessor heightAccessor,
		RandomState randomState,
		boolean sky
	) {
		NoiseColumn column = generator.getBaseColumn(x, z, heightAccessor, randomState);
		int max = sky ? Bands.SKY_MAX : Bands.OVERWORLD_LIMIT - 1;
		int min = sky ? Bands.SKY_MIN : heightAccessor.getMinY();
		for (int y = max; y >= min; y--) {
			if (type.isOpaque().test(column.getBlock(y))) {
				return y + 1;
			}
		}
		return heightAccessor.getMinY();
	}

	public static int groundHeight(ChunkAccess chunk, Heightmap.Types type, int x, int z) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int y = Bands.OVERWORLD_LIMIT - 1; y >= Bands.OVERWORLD_FLOOR; y--) {
			pos.set(x, y, z);
			BlockState state = chunk.getBlockState(pos);
			if (type.isOpaque().test(state)) {
				return y;
			}
		}
		return Bands.OVERWORLD_FLOOR;
	}
}
