package stackedworld;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import stackedworld.worldgen.SkySurfaceFeature;
import stackedworld.worldgen.StackedBiomeSource;
import stackedworld.worldgen.StackedNoiseChunkGenerator;

public final class StackedDimensionMod implements ModInitializer {
	public static final String MOD_ID = "stackedworld";
	public static final ResourceKey<PlacedFeature> SKY_SURFACE = ResourceKey.create(
		Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(MOD_ID, "sky_surface")
	);
	public static final ResourceKey<WorldPreset> PRESET = ResourceKey.create(
		Registries.WORLD_PRESET, Identifier.fromNamespaceAndPath(MOD_ID, "stacked_dimension")
	);
	public static final ResourceKey<NoiseGeneratorSettings> NOISE_SETTINGS = ResourceKey.create(
		Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath(MOD_ID, "overworld")
	);

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.BIOME_SOURCE, Identifier.fromNamespaceAndPath(MOD_ID, "stacked"), StackedBiomeSource.CODEC);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "sky_surface"), SkySurfaceFeature.CODEC);
		Registry.register(BuiltInRegistries.CHUNK_GENERATOR, Identifier.fromNamespaceAndPath(MOD_ID, "stacked_noise"), StackedNoiseChunkGenerator.CODEC);
		BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD), GenerationStep.Decoration.TOP_LAYER_MODIFICATION, SKY_SURFACE);
	}
}
