package net.arna.jcraft.client.tracer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public final class MuzzleTracker {
    private static final Map<UUID, Vec3> MAIN_HAND = new ConcurrentHashMap<>();
    private static final Map<UUID, Vec3> OFF_HAND = new ConcurrentHashMap<>();

    private MuzzleTracker() {}

    public static void record(UUID shooterId, InteractionHand hand, Vec3 worldPos) {
        positions(hand).put(shooterId, worldPos);
    }

    public static Vec3 get(UUID shooterId, InteractionHand hand) {
        return positions(hand).get(shooterId);
    }

    private static Map<UUID, Vec3> positions(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? MAIN_HAND : OFF_HAND;
    }
}
