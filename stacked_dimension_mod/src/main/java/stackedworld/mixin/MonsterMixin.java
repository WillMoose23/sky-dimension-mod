package stackedworld.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.NetherBand;

@Mixin(Monster.class)
public class MonsterMixin {
	@Inject(method = "isDarkEnoughToSpawn", at = @At("HEAD"), cancellable = true)
	private static void stacked$netherBandLight(
		ServerLevelAccessor level,
		BlockPos pos,
		RandomSource random,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (!NetherBand.contains(level, pos.getY())) {
			return;
		}

		int brightness = level.getLevel().isThundering() ? level.getMaxLocalRawBrightness(pos, 10) : level.getMaxLocalRawBrightness(pos);
		cir.setReturnValue(brightness <= NetherBand.MONSTER_LIGHT);
	}
}
