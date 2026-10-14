package stackedworld.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.FeaturePlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import stackedworld.NetherBand;
import stackedworld.SurfaceBand;
import stackedworld.worldgen.SkySurfaceFeature;

@Mixin(FeaturePlacer.class)
public class FeaturePlacerMixin {
	@Shadow
	@Final
	private WorldGenLevel level;

	@WrapOperation(
		method = "placeWithBiomeCheck",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/levelgen/placement/FeaturePlacer;place(Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Z)Z"
		)
	)
	private boolean stacked$anchorForFeature(
		FeaturePlacer self,
		PlacedFeature placedFeature,
		RandomSource random,
		BlockPos origin,
		boolean biomeCheck,
		Operation<Boolean> original
	) {
		SurfaceBand.featureAnchors(true);
		SurfaceBand.netherAnchors(SurfaceBand.isNetherFeature(this.level, placedFeature));
		try {
			return original.call(self, placedFeature, random, origin, biomeCheck);
		} finally {
			SurfaceBand.netherAnchors(false);
			SurfaceBand.featureAnchors(false);
		}
	}

	@WrapOperation(
		method = "place(Lnet/minecraft/world/level/levelgen/placement/PlacedFeature;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Z)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/levelgen/feature/Feature;place(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z"
		)
	)
	private boolean stacked$keepOverworldDecorOutOfNether(
		Feature feature,
		WorldGenLevel level,
		ChunkGenerator generator,
		RandomSource random,
		BlockPos origin,
		Operation<Boolean> original
	) {
		if (NetherBand.contains(level, origin.getY()) && !SurfaceBand.netherAnchors() && !(feature instanceof SkySurfaceFeature)) {
			return false;
		}
		return original.call(feature, level, generator, random, origin);
	}
}
