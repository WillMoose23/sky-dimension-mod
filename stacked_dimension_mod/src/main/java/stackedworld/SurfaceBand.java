package stackedworld;

import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class SurfaceBand {
	private static final ThreadLocal<Boolean> SKY_STRUCTURES = ThreadLocal.withInitial(() -> false);
	private static final ThreadLocal<Boolean> NETHER_STRUCTURES = ThreadLocal.withInitial(() -> false);
	private static final ThreadLocal<Boolean> FEATURE_ANCHORS = ThreadLocal.withInitial(() -> false);
	private static final ThreadLocal<Boolean> NETHER_ANCHORS = ThreadLocal.withInitial(() -> false);
	private static final ThreadLocal<Boolean> CAP_FEATURES = ThreadLocal.withInitial(() -> false);
	private static final ThreadLocal<Boolean> CAP_SPAWN = ThreadLocal.withInitial(() -> false);

	private SurfaceBand() {
	}

	public static boolean skyStructures() {
		return SKY_STRUCTURES.get();
	}

	public static void skyStructures(boolean value) {
		SKY_STRUCTURES.set(value);
	}

	public static boolean netherStructures() {
		return NETHER_STRUCTURES.get();
	}

	public static void netherStructures(boolean value) {
		NETHER_STRUCTURES.set(value);
	}

	public static boolean featureAnchors() {
		return FEATURE_ANCHORS.get();
	}

	public static void featureAnchors(boolean value) {
		FEATURE_ANCHORS.set(value);
	}

	public static boolean netherAnchors() {
		return NETHER_ANCHORS.get();
	}

	public static void netherAnchors(boolean value) {
		NETHER_ANCHORS.set(value);
	}

	public static boolean capFeatures() {
		return CAP_FEATURES.get();
	}

	public static void capFeatures(boolean value) {
		CAP_FEATURES.set(value);
	}

	public static boolean capSpawn() {
		return CAP_SPAWN.get();
	}

	public static void capSpawn(boolean value) {
		CAP_SPAWN.set(value);
	}

	public static boolean isNetherFeature(WorldGenLevel level, PlacedFeature feature) {
		Registry<PlacedFeature> registry = level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
		Optional<ResourceKey<PlacedFeature>> key = registry.getResourceKey(feature);
		if (key.isEmpty()) {
			return false;
		}

		String path = key.get().identifier().getPath();
		return path.contains("nether")
			|| path.contains("debris")
			|| path.contains("glowstone")
			|| path.contains("basalt")
			|| path.contains("blackstone")
			|| path.contains("crimson")
			|| path.contains("warped")
			|| path.contains("soul")
			|| path.contains("quartz")
			|| path.contains("delta")
			|| path.contains("weeping")
			|| path.contains("twisting")
			|| path.equals("patch_fire")
			|| path.equals("ore_magma")
			|| path.equals("spring_open")
			|| path.contains("spring_closed");
	}
}
