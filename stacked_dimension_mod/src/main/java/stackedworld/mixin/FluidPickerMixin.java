package stackedworld.mixin;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.Bands;
import stackedworld.lighting.StackedSkylightBands;

@Mixin(NoiseBasedChunkGenerator.class)
public class FluidPickerMixin {
	@Inject(method = "createFluidPicker", at = @At("RETURN"), cancellable = true)
	private static void stacked$airInNetherBand(NoiseGeneratorSettings settings, CallbackInfoReturnable<Aquifer.FluidPicker> cir) {
		NoiseSettings noise = settings.noiseSettings();
		if (!StackedSkylightBands.matchesColumn(noise.minY(), noise.height())) {
			return;
		}

		Aquifer.FluidPicker vanilla = cir.getReturnValue();
		if (vanilla == null) {
			return;
		}

		// Vanilla's global picker uses lava below y=-54. In this column that fills the nether band.
		Aquifer.FluidStatus empty = new Aquifer.FluidStatus(DimensionType.MIN_Y * 2, Blocks.AIR.defaultBlockState());
		cir.setReturnValue((x, y, z) -> {
			if (y >= Bands.MIN_Y && y < Bands.OVERWORLD_FLOOR) {
				return empty;
			}
			return vanilla.computeFluid(x, y, z);
		});
	}
}
