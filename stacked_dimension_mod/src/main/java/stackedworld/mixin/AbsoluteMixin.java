package stackedworld.mixin;

import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.NetherBand;
import stackedworld.SurfaceBand;
import stackedworld.lighting.StackedSkylightBands;

@Mixin(VerticalAnchor.Absolute.class)
public class AbsoluteMixin {
	@Inject(method = "resolveY", at = @At("RETURN"), cancellable = true)
	private void stacked$shiftNetherAbsolute(WorldGenerationContext context, CallbackInfoReturnable<Integer> cir) {
		if (!StackedSkylightBands.matchesColumn(context.getMinGenY(), context.getGenDepth())
			|| (!SurfaceBand.netherAnchors() && !SurfaceBand.netherStructures())) {
			return;
		}

		int y = ((VerticalAnchor.Absolute)(Object)this).y();
		if (y >= 0 && y < NetherBand.VANILLA_HEIGHT) {
			cir.setReturnValue(NetherBand.shiftVanillaY(y));
		}
	}
}
