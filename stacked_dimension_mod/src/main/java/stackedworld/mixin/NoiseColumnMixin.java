package stackedworld.mixin;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.Bands;
import stackedworld.SurfaceBand;
import stackedworld.worldgen.ColumnSurfaces;

@Mixin(NoiseBasedChunkGenerator.class)
public class NoiseColumnMixin {
	@Inject(method = "getBaseHeight", at = @At("RETURN"), cancellable = true)
	private void stacked$pickBand(
		int x,
		int z,
		Heightmap.Types type,
		LevelHeightAccessor heightAccessor,
		RandomState randomState,
		CallbackInfoReturnable<Integer> cir
	) {
		if (heightAccessor.getMinY() != Bands.MIN_Y) {
			return;
		}

		int surface = ColumnSurfaces.structureSurface(
			(NoiseBasedChunkGenerator)(Object)this, x, z, type, heightAccessor, randomState, SurfaceBand.skyStructures()
		);
		cir.setReturnValue(surface);
	}
}
