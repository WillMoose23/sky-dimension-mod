package stackedworld.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import stackedworld.Bands;

final class SkyUndersideOres {
	private static final Direction[] SIDES = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
	private static final int ROLL = 2000;

	private SkyUndersideOres() {
	}

	static boolean decorate(WorldGenLevel level, RandomSource random, BlockPos origin) {
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		boolean placed = false;

		for (int x = minX; x < minX + 16; x++) {
			for (int z = minZ; z < minZ + 16; z++) {
				for (int y = Bands.SKY_MAX; y >= Bands.SKY_MIN; y--) {
					pos.set(x, y, z);
					if (!isExposedStone(level, pos, level.getBlockState(pos))) {
						continue;
					}
					if (tryPlace(level, random, pos, x, y, z, minX, minZ)) {
						placed = true;
					}
				}
			}
		}

		return placed;
	}

	private static boolean tryPlace(
		WorldGenLevel level,
		RandomSource random,
		BlockPos.MutableBlockPos pos,
		int x,
		int y,
		int z,
		int minX,
		int minZ
	) {
		int roll = random.nextInt(ROLL);
		if (roll < 16) {
			return placeVein(level, random, pos, x, y, z, minX, minZ, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, 2 + random.nextInt(3));
		}
		if (roll < 28) {
			return placeVein(level, random, pos, x, y, z, minX, minZ, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE, 2 + random.nextInt(2));
		}
		if (roll < 36) {
			return placeVein(level, random, pos, x, y, z, minX, minZ, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, 1 + random.nextInt(3));
		}
		if (roll < 42) {
			return placeVein(level, random, pos, x, y, z, minX, minZ, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, 1 + random.nextInt(2));
		}
		if (roll < 48) {
			return placeVein(level, random, pos, x, y, z, minX, minZ, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, 1 + random.nextInt(2));
		}
		if (roll < 52) {
			return placeVein(level, random, pos, x, y, z, minX, minZ, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE, 1);
		}
		if (roll < 54) {
			return placeVein(level, random, pos, x, y, z, minX, minZ, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, 1);
		}
		if (roll < 55) {
			return placeVein(level, random, pos, x, y, z, minX, minZ, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE, 1);
		}
		return false;
	}

	private static boolean placeVein(
		WorldGenLevel level,
		RandomSource random,
		BlockPos.MutableBlockPos pos,
		int x,
		int y,
		int z,
		int minX,
		int minZ,
		Block stoneOre,
		Block deepslateOre,
		int size
	) {
		if (!placeOne(level, pos, x, y, z, stoneOre, deepslateOre)) {
			return false;
		}
		if (size <= 1) {
			return true;
		}

		int placedCount = 1;
		int offset = random.nextInt(SIDES.length);
		for (int i = 0; i < SIDES.length && placedCount < size; i++) {
			Direction side = SIDES[(offset + i) % SIDES.length];
			int nextX = x + side.getStepX();
			int nextZ = z + side.getStepZ();
			if (nextX < minX || nextX >= minX + 16 || nextZ < minZ || nextZ >= minZ + 16) {
				continue;
			}
			if (placeOne(level, pos, nextX, y, nextZ, stoneOre, deepslateOre)) {
				placedCount++;
			}
		}
		return true;
	}

	private static boolean placeOne(
		WorldGenLevel level,
		BlockPos.MutableBlockPos pos,
		int x,
		int y,
		int z,
		Block stoneOre,
		Block deepslateOre
	) {
		if (y < Bands.SKY_MIN || y > Bands.SKY_MAX) {
			return false;
		}
		pos.set(x, y, z);
		BlockState host = level.getBlockState(pos);
		if (!isExposedStone(level, pos, host)) {
			return false;
		}
		Block ore = host.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES) ? deepslateOre : stoneOre;
		pos.set(x, y, z);
		level.setBlock(pos, ore.defaultBlockState(), 2);
		return true;
	}

	private static boolean isExposedStone(WorldGenLevel level, BlockPos.MutableBlockPos pos, BlockState state) {
		if (!state.is(BlockTags.BASE_STONE_OVERWORLD)) {
			return false;
		}
		int y = pos.getY();
		if (y < Bands.SKY_MIN || y > Bands.SKY_MAX) {
			return false;
		}

		int belowY = y - 1;
		pos.setY(belowY);
		BlockState below = level.getBlockState(pos);
		pos.setY(y);
		if (below.isAir()) {
			return true;
		}
		return belowY >= Bands.SKY_MIN && below.is(Blocks.WATER);
	}
}
