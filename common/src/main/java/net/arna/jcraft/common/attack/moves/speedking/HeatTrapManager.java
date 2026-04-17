package net.arna.jcraft.common.attack.moves.speedking;


import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.arna.jcraft.common.network.s2c.HeatParticlePacket;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HeatTrapManager {
    public static final int MAX_HEAT = 10;
    private static final int HEAT_DURATION = 500;

    private record HeatEntry(UUID attackerUUID, WeakReference<LivingEntity> target, int heatLevel, long expiryTick) {}

    private static final Map<UUID, HeatEntry> HEAT_MAP = new HashMap<>();

    private record BlockHeatEntry(UUID attackerUUID, long expiryTick) {}

    private static final Map<String, BlockHeatEntry> HEATED_BLOCKS = new HashMap<>();

    private static String blockKey(Level level, BlockPos pos) {
        return level.dimension().location() + ";" + pos.getX() + ";" + pos.getY() + ";" + pos.getZ();
    }

    public static void heatBlock(Level level, BlockPos pos, int durationTicks, UUID attackerUUID) {
        if (level.isClientSide()) return;
        HEATED_BLOCKS.put(blockKey(level, pos), new BlockHeatEntry(attackerUUID, level.getGameTime() + durationTicks));
    }

    public static void addHeat(LivingEntity target, LivingEntity attacker) {
        if (target.level().isClientSide()) return;
        if (target instanceof StandEntity<?, ?>) return;
        UUID targetId = target.getUUID();
        HeatEntry existing = HEAT_MAP.get(targetId);
        int current = (existing != null && existing.attackerUUID().equals(attacker.getUUID())) ? existing.heatLevel() : 0;
        int newHeat = Math.min(MAX_HEAT, current + 1);
        HEAT_MAP.put(targetId, new HeatEntry(attacker.getUUID(), new WeakReference<>(target), newHeat, target.level().getGameTime() + HEAT_DURATION));
    }

    public static int getHeat(LivingEntity target) {
        return getHeat(target.getUUID());
    }

    public static int getHeat(UUID targetUUID) {
        HeatEntry entry = HEAT_MAP.get(targetUUID);
        return entry != null ? entry.heatLevel() : 0;
    }

    public static UUID getAttackerUUID(LivingEntity target) {
        HeatEntry entry = HEAT_MAP.get(target.getUUID());
        return entry != null ? entry.attackerUUID() : null;
    }

    public static void clearHeat(LivingEntity target) {
        HEAT_MAP.remove(target.getUUID());
    }

    private static void setHeat(LivingEntity target, UUID attackerUUID, int amount, long expiryTick) {
        if (target instanceof StandEntity<?, ?>) return;
        HEAT_MAP.put(target.getUUID(), new HeatEntry(attackerUUID, new WeakReference<>(target), Math.min(MAX_HEAT, amount), expiryTick));
    }

    /** Removes all heated blocks belonging to the attacker in this level and returns their positions. */
    public static List<BlockPos> detonateHeatedBlocks(Level level, UUID attackerUUID) {
        List<BlockPos> result = new ArrayList<>();
        String dimPrefix = level.dimension().location() + ";";
        HEATED_BLOCKS.entrySet().removeIf(e -> {
            if (!e.getValue().attackerUUID().equals(attackerUUID)) return false;
            String key = e.getKey();
            if (!key.startsWith(dimPrefix)) return false;
            String[] coords = key.substring(dimPrefix.length()).split(";", 3);
            if (coords.length < 3) return false;
            try {
                result.add(new BlockPos(
                        Integer.parseInt(coords[0]),
                        Integer.parseInt(coords[1]),
                        Integer.parseInt(coords[2])));
            } catch (NumberFormatException ignored) {}
            return true;
        });
        return result;
    }

    public static void tick(ServerLevel level, SpeedKingEntity stand) {
        if (!stand.hasUser()) return;
        LivingEntity user = stand.getUserOrThrow();
        if (!(user instanceof ServerPlayer player)) return;

        long now = level.getGameTime();
        HEAT_MAP.entrySet().removeIf(e -> now >= e.getValue().expiryTick());
        HEATED_BLOCKS.entrySet().removeIf(e -> now >= e.getValue().expiryTick());

        for (Map.Entry<String, BlockHeatEntry> blockEntry : HEATED_BLOCKS.entrySet()) {
            BlockHeatEntry blockHeat = blockEntry.getValue();
            if (!user.getUUID().equals(blockHeat.attackerUUID())) continue;

            String key = blockEntry.getKey();
            int first = key.indexOf(';');
            if (first == -1) continue;
            String[] coords = key.substring(first + 1).split(";", 3);
            if (coords.length < 3) continue;
            try {
                int bx = Integer.parseInt(coords[0]);
                int by = Integer.parseInt(coords[1]);
                int bz = Integer.parseInt(coords[2]);
                BlockPos pos = new BlockPos(bx, by, bz);

                List<LivingEntity> standing = level.getEntitiesOfClass(LivingEntity.class,
                        new AABB(pos).inflate(0.1, 0.5, 0.1),
                        e -> e != user && e.isAlive() && !e.isSpectator());

                for (LivingEntity entity : standing) {
                    if (now % 20 == 0) {
                        addHeat(entity, user);
                    }
                }

                BlockState bs = level.getBlockState(pos);
                if (bs.is(Blocks.WATER)) {
                    if (now % 10 == 0) {
                        level.getEntitiesOfClass(LivingEntity.class,
                                new AABB(pos).inflate(0.1),
                                e -> e != user && e.isAlive() && !e.isSpectator() && !e.fireImmune())
                            .forEach(e -> e.hurt(level.damageSources().inFire(), 1.0f));
                    }
                } else if (!bs.isAir()) {
                    if (now % 20 == 0) {
                        level.getEntitiesOfClass(LivingEntity.class,
                                new AABB(pos.above()).inflate(0.2, 0.1, 0.2),
                                e -> e != user && e.isAlive() && !e.isSpectator() && !e.fireImmune() && e.onGround())
                            .forEach(e -> e.hurt(level.damageSources().hotFloor(), 1.0f));
                    }
                }
            } catch (NumberFormatException ignored) {}
        }

        // Heat spread via hitbox collision (every 5 ticks)
        if (now % 5 == 0) {
            // Collect all spread events first to avoid same-tick cascading
            record SpreadEvent(UUID sourceId, LivingEntity dest, int spreadHeat, UUID attackerUUID, long expiry) {}
            List<SpreadEvent> events = new ArrayList<>();

            for (Map.Entry<UUID, HeatEntry> heatEntry : HEAT_MAP.entrySet()) {
                HeatEntry he = heatEntry.getValue();
                if (!he.attackerUUID().equals(user.getUUID())) continue;
                LivingEntity source = he.target().get();
                if (source == null || !source.isAlive() || source.level() != level) continue;

                int sourceHeat = he.heatLevel();
                if (sourceHeat <= 0) continue;
                int spreadHeat = (sourceHeat + 1) / 2; // ceil(sourceHeat / 2)

                level.getEntitiesOfClass(LivingEntity.class, source.getBoundingBox(),
                        e -> e != source && e != user && e.isAlive() && !e.isSpectator() && !(e instanceof StandEntity<?, ?>))
                    .forEach(dest -> {
                        int destHeat = getHeat(dest.getUUID());
                        if (destHeat < spreadHeat) {
                            events.add(new SpreadEvent(heatEntry.getKey(), dest, spreadHeat, he.attackerUUID(), level.getGameTime() + HEAT_DURATION));
                        }
                    });
            }

            for (SpreadEvent ev : events) {
                // Set source to spreadHeat
                HeatEntry src = HEAT_MAP.get(ev.sourceId());
                if (src != null) {
                    HEAT_MAP.put(ev.sourceId(), new HeatEntry(src.attackerUUID(), src.target(), ev.spreadHeat(), src.expiryTick()));
                }
                // Set dest to spreadHeat
                setHeat(ev.dest(), ev.attackerUUID(), ev.spreadHeat(), ev.expiry());
            }
        }

        for (HeatEntry entry : HEAT_MAP.values()) {
            if (!entry.attackerUUID().equals(user.getUUID())) continue;
            LivingEntity target = entry.target().get();
            if (target == null || !target.isAlive() || target.level() != level) continue;

            int heat = entry.heatLevel();
            Vec3 pos = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
            float spread = target.getBbWidth() * 0.6f;

            HeatParticlePacket.send(player, pos, target.getBbHeight(), spread, heat);
        }
    }
}
