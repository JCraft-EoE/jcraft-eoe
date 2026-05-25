package net.arna.jcraft.common.attack.moves.aerosmith;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.Map;

// near identical system to speed king
// once speed king gets fully added, we're going to have to make an AbstractHeatManager class so no code duplication (or just a unified heat manager for all stands that have this effect)
public class BulletHeatManager {
    private static final Map<String, Long> HEATED_BLOCKS = new HashMap<>();

    private static String key(Level level, BlockPos pos) {
        return level.dimension().location() + ";" + pos.getX() + ";" + pos.getY() + ";" + pos.getZ();
    }

    public static void heatBlock(Level level, BlockPos pos, int durationTicks) {
        if (level.isClientSide()) return;
        HEATED_BLOCKS.put(key(level, pos), level.getGameTime() + durationTicks);
    }

    public static boolean isHeated(Level level, BlockPos pos) {
        Long expiry = HEATED_BLOCKS.get(key(level, pos));
        return expiry != null && level.getGameTime() < expiry;
    }

    public static void tick(ServerLevel level) {
        long now = level.getGameTime();
        String dimPrefix = level.dimension().location() + ";";

        HEATED_BLOCKS.entrySet().removeIf(e -> {
            if (!e.getKey().startsWith(dimPrefix)) return false;
            if (now >= e.getValue()) return true;

            String[] parts = e.getKey().substring(dimPrefix.length()).split(";", 3);
            if (parts.length < 3) return false;
            try {
                BlockPos pos = new BlockPos(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]));

                if (now % 5 == 0) {
                    double px = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
                    double py = pos.getY() + 1.0;
                    double pz = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
                    level.sendParticles(ParticleTypes.SMOKE, px, py, pz, 1, 0.0, 0.05, 0.0, 0.02);
                }

                if (now % 20 == 0) {
                    level.getEntitiesOfClass(LivingEntity.class,
                            new AABB(pos.above()).inflate(0.2, 0.1, 0.2),
                            e2 -> e2.isAlive() && !e2.isSpectator() && !e2.fireImmune() && e2.onGround())
                        .forEach(entity -> entity.hurt(level.damageSources().hotFloor(), 1.0f));
                }
            } catch (NumberFormatException ignored) {}

            return false;
        });
    }
}
