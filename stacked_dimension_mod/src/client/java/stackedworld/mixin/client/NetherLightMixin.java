package stackedworld.mixin.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.Bands;
import stackedworld.NetherBand;
import stackedworld.lighting.StackedSkylightBands;

@Mixin(BlockAndLightGetter.class)
public interface NetherLightMixin {
	@Inject(method = "getBrightness", at = @At("RETURN"), cancellable = true)
	private void stacked$netherCavernLight(LightLayer layer, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
		if (layer != LightLayer.BLOCK || pos.getY() >= Bands.OVERWORLD_FLOOR) {
			return;
		}
		if (!(this instanceof LevelHeightAccessor accessor) || !StackedSkylightBands.isStackedColumn(accessor)) {
			return;
		}

		int light = cir.getReturnValueI();
		if (light < NetherBand.VISUAL_BLOCK_LIGHT) {
			cir.setReturnValue(NetherBand.VISUAL_BLOCK_LIGHT);
		}
	}
}
