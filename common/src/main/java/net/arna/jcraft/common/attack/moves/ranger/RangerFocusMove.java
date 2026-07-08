package net.arna.jcraft.common.attack.moves.ranger;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonGunslingerComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.spec.RangerSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class RangerFocusMove extends AbstractMove<RangerFocusMove, RangerSpec> {
    public static final double TARGET_RANGE = 32.0;
    // cos(30 degrees), the horizontal half-angle around the crosshair. Pitch is ignored so lobbed shots still lock on
    private static final double MIN_TARGET_DOT = 0.866;
    private static final float MAX_TURN_DEGREES = 6.0f;
    private static final double SHOOTDOWN_RANGE = 1.0;

    public RangerFocusMove(final int cooldown) {
        super(cooldown, 0, 0, 0f);
    }

    @Override
    public boolean conditionsMet(final RangerSpec attacker) {
        final CommonGunslingerComponent gunslinger = JComponentPlatformUtils.getGunslinger(attacker.getUserOrThrow());
        return super.conditionsMet(attacker) && (gunslinger.isFocusActive() || gunslinger.getFocus() > 0f);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final RangerSpec attacker, final LivingEntity user) {
        final CommonGunslingerComponent gunslinger = JComponentPlatformUtils.getGunslinger(user);
        gunslinger.setFocusActive(!gunslinger.isFocusActive());
        return Set.of();
    }

    public static boolean isFocusTarget(final LivingEntity user, final Entity target) {
        if (target == user || !target.isAlive()) {
            return false;
        }
        if (target instanceof Projectile projectile) {
            final Entity projectileOwner = projectile.getOwner();
            if ((projectileOwner != null && JUtils.getUserIfStand(projectileOwner) == user) ||
                    projectile.onGround() || projectile.getDeltaMovement().lengthSqr() < 1.0E-3) {
                return false;
            }
        } else if (target instanceof LivingEntity) {
            if (target == JUtils.getStand(user) || !EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) {
                return false;
            }
        } else {
            return false;
        }
        if (user.distanceTo(target) > TARGET_RANGE) {
            return false;
        }
        return horizontalAlignment(user, target) > MIN_TARGET_DOT && user.hasLineOfSight(target);
    }

    @Nullable
    public static Entity selectFocusTarget(final LivingEntity user) {
        final AABB box = user.getBoundingBox().inflate(TARGET_RANGE);
        Entity best = null;
        double bestDot = MIN_TARGET_DOT;
        for (final Entity candidate : user.level().getEntities(user, box, e -> isFocusTarget(user, e))) {
            final double dot = horizontalAlignment(user, candidate);
            if (dot > bestDot) {
                bestDot = dot;
                best = candidate;
            }
        }
        return best;
    }

    private static double horizontalAlignment(final LivingEntity user, final Entity target) {
        final Vec3 look = user.getLookAngle();
        final Vec3 toTarget = target.getBoundingBox().getCenter().subtract(user.getEyePosition());
        final Vec3 flatLook = new Vec3(look.x, 0.0, look.z);
        final Vec3 flatToTarget = new Vec3(toTarget.x, 0.0, toTarget.z);
        if (flatLook.lengthSqr() < 1.0E-4 || flatToTarget.lengthSqr() < 1.0E-4) {
            return -1.0;
        }
        return flatLook.normalize().dot(flatToTarget.normalize());
    }

    public static boolean steerProjectile(final Projectile projectile, final LivingEntity owner, final boolean yawUpdated) {
        final Entity target = selectFocusTarget(owner);
        if (target == null) {
            return false;
        }

        final Vec3 velocity = projectile.getDeltaMovement();
        final double speed = velocity.horizontalDistance();
        boolean updated = false;
        if (!yawUpdated && speed > 1.0E-3) {
            final Vec3 toTarget = target.getBoundingBox().getCenter().subtract(projectile.position());
            final float currentYaw = (float) (Mth.atan2(velocity.z, velocity.x) * Mth.RAD_TO_DEG);
            final float targetYaw = (float) (Mth.atan2(toTarget.z, toTarget.x) * Mth.RAD_TO_DEG);
            final float newYaw = (currentYaw + Mth.clamp(Mth.wrapDegrees(targetYaw - currentYaw), -MAX_TURN_DEGREES, MAX_TURN_DEGREES)) * Mth.DEG_TO_RAD;
            projectile.setDeltaMovement(Mth.cos(newYaw) * speed, velocity.y, Mth.sin(newYaw) * speed);
            projectile.hurtMarked = true;
            updated = true;
        }

        if (target instanceof Projectile shot && projectile.distanceTo(shot) < SHOOTDOWN_RANGE) {
            shot.playSound(JSoundRegistry.BULLET_RICOCHET.get());
            shot.discard();
        }
        return updated;
    }

    @Override
    public @NonNull MoveType<RangerFocusMove> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull RangerFocusMove getThis() {
        return this;
    }

    @Override
    public @NonNull RangerFocusMove copy() {
        return copyExtras(new RangerFocusMove(getCooldown()));
    }

    public static class Type extends AbstractMove.Type<RangerFocusMove> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<RangerFocusMove>, RangerFocusMove> buildCodec(RecordCodecBuilder.Instance<RangerFocusMove> instance) {
            return instance.group(extras(), cooldown()).apply(instance, applyExtras(RangerFocusMove::new));
        }
    }
}
