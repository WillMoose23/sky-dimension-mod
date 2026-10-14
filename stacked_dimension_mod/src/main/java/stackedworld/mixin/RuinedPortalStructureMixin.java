package stackedworld.mixin;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import stackedworld.NetherBand;
import stackedworld.SurfaceBand;

@Mixin(RuinedPortalStructure.class)
public class RuinedPortalStructureMixin {
	@Redirect(
		method = "findSuitableY",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/util/Mth;randomBetweenInclusive(Lnet/minecraft/util/RandomSource;II)I"
		)
	)
	private static int stacked$shiftInNetherPlacement(RandomSource random, int min, int max) {
		int y = Mth.randomBetweenInclusive(random, min, max);
		if (SurfaceBand.netherStructures()) {
			return NetherBand.shiftVanillaY(y);
		}
		return y;
	}
}
