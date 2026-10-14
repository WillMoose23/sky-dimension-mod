package stackedworld.mixin;

import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LayerLightSectionStorage.class)
public interface LayerLightSectionStorageAccessor {
	@Invoker("setStoredLevel")
	void stacked$setStoredLevel(long blockNode, int level);

	@Invoker("getStoredLevel")
	int stacked$getStoredLevel(long blockNode);

	@Invoker("storingLightForSection")
	boolean stacked$storingLightForSection(long sectionNode);
}
