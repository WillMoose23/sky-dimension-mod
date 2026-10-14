package stackedworld.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.Bands;
import stackedworld.lighting.StackedSkylightBands;

@Mixin(LevelReader.class)
public interface LevelReaderBiomeMixin {
	@Inject(method = "getNoiseBiome", at = @At("RETURN"), cancellable = true)
	private void stacked$netherBandBiome(int quartX, int quartY, int quartZ, CallbackInfoReturnable<Holder<Biome>> cir) {
		LevelReader self = (LevelReader)(Object)this;
		if (!StackedSkylightBands.isStackedColumn(self) || QuartPos.toBlock(quartY) >= Bands.OVERWORLD_FLOOR) {
			return;
		}

		Holder<Biome> current = cir.getReturnValue();
		if (current != null && current.is(BiomeTags.IS_NETHER)) {
			return;
		}

		Holder<Biome> resolved = self.getUncachedNoiseBiome(quartX, quartY, quartZ);
		if (resolved != null && resolved.is(BiomeTags.IS_NETHER)) {
			cir.setReturnValue(resolved);
		}
	}
}
