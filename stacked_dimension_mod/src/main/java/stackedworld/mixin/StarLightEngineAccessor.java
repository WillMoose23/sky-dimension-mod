package stackedworld.mixin;

import ca.spottedleaf.starlight.common.light.SWMRNibbleArray;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "ca.spottedleaf.starlight.common.light.StarLightEngine", remap = false)
public interface StarLightEngineAccessor {
	@Accessor("world")
	Level stacked$world();

	@Accessor("mutablePos2")
	BlockPos.MutableBlockPos stacked$mutablePos2();

	@Accessor("mutablePos3")
	BlockPos.MutableBlockPos stacked$mutablePos3();

	@Accessor("coordinateOffset")
	int stacked$coordinateOffset();

	@Accessor("minLightSection")
	int stacked$minLightSection();

	@Accessor("increaseQueueInitialLength")
	int stacked$increaseQueueInitialLength();

	@Accessor("increaseQueueInitialLength")
	void stacked$increaseQueueInitialLength(int value);

	@Invoker("appendToIncreaseQueue")
	void stacked$appendToIncreaseQueue(long value);

	@Invoker("getBlockState")
	BlockState stacked$getBlockState(int worldX, int worldY, int worldZ);

	@Invoker("getNibbleFromCache")
	SWMRNibbleArray stacked$getNibbleFromCache(int chunkX, int chunkY, int chunkZ);

	@Invoker("setLightLevel")
	void stacked$setLightLevel(int worldX, int worldY, int worldZ, int level);
}
