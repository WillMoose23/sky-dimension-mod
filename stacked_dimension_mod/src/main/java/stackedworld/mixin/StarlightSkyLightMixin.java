package stackedworld.mixin;

import ca.spottedleaf.starlight.common.blockstate.ExtendedAbstractBlockState;
import ca.spottedleaf.starlight.common.light.SkyStarLightEngine;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.lang.reflect.Field;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.Bands;
import stackedworld.lighting.StackedSkylightBands;

@Mixin(targets = "ca.spottedleaf.starlight.common.light.SkyStarLightEngine", remap = false)
public abstract class StarlightSkyLightMixin {
	@Shadow
	protected abstract boolean checkNullSection(int chunkX, int chunkY, int chunkZ, boolean extrudeInitialised);

	@Unique
	private boolean stacked$known;
	@Unique
	private boolean stacked$stacked;
	@Unique
	private static long stacked$skySpreadMask;

	@Unique
	private boolean stacked$isStacked() {
		if (!this.stacked$known) {
			this.stacked$stacked = StackedSkylightBands.isStackedColumn(this.stacked$engine().stacked$world());
			this.stacked$known = true;
		}
		return this.stacked$stacked;
	}

	@Unique
	private StarLightEngineAccessor stacked$engine() {
		return (StarLightEngineAccessor)(Object)this;
	}

	@Unique
	private static long stacked$skySpread() {
		long mask = stacked$skySpreadMask;
		if (mask != 0L) {
			return mask;
		}
		try {
			Class<?> axis = Class.forName("ca.spottedleaf.starlight.common.light.StarLightEngine$AxisDirection");
			Field positiveYField = axis.getDeclaredField("POSITIVE_Y");
			positiveYField.setAccessible(true);
			Field maskField = axis.getDeclaredField("everythingButThisDirection");
			maskField.setAccessible(true);
			mask = maskField.getLong(positiveYField.get(null));
		} catch (ReflectiveOperationException ex) {
			throw new IllegalStateException("ScalableLux skylight direction mask is missing", ex);
		}
		stacked$skySpreadMask = mask;
		return mask;
	}

	@Inject(method = "tryPropagateSkylight", at = @At("RETURN"))
	private void stacked$lightOverworldUnderIslands(
		BlockGetter world,
		int worldX,
		int startY,
		int worldZ,
		boolean extrudeInitialised,
		boolean delayLightSet,
		CallbackInfoReturnable<Integer> cir
	) {
		if (!this.stacked$isStacked() || cir.getReturnValueI() < Bands.SKY_MIN) {
			return;
		}
		this.stacked$lightOverworldColumn(worldX, worldZ, extrudeInitialised, delayLightSet);
	}

	/**
	 * Starlight stops the downward skylight walk at the first island block, which leaves the overworld dark.
	 * Paint a fresh column from y=320 as open sky. The sky band above that line is left as the original walk lit it.
	 */
	@Unique
	private void stacked$lightOverworldColumn(int worldX, int worldZ, boolean extrudeInitialised, boolean delayLightSet) {
		StarLightEngineAccessor engine = this.stacked$engine();
		BlockPos.MutableBlockPos mutablePos = engine.stacked$mutablePos3();
		int encodeOffset = engine.stacked$coordinateOffset();
		long propagateDirection = stacked$skySpread();
		int startY = Bands.OVERWORLD_LIMIT;
		int floor = engine.stacked$minLightSection() << 4;
		this.checkNullSection(worldX >> 4, startY >> 4, worldZ >> 4, extrudeInitialised);
		BlockState above = Blocks.AIR.defaultBlockState();

		while (startY >= floor) {
			if ((startY & 15) == 15) {
				this.checkNullSection(worldX >> 4, startY >> 4, worldZ >> 4, extrudeInitialised);
			}

			BlockState current = engine.stacked$getBlockState(worldX, startY, worldZ);
			VoxelShape fromShape;
			if (((ExtendedAbstractBlockState)above).scalablelux$isConditionallyFullOpaque()) {
				engine.stacked$mutablePos2().set(worldX, startY + 1, worldZ);
				fromShape = above.getFaceOcclusionShape(Direction.DOWN);
				if (Shapes.faceShapeOccludes(Shapes.empty(), fromShape)) {
					return;
				}
			} else {
				fromShape = Shapes.empty();
			}

			mutablePos.set(worldX, startY, worldZ);
			long flags = 0L;
			if (((ExtendedAbstractBlockState)current).scalablelux$isConditionallyFullOpaque()) {
				VoxelShape cullingFace = current.getFaceOcclusionShape(Direction.UP);
				if (Shapes.faceShapeOccludes(fromShape, cullingFace)) {
					return;
				}
				flags |= Long.MIN_VALUE;
			}

			if (current.getLightDampening() > 0) {
				return;
			}

			engine.stacked$appendToIncreaseQueue(
				(worldX + (worldZ << 6) + (startY << 12) + encodeOffset) & 268435455L | (15L << 28) | (propagateDirection << 32) | flags
			);
			above = current;
			if (engine.stacked$getNibbleFromCache(worldX >> 4, startY >> 4, worldZ >> 4) == null) {
				engine.stacked$increaseQueueInitialLength(engine.stacked$increaseQueueInitialLength() - 1);
				startY &= -16;
				above = Blocks.AIR.defaultBlockState();
			} else if (!delayLightSet) {
				engine.stacked$setLightLevel(worldX, startY, worldZ, 15);
			}
			startY--;
		}
	}

	@WrapOperation(
		method = "propagateBlockChanges",
		at = @At(
			value = "INVOKE",
			target = "Lca/spottedleaf/starlight/common/light/SkyStarLightEngine;appendToDecreaseQueue(J)V"
		)
	)
	private void stacked$keepOverworldSkylight(
		SkyStarLightEngine self,
		long queued,
		Operation<Void> original,
		@Local(name = "maxPropagationY") int maxPropagationY,
		@Local(name = "currY") int currY
	) {
		if (this.stacked$isStacked() && maxPropagationY >= Bands.SKY_MIN && currY < Bands.SKY_MIN) {
			return;
		}
		original.call(self, queued);
	}

	@WrapOperation(
		method = "calculateLightValue",
		at = @At(value = "INVOKE", target = "Lca/spottedleaf/starlight/common/light/SkyStarLightEngine;getLightLevel(II)I")
	)
	private int stacked$ignoreOtherBand(
		SkyStarLightEngine self, int sectionIndex, int localIndex, Operation<Integer> original, @Local(name = "worldY") int worldY, @Local(name = "offY") int offY
	) {
		if (this.stacked$isStacked() && StackedSkylightBands.crossesSkyFloor(worldY, offY)) {
			return 0;
		}
		return original.call(self, sectionIndex, localIndex);
	}
}
