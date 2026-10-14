package stackedworld.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import stackedworld.Bands;

public record SkySurfaceFeature() implements Feature {
	public static final MapCodec<SkySurfaceFeature> CODEC = MapCodec.unit(SkySurfaceFeature::new);

	@Override
	public MapCodec<SkySurfaceFeature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
		if (level.getMinY() != Bands.MIN_Y) {
			return false;
		}

		boolean placed = SkyUndersideOres.decorate(level, random, origin);
		int minX = origin.getX() & ~15;
		int minZ = origin.getZ() & ~15;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (int x = minX; x < minX + 16; x++) {
			for (int z = minZ; z < minZ + 16; z++) {
				boolean aboveAir = true;
				for (int y = Bands.SKY_MAX; y >= Bands.SKY_MIN; y--) {
					pos.set(x, y, z);
					BlockState state = level.getBlockState(pos);
					if (!state.getFluidState().isEmpty() && state.getFluidState().is(Fluids.WATER)) {
						if (state.is(Blocks.WATER)) {
							level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
						} else if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
							level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), 2);
						} else {
							level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
						}
						aboveAir = true;
						continue;
					}
					if (state.isAir() || !state.getFluidState().isEmpty()) {
						aboveAir = true;
						continue;
					}
					if (!aboveAir || !isTerrain(state)) {
						aboveAir = false;
						continue;
					}

					aboveAir = false;
					Holder<Biome> biome = level.getBiome(pos);
					cover(level, pos, biome);
					if (random.nextInt(14) == 0) {
						placeCoverFeature(level, chunkGenerator, random, pos, treeFor(biome));
					} else if (random.nextInt(5) == 0 && level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
						pos.move(0, 1, 0);
						if (level.getBlockState(pos).isAir()) {
							level.setBlock(pos, Blocks.SHORT_GRASS.defaultBlockState(), 2);
						}
						pos.move(0, -1, 0);
					}
					placed = true;
				}
			}
		}

		return placed;
	}

	private static void cover(WorldGenLevel level, BlockPos.MutableBlockPos surface, Holder<Biome> biome) {
		BlockState top;
		BlockState fill;
		if (biome.is(BiomeTags.IS_BADLANDS)) {
			top = Blocks.RED_SAND.defaultBlockState();
			fill = Blocks.RED_SAND.defaultBlockState();
		} else if (biome.is(Biomes.DESERT) && !biome.is(BiomeTags.IS_OCEAN) && !biome.is(BiomeTags.IS_RIVER)) {
			top = Blocks.SAND.defaultBlockState();
			fill = Blocks.SANDSTONE.defaultBlockState();
		} else if (biome.is(Biomes.MUSHROOM_FIELDS)) {
			top = Blocks.MYCELIUM.defaultBlockState();
			fill = Blocks.DIRT.defaultBlockState();
		} else {
			top = Blocks.GRASS_BLOCK.defaultBlockState();
			fill = Blocks.DIRT.defaultBlockState();
		}

		level.setBlock(surface, top, 2);
		for (int depth = 1; depth <= 3; depth++) {
			surface.move(0, -1, 0);
			if (!isTerrain(level.getBlockState(surface))) {
				surface.move(0, depth, 0);
				return;
			}
			level.setBlock(surface, depth == 1 && top.is(Blocks.GRASS_BLOCK) ? Blocks.DIRT.defaultBlockState() : fill, 2);
		}
		surface.move(0, 3, 0);

		if (biome.value().coldEnoughToSnow(surface, level.getSeaLevel()) && top.is(Blocks.GRASS_BLOCK)) {
			BlockPos above = surface.above();
			if (level.getBlockState(above).isAir()) {
				level.setBlock(above, Blocks.SNOW.defaultBlockState(), 2);
			}
		}
	}

	private static void placeCoverFeature(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos surface, String featurePath) {
		Registry<Feature> features = level.registryAccess().lookupOrThrow(Registries.FEATURE);
		ResourceKey<Feature> key = ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath("minecraft", featurePath));
		features.get(key).ifPresent(holder -> holder.value().place(level, generator, random, surface));
	}

	private static String treeFor(Holder<Biome> biome) {
		if (biome.is(BiomeTags.IS_BADLANDS)) {
			return "dead_bush";
		}
		if (biome.is(Biomes.DESERT)) {
			return "cactus";
		}
		if (biome.is(BiomeTags.IS_TAIGA)) {
			return "spruce";
		}
		if (biome.is(BiomeTags.IS_JUNGLE)) {
			return "jungle_tree";
		}
		if (biome.is(BiomeTags.IS_SAVANNA)) {
			return "acacia";
		}
		if (biome.is(Biomes.BIRCH_FOREST) || biome.is(Biomes.OLD_GROWTH_BIRCH_FOREST)) {
			return "birch";
		}
		if (biome.is(Biomes.CHERRY_GROVE)) {
			return "cherry";
		}
		if (biome.is(Biomes.DARK_FOREST)) {
			return "dark_oak";
		}
		if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) {
			return "swamp_oak";
		}
		return "oak";
	}

	private static boolean isTerrain(BlockState state) {
		return state.is(Blocks.STONE)
			|| state.is(Blocks.DIRT)
			|| state.is(Blocks.GRASS_BLOCK)
			|| state.is(Blocks.DEEPSLATE)
			|| state.is(Blocks.SAND)
			|| state.is(Blocks.RED_SAND)
			|| state.is(Blocks.SANDSTONE)
			|| state.is(Blocks.GRAVEL)
			|| state.is(Blocks.TUFF)
			|| state.is(Blocks.MYCELIUM)
			|| state.is(BlockTags.BASE_STONE_OVERWORLD);
	}
}
