package stackedworld.mixin;

import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.Bands;
import stackedworld.SurfaceBand;

@Mixin(VerticalAnchor.AboveBottom.class)
public class AboveBottomMixin {
	@Inject(method = "resolveY", at = @At("RETURN"), cancellable = true)
	private void stacked$restoreOverworldBottom(WorldGenerationContext context, CallbackInfoReturnable<Integer> cir) {
		if (context.getMinGenY() == Bands.MIN_Y && SurfaceBand.featureAnchors() && !SurfaceBand.netherAnchors()) {
			cir.setReturnValue(cir.getReturnValueI() + Bands.BOTTOM_SHIFT);
		}
	}
}
