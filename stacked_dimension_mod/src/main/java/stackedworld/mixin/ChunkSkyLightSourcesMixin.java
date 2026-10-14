package stackedworld.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.lighting.StackedSkylightBands;

@Mixin(ChunkSkyLightSources.class)
public class ChunkSkyLightSourcesMixin {
	@Shadow
	private int minY;
	@Shadow
	private BlockPos.MutableBlockPos mutablePos1;
	@Shadow
	private BlockPos.MutableBlockPos mutablePos2;

	@Shadow
	private void set(int index, int value) {
	}

	@Shadow
	private static boolean isEdgeOccluded(BlockState topState, BlockState bottomState) {
		return false;
	}

	@Shadow
	private int findLowestSourceBelow(BlockGetter level, BlockPos startPos, BlockState startState) {
		return 0;
	}

	@Unique
	private boolean stacked$stacked;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void stacked$rememberColumn(LevelHeightAccessor level, CallbackInfo ci) {
		this.stacked$stacked = StackedSkylightBands.isStackedColumn(level);
	}

	@Inject(method = "findLowestSourceY", at = @At("HEAD"), cancellable = true)
	private void stacked$groundRoof(ChunkAccess chunk, int topSectionIndex, int x, int z, CallbackInfoReturnable<Integer> cir) {
		if (!this.stacked$stacked) {
			return;
		}

		int topY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(topSectionIndex) + 1);
		BlockPos.MutableBlockPos topPos = this.mutablePos1.set(x, topY, z);
		BlockPos.MutableBlockPos bottomPos = this.mutablePos2.setWithOffset(topPos, Direction.DOWN);
		BlockState topState = Blocks.AIR.defaultBlockState();

		for (int sectionIndex = topSectionIndex; sectionIndex >= 0; sectionIndex--) {
			LevelChunkSection section = chunk.getSection(sectionIndex);
			if (section.hasOnlyAir()) {
				topState = Blocks.AIR.defaultBlockState();
				int sectionY = chunk.getSectionYFromSectionIndex(sectionIndex);
				topPos.setY(SectionPos.sectionToBlockCoord(sectionY));
				bottomPos.setY(topPos.getY() - 1);
			} else {
				for (int y = 15; y >= 0; y--) {
					BlockState bottomState = section.getBlockState(x, y, z);
					if (stacked$occludesGround(topPos.getY(), bottomPos.getY(), topState, bottomState)) {
						cir.setReturnValue(topPos.getY());
						return;
					}

					topState = bottomState;
					topPos.set(bottomPos);
					bottomPos.move(Direction.DOWN);
				}
			}
		}

		cir.setReturnValue(this.minY);
	}

	@Inject(method = "updateEdge", at = @At("HEAD"), cancellable = true)
	private void stacked$groundEdge(
		BlockGetter level,
		int index,
		int oldTopEdgeY,
		BlockPos topPos,
		BlockState topState,
		BlockPos bottomPos,
		BlockState bottomState,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (!this.stacked$stacked) {
			return;
		}

		int checkedEdgeY = topPos.getY();
		if (stacked$occludesGround(topPos.getY(), bottomPos.getY(), topState, bottomState)) {
			if (checkedEdgeY > oldTopEdgeY) {
				this.set(index, checkedEdgeY);
				cir.setReturnValue(true);
				return;
			}
		} else if (checkedEdgeY == oldTopEdgeY) {
			this.set(index, this.findLowestSourceBelow(level, bottomPos, bottomState));
			cir.setReturnValue(true);
			return;
		}

		cir.setReturnValue(false);
	}

	@Inject(method = "findLowestSourceBelow", at = @At("HEAD"), cancellable = true)
	private void stacked$groundRoofBelow(BlockGetter level, BlockPos startPos, BlockState startState, CallbackInfoReturnable<Integer> cir) {
		if (!this.stacked$stacked) {
			return;
		}

		BlockPos.MutableBlockPos topPos = this.mutablePos1.set(startPos);
		BlockPos.MutableBlockPos bottomPos = this.mutablePos2.setWithOffset(startPos, Direction.DOWN);
		BlockState topState = startState;

		while (bottomPos.getY() >= this.minY) {
			BlockState bottomState = level.getBlockState(bottomPos);
			if (stacked$occludesGround(topPos.getY(), bottomPos.getY(), topState, bottomState)) {
				cir.setReturnValue(topPos.getY());
				return;
			}

			topState = bottomState;
			topPos.set(bottomPos);
			bottomPos.move(Direction.DOWN);
		}

		cir.setReturnValue(this.minY);
	}

	@Unique
	private static boolean stacked$occludesGround(int topY, int bottomY, BlockState topState, BlockState bottomState) {
		return isEdgeOccluded(topState, bottomState) && !StackedSkylightBands.isSkyRoofEdge(topY, bottomY);
	}
}
