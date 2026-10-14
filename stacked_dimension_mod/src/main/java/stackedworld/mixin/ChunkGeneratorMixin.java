package stackedworld.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import stackedworld.Bands;
import stackedworld.NetherBand;
import stackedworld.StackedDimensionMod;
import stackedworld.SurfaceBand;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
	@WrapMethod(method = "tryGenerateStructure")
	private boolean stacked$routeStructureHeight(
		StructureSet.StructureSelectionEntry selected,
		StructureManager structureManager,
		RegistryAccess registryAccess,
		RandomState randomState,
		StructureTemplateManager structureTemplateManager,
		long seed,
		ChunkAccess centerChunk,
		ChunkPos sourceChunkPos,
		ResourceKey<Level> level,
		Climate.Sampler climateSampler,
		Operation<Boolean> original
	) {
		Identifier structureId = selected.structure().unwrapKey().map(ResourceKey::identifier).orElse(null);
		boolean stackedColumn = centerChunk.getMinY() == Bands.MIN_Y;
		boolean netherStructure = structureId != null && stacked$isNetherStructure(structureId);
		boolean sky = structureId != null
			&& StackedDimensionMod.MOD_ID.equals(structureId.getNamespace())
			&& !netherStructure;
		if (sky && !stackedColumn) {
			return false;
		}
		if (netherStructure && structureId.getNamespace().equals(StackedDimensionMod.MOD_ID) && !stackedColumn) {
			return false;
		}
		if (stackedColumn && structureId != null && structureId.getPath().equals("ruined_portal_nether") && !StackedDimensionMod.MOD_ID.equals(structureId.getNamespace())) {
			return false;
		}
		SurfaceBand.skyStructures(sky);
		SurfaceBand.netherStructures(stackedColumn && netherStructure);
		try {
			return original.call(
				selected,
				structureManager,
				registryAccess,
				randomState,
				structureTemplateManager,
				seed,
				centerChunk,
				sourceChunkPos,
				level,
				climateSampler
			);
		} finally {
			SurfaceBand.skyStructures(false);
			SurfaceBand.netherStructures(false);
		}
	}

	private static boolean stacked$isNetherStructure(Identifier id) {
		String path = id.getPath();
		return path.equals("fortress")
			|| path.equals("bastion_remnant")
			|| path.equals("nether_fossil")
			|| path.equals("ruined_portal_nether");
	}

	@WrapOperation(
		method = "getMobsAt",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/attribute/EnvironmentAttributeSystem;getValue(Lnet/minecraft/world/attribute/EnvironmentAttribute;Lnet/minecraft/core/BlockPos;)Ljava/lang/Object;"
		)
	)
	private Object stacked$netherBandSpawnList(
		EnvironmentAttributeSystem attributes,
		EnvironmentAttribute<?> attribute,
		BlockPos pos,
		Operation<Object> original,
		@Local(argsOnly = true) Level level
	) {
		if (attribute != EnvironmentAttributes.NATURAL_MOB_SPAWNS || !(level instanceof ServerLevel server) || !NetherBand.contains(server, pos.getY())) {
			return original.call(attributes, attribute, pos);
		}

		Holder<Biome> biome = server.getUncachedNoiseBiome(QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getY()), QuartPos.fromBlock(pos.getZ()));
		if (!biome.is(BiomeTags.IS_NETHER)) {
			return original.call(attributes, attribute, pos);
		}

		MobSpawnSettings base = attributes.getDimensionValue(EnvironmentAttributes.NATURAL_MOB_SPAWNS);
		return biome.value().getAttributes().applyModifier(EnvironmentAttributes.NATURAL_MOB_SPAWNS, base);
	}

	@WrapMethod(method = "applyBiomeDecoration")
	private void stacked$capFeatureHeight(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager, Operation<Void> original) {
		SurfaceBand.capFeatures(true);
		try {
			original.call(level, chunk, structureManager);
		} finally {
			SurfaceBand.capFeatures(false);
		}
	}
}
