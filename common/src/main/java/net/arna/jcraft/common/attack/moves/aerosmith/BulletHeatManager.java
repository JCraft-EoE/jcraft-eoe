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
    private record DimBlockPos(Level level, BlockPos pos) {}

    private static final Map<DimBlockPos, Integer> HEATED_BLOCKS = new HashMap<>();
    private static int tickCounter = 0;

    public static void heatBlock(Level level, BlockPos pos, int durationTicks) {
        if (level.isClientSide()) return;
        HEATED_BLOCKS.put(new DimBlockPos(level, pos), durationTicks);
    }

    public static boolean isHeated(Level level, BlockPos pos) {
        Integer remaining = HEATED_BLOCKS.get(new DimBlockPos(level, pos));
        return remaining != null && remaining > 0;
    }

    public static void tick(ServerLevel level) {
        tickCounter++;

        HEATED_BLOCKS.entrySet().removeIf(e -> {
            int remaining = e.getValue() - 1;
            if (remaining <= 0) return true;
            e.setValue(remaining);

            if (e.getKey().level() != level) return false;

            BlockPos pos = e.getKey().pos();

            if (tickCounter % 5 == 0) {
                double px = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
                double py = pos.getY() + 1.0;
                double pz = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
                level.sendParticles(ParticleTypes.SMOKE, px, py, pz, 1, 0.0, 0.05, 0.0, 0.02);
            }

            if (tickCounter % 20 == 0) {
                level.getEntitiesOfClass(LivingEntity.class,
                        new AABB(pos.above()).inflate(0.2, 0.1, 0.2),
                        e2 -> e2.isAlive() && !e2.isSpectator() && !e2.fireImmune() && e2.onGround())
                    .forEach(entity -> entity.hurt(level.damageSources().hotFloor(), 1.0f));
            }

            return false;
        });
    }
}
