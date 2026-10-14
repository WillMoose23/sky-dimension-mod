package stackedworld.portal;

import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BlockUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import stackedworld.Bands;
import stackedworld.NetherBand;

public final class StackedPortal {
	private static final int LINK_RADIUS_NETHER = 16;
	private static final int LINK_RADIUS_OVERWORLD = 128;
	private static final int FLOOR_SEARCH_RADIUS = 16;
	private static final int NETHER_FALLBACK_Y = -100;
	private static final int OVERWORLD_FALLBACK_Y = 70;

	private StackedPortal() {
	}

	public static @Nullable TeleportTransition destination(ServerLevel level, Entity entity, BlockPos entry) {
		if (!level.getGameRules().get(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS)) {
			return null;
		}

		boolean toNether = entity.getY() >= Bands.OVERWORLD_FLOOR;
		WorldBorder border = level.getWorldBorder();
		BlockPos clamped = border.clampToBounds(entry.getX() + 0.5, toNether ? NETHER_FALLBACK_Y : OVERWORLD_FALLBACK_Y, entry.getZ() + 0.5);
		int x = clamped.getX();
		int z = clamped.getZ();
		BlockPos linked = findLinkedPortal(level, x, z, toNether);
		BlockUtil.FoundRectangle exit;
		if (linked != null) {
			BlockState portalState = level.getBlockState(linked);
			Direction.Axis axis = portalState.getOptionalValue(NetherPortalBlock.AXIS).orElse(Direction.Axis.X);
			exit = BlockUtil.getLargestRectangleAround(
				linked, axis, 21, Direction.Axis.Y, 21, pos -> level.getBlockState(pos) == portalState
			);
		} else {
			if (entity.isSpectator()) {
				return null;
			}
			Direction.Axis axis = entity.level().getBlockState(entry).getOptionalValue(NetherPortalBlock.AXIS).orElse(Direction.Axis.X);
			BlockPos feet = findStandingSpot(level, x, z, toNether);
			if (feet == null) {
				feet = fallbackSpot(level, x, z, toNether);
			}
			if (!border.isWithinBounds(feet)) {
				return null;
			}
			exit = createFrame(level, feet, axis);
		}

		return transition(level, entity, entry, exit);
	}

	private static @Nullable BlockPos findLinkedPortal(ServerLevel level, int x, int z, boolean toNether) {
		int minY = toNether ? Bands.MIN_Y : Bands.OVERWORLD_FLOOR;
		int maxY = toNether ? Bands.OVERWORLD_FLOOR - 1 : Bands.SKY_MIN - 1;
		int radius = toNether ? LINK_RADIUS_NETHER : LINK_RADIUS_OVERWORLD;
		BlockPos approximate = new BlockPos(x, (minY + maxY) / 2, z);
		PoiManager poiManager = level.getPoiManager();
		poiManager.ensureLoadedAndValid(level, approximate, radius);
		return poiManager.getInSquare(type -> type.is(PoiTypes.NETHER_PORTAL), approximate, radius, PoiManager.Occupancy.ANY)
			.map(PoiRecord::getPos)
			.filter(pos -> pos.getY() >= minY && pos.getY() <= maxY)
			.filter(pos -> level.getBlockState(pos).hasProperty(BlockStateProperties.HORIZONTAL_AXIS))
			.min(Comparator.<BlockPos>comparingDouble(pos -> horizontalDistanceSqr(pos, x, z)).thenComparingInt(BlockPos::getY))
			.orElse(null);
	}

	private static double horizontalDistanceSqr(BlockPos pos, int x, int z) {
		double dx = pos.getX() - x;
		double dz = pos.getZ() - z;
		return dx * dx + dz * dz;
	}

