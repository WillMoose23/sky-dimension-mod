package stackedworld.mixin;

import net.minecraft.world.entity.monster.hoglin.Hoglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.NetherBand;

@Mixin(Hoglin.class)
public class HoglinMixin {
	@Inject(method = "isConverting", at = @At("HEAD"), cancellable = true)
	private void stacked$stayHoglinInNetherBand(CallbackInfoReturnable<Boolean> cir) {
		Hoglin hoglin = (Hoglin)(Object)this;
		if (NetherBand.contains(hoglin.level(), hoglin.getY())) {
			cir.setReturnValue(false);
		}
	}
}
