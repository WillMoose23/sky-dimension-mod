package stackedworld.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import stackedworld.StackedDimensionMod;
import stackedworld.client.gui.CreateStackedWorldScreen;
import stackedworld.worldgen.SkyIslandSettings;
import stackedworld.worldgen.StackedNoiseChunkGenerator;

public final class StackedWorldPresetEditor implements PresetEditor {
	public static final StackedWorldPresetEditor EDITOR = new StackedWorldPresetEditor();

	private StackedWorldPresetEditor() {
	}

	@Override
	public Screen createEditScreen(CreateWorldScreen parent, WorldCreationContext settings) {
		ChunkGenerator overworld = settings.selectedDimensions().overworld();
		SkyIslandSettings current = overworld instanceof StackedNoiseChunkGenerator stacked ? stacked.skyIslands() : SkyIslandSettings.DEFAULT;
		return new CreateStackedWorldScreen(parent, current, updated -> parent.getUiState().updateDimensions((registryAccess, dimensions) -> {
			BiomeSource biomes = dimensions.overworld().getBiomeSource();
			Holder<NoiseGeneratorSettings> noise = registryAccess.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(StackedDimensionMod.NOISE_SETTINGS);
			return dimensions.replaceOverworldGenerator(registryAccess, new StackedNoiseChunkGenerator(biomes, noise, updated));
		}));
	}
}
