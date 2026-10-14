package stackedworld.worldgen;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.op.RangeChoiceFunction;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import stackedworld.Bands;

public final class SkyDensity {
	public static final float OCEAN_CONTINENTS = -0.11F;

	private SkyDensity() {
	}

	public static NoiseGeneratorSettings apply(NoiseGeneratorSettings settings, SkyIslandSettings islands) {
		NoiseRouter router = settings.noiseRouter();
		DensityFunction tuned = retargetSky(router.finalDensity(), router.continents(), islands);
		if (tuned == router.finalDensity()) {
			return settings;
		}

		NoiseRouter patched = new NoiseRouter(
			router.temperature(),
			router.vegetation(),
			router.continents(),
			router.erosion(),
			router.depth(),
			router.ridges(),
			router.chunkSurfaceLevel(),
			tuned
		);
		return new NoiseGeneratorSettings(
			settings.noiseSettings(),
			settings.defaultBlock(),
			settings.defaultFluid(),
			patched,
			settings.materialRule(),
			settings.spawnTarget(),
			settings.seaLevel(),
			settings.disableMobGeneration(),
			settings.aquifers(),
			settings.useLegacyRandomSource(),
			settings.debugFunctions()
		);
	}

	private static DensityFunction retargetSky(DensityFunction finalDensity, DensityFunction continents, SkyIslandSettings islands) {
		if (!(finalDensity instanceof RangeChoiceFunction overworldBand)) {
			return finalDensity;
		}
		if (!(overworldBand.whenOutOfRange() instanceof RangeChoiceFunction lowerBand)) {
			return finalDensity;
		}

		DensityFunction sky = lowerBand.whenOutOfRange();
		DensityFunction land = tune(sky, islands.landAmplitude(), islands.landXzScale());
		DensityFunction ocean = tune(sky, islands.oceanAmplitude(), islands.oceanXzScale());
		if (land == sky && ocean == sky) {
			return finalDensity;
		}

		DensityFunction split = DensityFunctions.rangeChoice(continents, -1.0E6F, OCEAN_CONTINENTS, ocean, land);
		return DensityFunctions.rangeChoice(
			overworldBand.input(),
			overworldBand.minInclusive(),
			overworldBand.maxExclusive(),
			overworldBand.whenInRange(),
			DensityFunctions.rangeChoice(lowerBand.input(), lowerBand.minInclusive(), lowerBand.maxExclusive(), lowerBand.whenInRange(), split)
		);
	}

	// Amplitude and horizontal scale are applied to the blended noise, before the Aether offset.
	// Scaling the finished density would leave the solid mask unchanged.
	private static DensityFunction tune(DensityFunction sky, float amplitude, double xzScaleFactor) {
		if (amplitude == 1.0F && xzScaleFactor == 1.0D) {
			return sky;
		}
		return sky.rewriteChildren(new IslandScale(amplitude, xzScaleFactor));
	}

	private record IslandScale(float amplitude, double xzScaleFactor) implements DfRewriteRule {
		@Override
		public DensityFunction rewrite(DensityFunction function) {
			if (!(function instanceof BlendedNoise noise)) {
				return function.rewriteChildren(this);
			}

			BlendedNoise scaled = new BlendedNoise(
				noise.xzScale() * this.xzScaleFactor,
				noise.yScale(),
				noise.xzFactor(),
				noise.yFactor(),
				noise.smearScaleMultiplier()
			);
			if (this.amplitude == 1.0F) {
				return scaled;
			}
			return DensityFunctions.mul(scaled, DensityFunctions.constant(this.amplitude));
		}
	}

	public static boolean isSky(int y) {
		return y >= Bands.SKY_MIN;
	}
}
