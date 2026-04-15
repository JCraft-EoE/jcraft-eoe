package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * Sirocco — aerial utility.
 * 1:1 with KQ's explosive dash mechanics: sets {@code dash = true}, applies look-direction
 * velocity on perform. Additionally flings all nearby entities in the same direction and
 * spawns heat wind particles behind the user.
 */
public final class SiroccoAttack extends AbstractMove<SiroccoAttack, SpeedKingEntity> {
    /** Radius around the user to grab and fling entities along */
    private final double entityRadius;

    public SiroccoAttack(final int cooldown, final int windup, final int duration,
                         final float moveDistance, final double entityRadius) {
        super(cooldown, windup, duration, moveDistance);
        this.entityRadius = entityRadius;
        dash = true;
    }

    @Override
    public @NonNull MoveType<SiroccoAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        // Direction: mirrors the regular dash (DashData.tryDash) — WASD relative to look angle,
        // falling back to mouse aim if no keys are held.
        final int forward = (int) attacker.getRemoteForwardInput();
        final int side = (int) attacker.getRemoteSideInput();

        final Vec3 dashVec;
        if (forward != 0 || side != 0) {
            Vec3 dir = Vec3.directionFromRotation(user.getXRot(), user.getYRot());
            dir = dir.yRot(1.57079632679f * side);
            if (side != 0 && forward == 1) {
                dir = dir.yRot(-0.785398163397f * side); // forward diagonal
            }
            if (forward == -1) {
                dir = dir.yRot(side == 0 ? 3.14159265359f : 0.785398163397f * side); // backward
            }
            dashVec = dir.normalize().scale(2);
        } else {
            dashVec = user.getLookAngle().scale(2);
        }

        // Apply velocity exactly like KQ dash
        user.setDeltaMovement(user.getDeltaMovement().add(dashVec));
        user.hurtMarked = true;

        // Fling all nearby entities in the same direction
        AABB area = user.getBoundingBox().inflate(entityRadius);
        attacker.level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != user && e != attacker && e.isAlive() && !e.isSpectator())
                .forEach(e -> {
                    e.setDeltaMovement(e.getDeltaMovement().add(dashVec));
                    e.hurtMarked = true;
                    e.fallDistance = 0f;
                });

        // Heat wind particles behind the user (server-side)
        if (!attacker.level().isClientSide() && attacker.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = user.position();
            Vec3 behind = pos.subtract(dashVec.normalize().scale(1.5));
            for (int i = 0; i < 8; i++) {
                double ox = (serverLevel.random.nextDouble() - 0.5) * 1.2;
                double oy = (serverLevel.random.nextDouble() - 0.5) * 0.8 + 0.5;
                double oz = (serverLevel.random.nextDouble() - 0.5) * 1.2;
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                        behind.x + ox, behind.y + oy, behind.z + oz,
                        1, -dashVec.x * 0.2, 0.05, -dashVec.z * 0.2, 0.0);
            }
            for (int i = 0; i < 4; i++) {
                double ox = (serverLevel.random.nextDouble() - 0.5) * 0.6;
                double oy = serverLevel.random.nextDouble();
                double oz = (serverLevel.random.nextDouble() - 0.5) * 0.6;
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        behind.x + ox, behind.y + oy, behind.z + oz,
                        1, -dashVec.x * 0.1, 0.05, -dashVec.z * 0.1, 0.0);
            }
        }

        return Set.of();
    }

    @Override
    protected @NonNull SiroccoAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SiroccoAttack copy() {
        return copyExtras(new SiroccoAttack(getCooldown(), getWindup(), getDuration(),
                getMoveDistance(), entityRadius));
    }

    public static class Type extends AbstractMove.Type<SiroccoAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SiroccoAttack>, SiroccoAttack> buildCodec(
                RecordCodecBuilder.Instance<SiroccoAttack> instance) {
            return baseDefault(instance, (cd, wu, dur, md) ->
                    new SiroccoAttack(cd, wu, dur, md, 4.0));
        }
    }
}
