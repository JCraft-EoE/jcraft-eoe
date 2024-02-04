package net.arna.jcraft.common.tickable;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.util.DimensionData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PastDimensions {
    protected static final List<DimensionData> dimensions = new ArrayList<>();
    public static void enqueue(DimensionData dimensionData) { dimensions.add(dimensionData); }
    public static void remove(DimensionData dimensionData) { dimensions.remove(dimensionData); }
    public static void tick(MinecraftServer server) {
        List<DimensionData> newDimensions = new ArrayList<>();

        for (DimensionData dimensionData : dimensions) {
            Entity user = dimensionData.user;
            if (user == null || !user.isAlive()) continue;

            ServerWorld original = server.getWorld(dimensionData.worldKey);
            if (user.getWorld() == original) continue;

            if (--dimensionData.timer > 1) {
                newDimensions.add(dimensionData);
                continue;
            }

            Vec3d dimPos = user.getPos(); //dimValues.pos;
            if (user instanceof ServerPlayerEntity player)
                player.teleport(original, dimPos.x, dimPos.y, dimPos.z, player.getYaw(), player.getPitch());
            else JCraft.teleportToWorld(user, original, dimPos.x, dimPos.y, dimPos.z);
        }

        if (JCraft.preloadLockTicks <= 0 && newDimensions.isEmpty()) // Nobody left in AU
            JCraft.clearPreloadedChunks();

        dimensions.clear();
        dimensions.addAll(newDimensions);
    }

    public static boolean tryExit(LivingEntity user, Set<? extends Entity> targets) {
        boolean isStored = false;

        for (DimensionData dimV : dimensions) {
            // Bring others out of the AU
            if (targets.contains(dimV.user)) {
                dimV.timer = 1;
                continue;
            }

            if (dimV.user != user)
                continue;
            isStored = true;
            dimV.timer = 1;
        }

        return isStored;
    }
}
