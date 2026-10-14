package stackedworld.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stackedworld.worldgen.NetherColumnBiomes;

@Mixin(LevelChunk.class)
public class LevelChunkBiomeMixin {
	@Inject(method = "postProcessGeneration", at = @At("HEAD"))
	private void stacked$netherSectionBiomes(ServerLevel level, CallbackInfo ci) {
		NetherColumnBiomes.correct(level, (LevelChunk)(Object)this);
	}
}
