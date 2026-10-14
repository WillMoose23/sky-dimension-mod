package stackedworld.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LightEngine.class)
public interface LightEngineInvoker {
	@Invoker("enqueueDecrease")
	void stacked$enqueueDecrease(long fromNode, long decreaseData);

	@Invoker("enqueueIncrease")
	void stacked$enqueueIncrease(long fromNode, long increaseData);

	@Invoker("getState")
	BlockState stacked$getState(BlockPos pos);

	@Accessor("chunkSource")
	LightChunkGetter stacked$chunkSource();

	@Accessor("storage")
	LayerLightSectionStorage<?> stacked$storage();
}
