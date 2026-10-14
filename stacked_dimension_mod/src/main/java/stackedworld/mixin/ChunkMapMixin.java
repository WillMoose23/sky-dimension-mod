package stackedworld.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import stackedworld.worldgen.StackedNoiseChunkGenerator;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
	@ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
	private static ChunkGenerator stacked$patchedNoise(ChunkGenerator generator) {
		if (generator instanceof StackedNoiseChunkGenerator stacked) {
			return stacked.delegate();
		}
		return generator;
	}
}
