package stackedworld.mixin;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.Bands;
import stackedworld.SurfaceBand;
import stackedworld.lighting.StackedSkylightBands;
import stackedworld.worldgen.ColumnSurfaces;

@Mixin(ChunkAccess.class)
public class ChunkAccessMixin {
	@Inject(method = "getHeight", at = @At("RETURN"), cancellable = true)
	private void stacked$keepGroundSurface(Heightmap.Types type, int x, int z, CallbackInfoReturnable<Integer> cir) {
		int y = cir.getReturnValueI();
		if (y > Bands.OVERWORLD_LIMIT) {
			if (SurfaceBand.capSpawn()) {
				cir.setReturnValue(Bands.OVERWORLD_LIMIT);
			} else if (SurfaceBand.capFeatures()) {
				cir.setReturnValue(ColumnSurfaces.groundHeight((ChunkAccess)(Object)this, type, x, z));
			}
			return;
		}
		if (y < Bands.OVERWORLD_FLOOR
			&& SurfaceBand.capFeatures()
			&& StackedSkylightBands.isStackedColumn((ChunkAccess)(Object)this)) {
			cir.setReturnValue(ColumnSurfaces.groundHeight((ChunkAccess)(Object)this, type, x, z));
		}
	}
}
