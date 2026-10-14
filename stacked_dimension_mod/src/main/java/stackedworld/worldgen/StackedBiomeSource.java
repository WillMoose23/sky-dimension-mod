package stackedworld.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import stackedworld.Bands;

public final class StackedBiomeSource extends BiomeSource {
	public static final MapCodec<StackedBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
				BiomeSource.CODEC.fieldOf("overworld").forGetter(source -> source.overworld),
				BiomeSource.CODEC.fieldOf("nether").forGetter(source -> source.nether)
			)
			.apply(instance, StackedBiomeSource::new)
	);

	private final BiomeSource overworld;
	private final BiomeSource nether;

	public StackedBiomeSource(BiomeSource overworld, BiomeSource nether) {
		this.overworld = overworld;
		this.nether = nether;
	}

	@Override
	protected MapCodec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return Stream.concat(this.overworld.possibleBiomes().stream(), this.nether.possibleBiomes().stream());
	}

	@Override
	public BiomeResolver createResolver(Climate.Sampler sampler) {
		BiomeResolver overworldResolver = this.overworld.createResolver(sampler);
		BiomeResolver netherResolver = this.nether.createResolver(sampler);
		return (quartX, quartY, quartZ) -> {
			int blockY = QuartPos.toBlock(quartY);
			if (blockY < Bands.OVERWORLD_FLOOR) {
				return netherResolver.getNoiseBiome(quartX, quartY, quartZ);
			}
			if (blockY >= Bands.SKY_MIN) {
				return overworldResolver.getNoiseBiome(quartX + 8192, QuartPos.fromBlock(400), quartZ - 8192);
			}
			return overworldResolver.getNoiseBiome(quartX, quartY, quartZ);
		};
	}
}
