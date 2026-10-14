package stackedworld.mixin;

import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.Bands;
import stackedworld.SurfaceBand;

@Mixin(VerticalAnchor.BelowTop.class)
public class BelowTopMixin {
	@Inject(method = "resolveY", at = @At("RETURN"), cancellable = true)
	private void stacked$restoreCeiling(WorldGenerationContext context, CallbackInfoReturnable<Integer> cir) {
		if (context.getMinGenY() != Bands.MIN_Y) {
			return;
		}
		boolean nether = SurfaceBand.netherAnchors() || SurfaceBand.netherStructures();
		if (!SurfaceBand.featureAnchors() && !nether) {
			return;
		}
		if (nether) {
			int offset = ((VerticalAnchor.BelowTop)(Object)this).offset();
			cir.setReturnValue(Bands.NETHER_TOP - offset);
		} else {
			cir.setReturnValue(cir.getReturnValueI() - Bands.TOP_SHIFT);
		}
	}
}
