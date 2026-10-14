package stackedworld.worldgen;

import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import stackedworld.Bands;
import stackedworld.NetherBand;

public final class NetherColumnBiomes {
	private NetherColumnBiomes() {
	}

	public static void correct(ServerLevel level, LevelChunk chunk) {
		if (!NetherBand.isColumn(level)) {
			return;
		}

		BiomeResolver resolver = level.uncachedBiomeResolver();
		int quartMinX = QuartPos.fromBlock(chunk.getPos().getMinBlockX());
		int quartMinZ = QuartPos.fromBlock(chunk.getPos().getMinBlockZ());
		int netherTopSection = SectionPos.blockToSectionCoord(Bands.OVERWORLD_FLOOR - 1);
		boolean changed = false;

		for (int sectionY = chunk.getMinSectionY(); sectionY <= netherTopSection; sectionY++) {
			LevelChunkSection section = chunk.getSection(chunk.getSectionIndexFromSectionY(sectionY));
			if (section == null || stacked$isNether(section)) {
				continue;
			}
			section.fillBiomesFromNoise(resolver, quartMinX, QuartPos.fromSection(sectionY), quartMinZ);
			changed = true;
		}

		if (changed) {
			chunk.markUnsaved();
		}
	}

	private static boolean stacked$isNether(LevelChunkSection section) {
		AtomicBoolean nether = new AtomicBoolean(true);
		section.getBiomes().getAll(biome -> {
			if (!biome.is(BiomeTags.IS_NETHER)) {
				nether.set(false);
			}
		});
		return nether.get();
	}
}
