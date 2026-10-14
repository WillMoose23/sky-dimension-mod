package stackedworld.mixin;

import ca.spottedleaf.starlight.common.light.StarLightEngine;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import stackedworld.lighting.StackedSkylightBands;

@Mixin(targets = "ca.spottedleaf.starlight.common.light.StarLightEngine", remap = false)
public abstract class StarlightSpreadMixin {
	@Shadow
	protected Level world;

	@Shadow
	protected boolean skylightPropagator;

	@Unique
	private boolean stacked$known;
	@Unique
	private boolean stacked$stacked;

	@Unique
	private boolean stacked$blocksCross(int fromY, int toY) {
		if (!this.skylightPropagator) {
			return false;
		}
		if (!this.stacked$known) {
			this.stacked$stacked = StackedSkylightBands.isStackedColumn(this.world);
			this.stacked$known = true;
		}
		return this.stacked$stacked && StackedSkylightBands.crossesSkyFloor(fromY, toY);
	}

	@WrapOperation(
		method = {"performLightIncrease", "performLightDecrease"},
		at = @At(
			value = "INVOKE",
			target = "Lca/spottedleaf/starlight/common/light/StarLightEngine;getBlockState(II)Lnet/minecraft/world/level/block/state/BlockState;"
		)
	)
	private BlockState stacked$stopBandCrossing(
		StarLightEngine self,
		int sectionIndex,
		int localIndex,
		Operation<BlockState> original,
		@Local(name = "posY") int posY,
		@Local(name = "offY") int offY
	) {
		if (this.stacked$blocksCross(posY, offY)) {
			return null;
		}
		return original.call(self, sectionIndex, localIndex);
	}
}
