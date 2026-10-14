package stackedworld.mixin;

import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.structures.NetherFossilStructure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import stackedworld.NetherBand;
import stackedworld.SurfaceBand;

@Mixin(NetherFossilStructure.class)
public class NetherFossilStructureMixin {
	@Redirect(
		method = "findGenerationPoint",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;getSeaLevel()I")
	)
	private int stacked$fossilFloor(ChunkGenerator generator) {
		if (SurfaceBand.netherStructures()) {
			return NetherBand.shiftVanillaY(NetherBand.VANILLA_SEA_LEVEL);
		}
		return generator.getSeaLevel();
	}
}
