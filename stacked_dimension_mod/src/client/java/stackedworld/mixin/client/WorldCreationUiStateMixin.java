package stackedworld.mixin.client;

import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.StackedDimensionMod;
import stackedworld.client.StackedWorldPresetEditor;

@Mixin(WorldCreationUiState.class)
public class WorldCreationUiStateMixin {
	@Inject(method = "getPresetEditor", at = @At("RETURN"), cancellable = true)
	private void stacked$editor(CallbackInfoReturnable<PresetEditor> cir) {
		if (cir.getReturnValue() != null) {
			return;
		}

		Holder<WorldPreset> preset = ((WorldCreationUiState)(Object)this).getWorldType().preset();
		if (preset != null && preset.is(StackedDimensionMod.PRESET)) {
			cir.setReturnValue(StackedWorldPresetEditor.EDITOR);
		}
	}
}
