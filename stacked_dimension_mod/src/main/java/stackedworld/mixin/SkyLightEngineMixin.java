package stackedworld.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.lighting.SkyLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stackedworld.Bands;
import stackedworld.lighting.StackedSkylightBands;

@Mixin(SkyLightEngine.class)
public abstract class SkyLightEngineMixin {
	@Shadow
	private BlockPos.MutableBlockPos mutablePos;

	@Shadow
	private static boolean crossedSectionEdge(Direction propagationDirection, int x, int z) {
		return false;
	}

	@Unique
	private boolean stacked$known;
	@Unique
	private boolean stacked$stacked;
	@Unique
	private final BlockPos.MutableBlockPos stacked$cursor = new BlockPos.MutableBlockPos();

	@Unique
	private boolean stacked$isStacked() {
		if (!this.stacked$known) {
			this.stacked$stacked = StackedSkylightBands.isStackedColumn(this.stacked$engine().stacked$chunkSource().getLevel());
			this.stacked$known = true;
		}
		return this.stacked$stacked;
	}

	@Unique
	private LightEngineInvoker stacked$engine() {
		return (LightEngineInvoker)(Object)this;
	}

	@WrapOperation(
		method = "propagateIncrease",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/lighting/SkyLightEngine;getOpacity(Lnet/minecraft/world/level/block/state/BlockState;)I"
		)
	)
	private int stacked$skyFloorOpacity(SkyLightEngine self, BlockState state, Operation<Integer> original, @Local(argsOnly = true, ordinal = 0) long fromNode, @Local(argsOnly = true) int fromLevel) {
		int opacity = original.call(self, state);
		if (!this.stacked$isStacked()) {
			return opacity;
		}

		int fromY = BlockPos.getY(fromNode);
		int toY = this.mutablePos.getY();
		if (!StackedSkylightBands.descendsIntoOverworld(fromY, toY)) {
			return opacity;
		}

		// Arrive as open sky above the overworld: destination opacity still applies, the sky block's does not.
		return fromLevel - (15 - opacity);
	}

	@WrapOperation(
		method = "propagateIncrease",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/lighting/SkyLightEngine;shapeOccludes(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"
		)
	)
	private boolean stacked$skyFloorShape(
		SkyLightEngine self, BlockState fromState, BlockState toState, Direction direction, Operation<Boolean> original, @Local(argsOnly = true, ordinal = 0) long fromNode
	) {
		if (this.stacked$isStacked() && direction == Direction.DOWN) {
			int fromY = BlockPos.getY(fromNode);
			if (StackedSkylightBands.descendsIntoOverworld(fromY, fromY + direction.getStepY())) {
				return false;
			}
		}
		return original.call(self, fromState, toState, direction);
	}

	@WrapOperation(
		method = {"propagateIncrease", "propagateDecrease"},
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/lighting/LightEngine$QueueEntry;shouldPropagateInDirection(JLnet/minecraft/core/Direction;)Z"
		)
	)
	private boolean stacked$keepUndersidesDark(long entry, Direction direction, Operation<Boolean> original, @Local(argsOnly = true, ordinal = 0) long fromNode) {
		if (!original.call(entry, direction)) {
			return false;
		}
		if (!this.stacked$isStacked() || direction != Direction.UP) {
			return true;
		}

		int fromY = BlockPos.getY(fromNode);
		return fromY >= Bands.SKY_MIN || fromY + 1 < Bands.SKY_MIN;
	}

	@Inject(method = "propagateFromEmptySections", at = @At("HEAD"), cancellable = true)
	private void stacked$stopEmptySpanAtSkyFloor(
		long toNode, Direction propagationDirection, int toLevel, boolean increase, int emptySectionsBelow, CallbackInfo ci
	) {
		if (!this.stacked$isStacked()) {
			return;
		}

		this.stacked$propagateFromEmptySections(toNode, propagationDirection, toLevel, increase, emptySectionsBelow);
		ci.cancel();
	}

	@Inject(method = "propagateLightSources", at = @At("RETURN"))
	private void stacked$afterPropagate(ChunkPos pos, CallbackInfo ci) {
		this.stacked$restoreChunk(pos);
	}

	@Inject(method = "setLightEnabled", at = @At("RETURN"))
	private void stacked$afterEnable(ChunkPos pos, boolean enable, CallbackInfo ci) {
		if (enable) {
			this.stacked$restoreChunk(pos);
		}
	}

	@Inject(method = "checkNode", at = @At("RETURN"))
	private void stacked$afterCheck(long blockNode, CallbackInfo ci) {
		if (!this.stacked$isStacked()) {
			return;
		}
		this.stacked$restoreColumn(BlockPos.getX(blockNode), BlockPos.getZ(blockNode));
	}

	@Unique
	private void stacked$restoreChunk(ChunkPos pos) {
		if (!this.stacked$isStacked()) {
			return;
		}

		int minX = SectionPos.sectionToBlockCoord(pos.x());
		int minZ = SectionPos.sectionToBlockCoord(pos.z());
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				this.stacked$restoreColumn(minX + x, minZ + z);
			}
		}
	}

	/**
	 * The ground roof is the only heightmap source, so vanilla paints every block above it as sky light.
	 * Drop that false source inside the sky band, below the island's own roof, and let propagation relight it.
	 */
	@Unique
	private void stacked$restoreColumn(int x, int z) {
		int roof = this.stacked$skySourceY(x, z);
		LayerLightSectionStorageAccessor light = (LayerLightSectionStorageAccessor)(Object)this.stacked$engine().stacked$storage();
		for (int y = roof - 1; y >= Bands.SKY_MIN; y--) {
			long blockNode = BlockPos.asLong(x, y, z);
			if (!light.stacked$storingLightForSection(SectionPos.blockToSection(blockNode))) {
				continue;
			}

			int level = light.stacked$getStoredLevel(blockNode);
			if (level == 0) {
				continue;
			}

			light.stacked$setStoredLevel(blockNode, 0);
			this.stacked$engine().stacked$enqueueDecrease(blockNode, LightEngine.QueueEntry.decreaseAllDirections(level));
		}
	}

	@Unique
	private int stacked$skySourceY(int x, int z) {
		int maxY = this.stacked$engine().stacked$chunkSource().getLevel().getMaxY();
		BlockState above = Blocks.AIR.defaultBlockState();
		for (int y = maxY; y >= Bands.SKY_MIN; y--) {
			BlockState state = this.stacked$engine().stacked$getState(this.stacked$cursor.set(x, y, z));
			if (StackedSkylightBands.isEdgeOccluded(above, state)) {
				return y + 1;
			}
			above = state;
		}
		return Bands.SKY_MIN;
	}

	@Unique
	private void stacked$propagateFromEmptySections(long toNode, Direction propagationDirection, int toLevel, boolean increase, int emptySectionsBelow) {
		if (emptySectionsBelow == 0) {
			return;
		}

		int x = BlockPos.getX(toNode);
		int z = BlockPos.getZ(toNode);
		if (!crossedSectionEdge(propagationDirection, SectionPos.sectionRelative(x), SectionPos.sectionRelative(z))) {
			return;
		}

		int y = BlockPos.getY(toNode);
		int sectionX = SectionPos.blockToSectionCoord(x);
		int sectionZ = SectionPos.blockToSectionCoord(z);
		int sectionY = SectionPos.blockToSectionCoord(y) - 1;
		int bottomSectionY = sectionY - emptySectionsBelow + 1;
		LayerLightSectionStorageAccessor light = (LayerLightSectionStorageAccessor)(Object)this.stacked$engine().stacked$storage();

		while (sectionY >= bottomSectionY) {
			// Section 20 holds both y=320 and y=321. Don't stamp a sky level onto the overworld floor.
			if (SectionPos.sectionToBlockCoord(sectionY) < Bands.SKY_MIN) {
				return;
			}

			long sectionNode = SectionPos.asLong(sectionX, sectionY, sectionZ);
			if (!light.stacked$storingLightForSection(sectionNode)) {
				sectionY--;
				continue;
			}

			int sectionMinY = SectionPos.sectionToBlockCoord(sectionY);
			for (int localY = 15; localY >= 0; localY--) {
				long blockNode = BlockPos.asLong(x, sectionMinY + localY, z);
				if (increase) {
					light.stacked$setStoredLevel(blockNode, toLevel);
					if (toLevel > 1) {
						this.stacked$engine().stacked$enqueueIncrease(blockNode, LightEngine.QueueEntry.increaseSkipOneDirection(toLevel, true, propagationDirection.getOpposite()));
					}
				} else {
					light.stacked$setStoredLevel(blockNode, 0);
					this.stacked$engine().stacked$enqueueDecrease(blockNode, LightEngine.QueueEntry.decreaseSkipOneDirection(toLevel, propagationDirection.getOpposite()));
				}
			}
			sectionY--;
		}
	}
}
