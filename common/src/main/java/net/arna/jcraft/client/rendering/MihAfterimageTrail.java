package net.arna.jcraft.client.rendering;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Per-entity ring buffer of recent world positions, used to draw the Made In Heaven afterimage along the path the user
 * actually travelled (so the copies trail out of the player instead of snapping to fixed offsets). Sampled once per
 * game tick; the renderer interpolates between samples for smooth motion.
 */
public class MihAfterimageTrail {
    private static final Map<LivingEntity, MihAfterimageTrail> TRAILS = new WeakHashMap<>();

    private final Vec3[] positions; // index 0 = most recent
    private int size = 0;
    private int lastTick = Integer.MIN_VALUE;

    private MihAfterimageTrail(final int capacity) {
        this.positions = new Vec3[capacity];
    }

    public static MihAfterimageTrail get(final LivingEntity entity, final int capacity) {
        return TRAILS.computeIfAbsent(entity, e -> new MihAfterimageTrail(capacity));
    }

    /** Records the position once per tick; a gap in ticks (stopped ramping) restarts the trail to avoid a stale streak. */
    public void sample(final int tick, final Vec3 position) {
        if (tick == lastTick) {
            return;
        }
        if (lastTick != Integer.MIN_VALUE && tick - lastTick != 1) {
            size = 0;
        }
        lastTick = tick;
        for (int i = positions.length - 1; i > 0; i--) {
            positions[i] = positions[i - 1];
        }
        positions[0] = position;
        if (size < positions.length) {
            size++;
        }
    }

    /** @return the position {@code index} ticks ago, or null if the trail isn't that long yet. */
    public Vec3 at(final int index) {
        return index >= 0 && index < size ? positions[index] : null;
    }
}
