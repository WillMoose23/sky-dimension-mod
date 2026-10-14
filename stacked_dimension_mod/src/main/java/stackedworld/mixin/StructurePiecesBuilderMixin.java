package stackedworld.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.spongepowered.asm.mixin.Mixin;
import stackedworld.NetherBand;
import stackedworld.SurfaceBand;

@Mixin(StructurePiecesBuilder.class)
public class StructurePiecesBuilderMixin {
	@WrapMethod(method = "moveInsideHeights")
	private void stacked$moveFortressIntoNetherBand(RandomSource random, int lowestAllowed, int highestAllowed, Operation<Void> original) {
		if (SurfaceBand.netherStructures()) {
			lowestAllowed = NetherBand.shiftVanillaY(lowestAllowed);
			highestAllowed = NetherBand.shiftVanillaY(highestAllowed);
		}
		original.call(random, lowestAllowed, highestAllowed);
	}
}
