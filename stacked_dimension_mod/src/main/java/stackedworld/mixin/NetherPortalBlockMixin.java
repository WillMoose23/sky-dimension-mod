package stackedworld.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stackedworld.NetherBand;
import stackedworld.portal.StackedPortal;

@Mixin(NetherPortalBlock.class)
public class NetherPortalBlockMixin {
	@Inject(method = "getPortalDestination", at = @At("HEAD"), cancellable = true)
	private void stacked$sameColumnPortal(
		ServerLevel currentLevel,
		Entity entity,
		BlockPos portalEntryPos,
		CallbackInfoReturnable<TeleportTransition> cir
	) {
		if (!NetherBand.isColumn(currentLevel)) {
			return;
		}
		cir.setReturnValue(StackedPortal.destination(currentLevel, entity, portalEntryPos));
	}
}
