package stackedworld.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import org.jspecify.annotations.Nullable;

public final class StackedNoiseChunkGenerator extends ChunkGenerator {
	public static final MapCodec<StackedNoiseChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				BiomeSource.CODEC.fieldOf("biome_source").forGetter(StackedNoiseChunkGenerator::getBiomeSource),
				NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(StackedNoiseChunkGenerator::storedSettings),
				SkyIslandSettings.CODEC.optionalFieldOf("sky_islands", SkyIslandSettings.DEFAULT).forGetter(StackedNoiseChunkGenerator::skyIslands)
			)
			.apply(instance, StackedNoiseChunkGenerator::new)
	);

	private final Holder<NoiseGeneratorSettings> storedSettings;
	private final SkyIslandSettings skyIslands;
	private volatile NoiseBasedChunkGenerator delegate;

	public StackedNoiseChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings, SkyIslandSettings skyIslands) {
		super(biomeSource);
		this.storedSettings = settings;
		this.skyIslands = skyIslands;
	}

	public Holder<NoiseGeneratorSettings> storedSettings() {
		return this.storedSettings;
	}

	public SkyIslandSettings skyIslands() {
		return this.skyIslands;
	}

	public NoiseBasedChunkGenerator delegate() {
		NoiseBasedChunkGenerator existing = this.delegate;
		if (existing != null) {
			return existing;
		}
		synchronized (this) {
			if (this.delegate == null) {
				NoiseGeneratorSettings patched = SkyDensity.apply(this.storedSettings.value(), this.skyIslands);
				this.delegate = new NoiseBasedChunkGenerator(this.getBiomeSource(), Holder.direct(patched));
			}
			return this.delegate;
		}
	}

	@Override
	protected MapCodec<? extends ChunkGenerator> codec() {
		return CODEC;
	}

	@Override
	public CompletableFuture<ChunkAccess> buildTerrain(
		ChunkAccess chunk,
		Blender blender,
		RandomState randomState,
		StructureManager structureManager,
		BiomeManager biomeManager,
		@Nullable WorldGenRegion carverBiomeRegion,
		Set<Holder<Biome>> possibleBiomes
	) {
		return this.delegate.buildTerrain(chunk, blender, randomState, structureManager, biomeManager, carverBiomeRegion, possibleBiomes);
	}

	@Override
	public int getSeaLevel() {
		return this.delegate.getSeaLevel();
	}

	@Override
	public int getMinY() {
		return this.delegate.getMinY();
	}

	@Override
	public int getGenDepth() {
		return this.delegate.getGenDepth();
	}

	@Override
	public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
		return this.delegate.getBaseHeight(x, z, type, heightAccessor, randomState);
	}

	@Override
	public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
		return this.delegate.getBaseColumn(x, z, heightAccessor, randomState);
	}

	@Override
	public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext samplerContext) {
		this.delegate.addDebugScreenInfo(result, randomState, feetPos, samplerContext);
	}

	@Override
	public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
		this.delegate.spawnOriginalMobs(worldGenRegion);
	}

	@Override
	public ChunkPos getOrigin(RandomState randomState) {
		return this.delegate.getOrigin(randomState);
	}
}
