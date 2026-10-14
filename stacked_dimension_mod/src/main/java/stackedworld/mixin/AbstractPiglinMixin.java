package stackedworld.mixin;

import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.NetherBand;

@Mixin(AbstractPiglin.class)
public class AbstractPiglinMixin {
	@Inject(method = "isConverting", at = @At("HEAD"), cancellable = true)
	private void stacked$stayPiglinInNetherBand(CallbackInfoReturnable<Boolean> cir) {
		AbstractPiglin piglin = (AbstractPiglin)(Object)this;
		if (NetherBand.contains(piglin.level(), piglin.getY())) {
			cir.setReturnValue(false);
		}
	}
}