	private static @Nullable BlockPos findStandingSpot(ServerLevel level, int x, int z, boolean toNether) {
		int minY = toNether ? Bands.MIN_Y + 2 : Bands.OVERWORLD_FLOOR + 1;
		int maxY = toNether ? Bands.OVERWORLD_FLOOR - 3 : Bands.SKY_MIN - 3;
		BlockPos direct = scanColumn(level, x, z, minY, maxY);
		if (direct != null) {
			return direct;
		}

		BlockPos origin = new BlockPos(x, minY, z);
		for (BlockPos.MutableBlockPos column : BlockPos.spiralAround(origin, FLOOR_SEARCH_RADIUS, Direction.EAST, Direction.SOUTH)) {
			if (column.getX() == x && column.getZ() == z) {
				continue;
			}
			BlockPos found = scanColumn(level, column.getX(), column.getZ(), minY, maxY);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private static @Nullable BlockPos scanColumn(ServerLevel level, int x, int z, int minY, int maxY) {
		for (int y = maxY; y >= minY; y--) {
			if (canStand(level, x, y, z)) {
				return new BlockPos(x, y, z);
			}
		}
		return null;
	}

	private static boolean canStand(ServerLevel level, int x, int y, int z) {
		BlockPos feet = new BlockPos(x, y, z);
		BlockPos floor = feet.below();
		BlockPos head = feet.above();
		if (floor.getY() < level.getMinY() || head.getY() > level.getMaxY()) {
			return false;
		}

		BlockState floorState = level.getBlockState(floor);
		if (!floorState.isFaceSturdy(level, floor, Direction.UP) || !floorState.getFluidState().isEmpty()) {
			return false;
		}
		return isOpen(level.getBlockState(feet)) && isOpen(level.getBlockState(head));
	}

	private static boolean isOpen(BlockState state) {
		return state.isAir() && state.getFluidState().isEmpty();
	}

	private static BlockPos fallbackSpot(ServerLevel level, int x, int z, boolean toNether) {
		if (!toNether) {
			for (int y = Bands.OVERWORLD_LIMIT - 1; y >= Bands.OVERWORLD_FLOOR; y--) {
				BlockPos floor = new BlockPos(x, y, z);
				BlockState state = level.getBlockState(floor);
				if (state.isFaceSturdy(level, floor, Direction.UP) && state.getFluidState().isEmpty() && floor.getY() + 1 < Bands.SKY_MIN) {
					return floor.above();
				}
			}
			return new BlockPos(x, OVERWORLD_FALLBACK_Y, z);
		}

		for (int y = NETHER_FALLBACK_Y - 10; y <= NETHER_FALLBACK_Y + 20; y++) {
			BlockState state = level.getBlockState(new BlockPos(x, y, z));
			if (!state.is(Blocks.LAVA) && state.getFluidState().isEmpty()) {
				return new BlockPos(x, y, z);
			}
		}
		return new BlockPos(x, NETHER_FALLBACK_Y, z);
	}

	private static BlockUtil.FoundRectangle createFrame(ServerLevel level, BlockPos feet, Direction.Axis axis) {
		Direction direction = Direction.get(Direction.AxisDirection.POSITIVE, axis);
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		BlockState portal = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
		for (int width = -1; width < 3; width++) {
			for (int height = -1; height < 4; height++) {
				if (width == -1 || width == 2 || height == -1 || height == 3) {
					mutable.setWithOffset(feet, width * direction.getStepX(), height, width * direction.getStepZ());
					if (level.getWorldBorder().isWithinBounds(mutable)) {
						level.setBlockAndUpdate(mutable, Blocks.OBSIDIAN.defaultBlockState());
					}
				}
			}
		}

		for (int width = 0; width < 2; width++) {
			for (int height = 0; height < 3; height++) {
				mutable.setWithOffset(feet, width * direction.getStepX(), height, width * direction.getStepZ());
				level.setBlock(mutable, portal, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
			}
		}
		return new BlockUtil.FoundRectangle(feet.immutable(), 2, 3);
	}

	private static TeleportTransition transition(ServerLevel level, Entity entity, BlockPos entry, BlockUtil.FoundRectangle exit) {
		BlockState entryState = entity.level().getBlockState(entry);
		Direction.Axis entryAxis;
		Vec3 offset;
		if (entryState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
			entryAxis = entryState.getValue(BlockStateProperties.HORIZONTAL_AXIS);
			BlockUtil.FoundRectangle entryArea = BlockUtil.getLargestRectangleAround(
				entry, entryAxis, 21, Direction.Axis.Y, 21, pos -> entity.level().getBlockState(pos) == entryState
			);
			offset = entity.getRelativePortalPosition(entryAxis, entryArea);
		} else {
			entryAxis = Direction.Axis.X;
			offset = new Vec3(0.5, 0.0, 0.0);
		}

		BlockPos bottomLeft = exit.minCorner;
		BlockState exitState = level.getBlockState(bottomLeft);
		Direction.Axis exitAxis = exitState.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);
		double width = exit.axis1Size;
		double height = exit.axis2Size;
		EntityDimensions dimensions = entity.getDimensions(entity.getPose());
		int outputRotation = entryAxis == exitAxis ? 0 : 90;
		double offsetRight = dimensions.width() / 2.0 + (width - dimensions.width()) * offset.x();
		double offsetUp = (height - dimensions.height()) * offset.y();
		double offsetForward = 0.5 + offset.z();
		boolean xAligned = exitAxis == Direction.Axis.X;
		Vec3 target = new Vec3(
			bottomLeft.getX() + (xAligned ? offsetRight : offsetForward),
			bottomLeft.getY() + offsetUp,
			bottomLeft.getZ() + (xAligned ? offsetForward : offsetRight)
		);
		Vec3 collisionFree = PortalShape.findCollisionFreePosition(target, level, entity, dimensions);
		return new TeleportTransition(
			level,
			collisionFree,
			Vec3.ZERO,
			outputRotation,
			0.0F,
			Relative.union(Relative.DELTA, Relative.ROTATION),
			TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET)
		);
	}
}
