package stackedworld.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import stackedworld.SurfaceBand;

@Mixin(PlayerSpawnFinder.class)
public class PlayerSpawnFinderMixin {
	@WrapMethod(method = "getLevelRespawnPos")
	private static BlockPos stacked$spawnOnGround(ServerLevel level, int x, int z, Operation<BlockPos> original) {
		SurfaceBand.capSpawn(true);
		try {
			return original.call(level, x, z);
		} finally {
			SurfaceBand.capSpawn(false);
		}
	}
}
