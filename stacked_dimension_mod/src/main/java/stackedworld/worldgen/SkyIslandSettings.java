package stackedworld.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SkyIslandSettings(float landFrequency, float landSize, float oceanFrequency, float oceanSize) {
	public static final SkyIslandSettings DEFAULT = new SkyIslandSettings(0.35F, 0.55F, 0.90F, 1.0F);

	public static final Codec<SkyIslandSettings> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
				Codec.floatRange(0.15F, 0.70F).optionalFieldOf("land_frequency", 0.35F).forGetter(SkyIslandSettings::landFrequency),
				Codec.floatRange(0.40F, 0.80F).optionalFieldOf("land_size", 0.55F).forGetter(SkyIslandSettings::landSize),
				Codec.floatRange(0.50F, 1.20F).optionalFieldOf("ocean_frequency", 0.90F).forGetter(SkyIslandSettings::oceanFrequency),
				Codec.floatRange(0.60F, 1.20F).optionalFieldOf("ocean_size", 1.0F).forGetter(SkyIslandSettings::oceanSize)
			)
			.apply(instance, SkyIslandSettings::new)
	);

	public float landAmplitude() {
		return this.landFrequency / DEFAULT.landFrequency();
	}

	public double landXzScale() {
		return (double) DEFAULT.landSize() / (double) this.landSize();
	}

	public float oceanAmplitude() {
		return this.oceanFrequency / DEFAULT.oceanFrequency();
	}

	public double oceanXzScale() {
		return (double) DEFAULT.oceanSize() / (double) this.oceanSize();
	}
}
