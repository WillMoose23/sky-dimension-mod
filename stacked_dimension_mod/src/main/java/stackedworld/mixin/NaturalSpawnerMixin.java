package stackedworld.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.Bands;
import stackedworld.NetherBand;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
	@Inject(method = "getRandomPosWithin", at = @At("RETURN"), cancellable = true)
	private static void stacked$netherFloor(Level level, LevelChunk chunk, CallbackInfoReturnable<BlockPos> cir) {
		BlockPos pos = cir.getReturnValue();
		if (pos == null || !NetherBand.contains(level, pos.getY())) {
			return;
		}

		BlockState state = chunk.getBlockState(pos);
		if (!state.getFluidState().isEmpty()) {
			return;
		}
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
		if (stacked$openFloor(chunk, cursor, pos.getY())) {
			return;
		}

		BlockPos floor = stacked$nearestFloor(chunk, pos);
		if (floor != null) {
			cir.setReturnValue(floor);
		}
	}

	private static boolean stacked$openFloor(LevelChunk chunk, BlockPos.MutableBlockPos cursor, int y) {
		if (y <= Bands.MIN_Y || y >= Bands.OVERWORLD_FLOOR) {
			return false;
		}
		cursor.setY(y);
		BlockState state = chunk.getBlockState(cursor);
		if (state.isRedstoneConductor(chunk, cursor) || !state.getFluidState().isEmpty()) {
			return false;
		}
		cursor.setY(y - 1);
		return chunk.getBlockState(cursor).isRedstoneConductor(chunk, cursor);
	}

	private static BlockPos stacked$nearestFloor(LevelChunk chunk, BlockPos pos) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
		BlockPos best = null;
		int bestDistance = Integer.MAX_VALUE;
		int minY = Math.max(chunk.getMinY() + 1, Bands.MIN_Y + 1);
		for (int y = Bands.OVERWORLD_FLOOR - 1; y >= minY; y--) {
			if (!stacked$openFloor(chunk, cursor, y)) {
				continue;
			}
			int distance = Math.abs(y - pos.getY());
			if (distance < bestDistance) {
				bestDistance = distance;
				best = new BlockPos(pos.getX(), y, pos.getZ());
			}
		}
		return best;
	}
}
