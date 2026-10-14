package stackedworld.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.NetherBand;
import stackedworld.SurfaceBand;

@Mixin(WorldGenRegion.class)
public class WorldGenRegionMixin {
	@Inject(method = "setBlock", at = @At("HEAD"), cancellable = true)
	private void stacked$keepOverworldDecorOutOfNether(
		BlockPos pos,
		BlockState state,
		int updateFlags,
		int updateLimit,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (SurfaceBand.featureAnchors()
			&& !SurfaceBand.netherAnchors()
			&& NetherBand.contains((WorldGenRegion)(Object)this, pos.getY())) {
			cir.setReturnValue(false);
		}
	}
}
