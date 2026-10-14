package stackedworld.mixin;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.NetherBand;
import stackedworld.SurfaceBand;

@Mixin(NetherFortressStructure.class)
public class NetherFortressStructureMixin {
	@Inject(method = "findGenerationPoint", at = @At("RETURN"), cancellable = true)
	private void stacked$checkBiomeInNetherBand(
		Structure.GenerationContext context,
		CallbackInfoReturnable<Optional<Structure.GenerationStub>> cir
	) {
		if (!SurfaceBand.netherStructures() || cir.getReturnValue() == null || cir.getReturnValue().isEmpty()) {
			return;
		}

		Structure.GenerationStub stub = cir.getReturnValue().get();
		BlockPos shifted = stub.position().offset(0, NetherBand.shiftVanillaY(0), 0);
		cir.setReturnValue(Optional.of(new Structure.GenerationStub(shifted, stub.generator())));
	}
}
