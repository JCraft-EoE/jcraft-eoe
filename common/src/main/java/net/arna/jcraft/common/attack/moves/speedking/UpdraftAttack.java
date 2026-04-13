package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
public final class UpdraftAttack extends AbstractMove<UpdraftAttack, SpeedKingEntity> {
    /** Total column lifetime in ticks */
    private final int windDuration;
    /** Upward velocity floored each tick while below max height */
    private final double windLift;
    /** Max height above column base before hovering */
    private final double maxHeight;
    /** Horizontal radius of the updraft column (blocks) */
    private final double columnRadius;

    private static final Map<String, UpdraftPad> ACTIVE_PADS = new HashMap<>();

    public UpdraftAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                         final int windDuration, final double windLift,
                         final double maxHeight, final double columnRadius) {
        super(cooldown, windup, duration, moveDistance);
        this.windDuration = windDuration;
        this.windLift = windLift;
        this.maxHeight = maxHeight;
        this.columnRadius = columnRadius;
    }

    @Override
    public @NonNull MoveType<UpdraftAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        if (attacker.level().isClientSide()) return Set.of();

        Vec3 userPos = user.position();
        // Place the column at the block the user is standing on
        BlockPos padPos = BlockPos.containing(userPos.x, userPos.y - 0.1, userPos.z);

        String padKey = padKey(attacker.level(), padPos);
        ACTIVE_PADS.put(padKey, new UpdraftPad(padPos, attacker.level().getGameTime(), user.getUUID(),
                windDuration, windLift, maxHeight, columnRadius));

        return Set.of();
    }

    public static void tickUpdraftPads(Level level) {
        if (level.isClientSide()) return;
        ServerLevel serverLevel = (ServerLevel) level;

        ACTIVE_PADS.entrySet().removeIf(entry -> {
            UpdraftPad pad = entry.getValue();
            long age = level.getGameTime() - pad.createdAt;

            Vec3 columnBase = Vec3.atCenterOf(pad.pos);
            // The active zone covers the full column height
            AABB column = new AABB(
                    columnBase.x - pad.columnRadius, columnBase.y,                        columnBase.z - pad.columnRadius,
                    columnBase.x + pad.columnRadius, columnBase.y + pad.maxHeight + 2,    columnBase.z + pad.columnRadius
            );

            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, column, e -> e.isAlive() && !e.isSpectator())) {
                Vec3 vel = entity.getDeltaMovement();
                double entityRelativeY = entity.getY() - columnBase.y;
                if (entityRelativeY < pad.maxHeight) {
                    // Below max height — push upward
                    entity.setDeltaMovement(vel.x, Math.max(vel.y, pad.windLift), vel.z);
                } else {
                    // At or above max height — hover: cancel gravity
                    entity.setDeltaMovement(vel.x, Math.max(vel.y, 0.0), vel.z);
                }
                entity.hurtMarked = true;
                entity.fallDistance = 0f;
            }

            // Particles — rising heat column
            spawnColumnParticles(serverLevel, columnBase, pad);

            // Remove after duration
            return age > pad.windDuration;
        });
    }

    private static void spawnColumnParticles(ServerLevel level, Vec3 base, UpdraftPad pad) {
        // Rising updraft particles scattered throughout the column
        for (int i = 0; i < 6; i++) {
            double ox = (level.random.nextDouble() - 0.5) * pad.columnRadius * 1.8;
            double oz = (level.random.nextDouble() - 0.5) * pad.columnRadius * 1.8;
            double oy = level.random.nextDouble() * (pad.maxHeight * 0.9);
            // xSpeed/zSpeed give slight outward drift; ySpeed gives upward velocity
            double xSpeed = ox * 0.02;
            double ySpeed = 0.1 + level.random.nextDouble() * 0.1;
            double zSpeed = oz * 0.02;
            level.sendParticles(JParticleTypeRegistry.UPDRAFT.get(),
                    base.x + ox, base.y + oy, base.z + oz,
                    1, xSpeed, ySpeed, zSpeed, 0.0);
        }

        // Dense burst at the base of the column where the wind originates
        for (int i = 0; i < 3; i++) {
            double ox = (level.random.nextDouble() - 0.5) * pad.columnRadius * 2.0;
            double oz = (level.random.nextDouble() - 0.5) * pad.columnRadius * 2.0;
            level.sendParticles(JParticleTypeRegistry.UPDRAFT.get(),
                    base.x + ox, base.y + 0.1, base.z + oz,
                    1, 0.0, 0.2, 0.0, 0.0);
        }
    }

    private static String padKey(Level level, BlockPos pos) {
        return level.dimension().location() + "_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
    }

    @Override
    protected @NonNull UpdraftAttack getThis() {
        return this;
    }

    @Override
    public @NonNull UpdraftAttack copy() {
        return copyExtras(new UpdraftAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                windDuration, windLift, maxHeight, columnRadius));
    }

    public record UpdraftPad(
            BlockPos pos,
            long createdAt,
            UUID ownerUUID,
            int windDuration,
            double windLift,
            double maxHeight,
            double columnRadius
    ) {}

    public static class Type extends AbstractMove.Type<UpdraftAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<UpdraftAttack>, UpdraftAttack> buildCodec(RecordCodecBuilder.Instance<UpdraftAttack> instance) {
            return baseDefault(instance, (cd, wu, dur, md) ->
                    new UpdraftAttack(cd, wu, dur, md, 160, 0.9, 25.0, 1.2));
        }
    }
}
