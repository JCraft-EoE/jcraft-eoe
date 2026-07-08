package net.arna.jcraft.common.attack.moves.vampire;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.LaserProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class SpaceRipperAttack<A extends IAttacker<? extends A, ?>> extends AbstractMove<SpaceRipperAttack<A>, A> {
    public SpaceRipperAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull MoveType<SpaceRipperAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Vec3 rotVec = user.getLookAngle();

        int chargeTime = getChargeTime();

        //noinspection ConstantValue // what??
        for (int i = -1; i < 3; i += 2) {
            LaserProjectile laser = new LaserProjectile(attacker.getEntityWorld(), user);
            laser.setDeltaMovement(attacker.getBaseEntity().getLookAngle().scale(2 + (chargeTime - 15) / 10.0));

            final Vec3 sideOffset = rotVec.yRot(1.57079632679f * i).scale(0.125);
            final Vec3 offset = RotationUtil.vecPlayerToWorld(sideOffset.x, sideOffset.y + (double) user.getEyeHeight(), sideOffset.z, GravityChangerAPI.getGravityDirection(user));
            final Vec3 offsetHeightPos = attacker.getBaseEntity().position().add(offset);
            laser.setPos(offsetHeightPos);

            if (chargeTime > 24) {
                laser.setUnblockable(true);
            }

            attacker.getEntityWorld().addFreshEntity(laser);

            JComponentPlatformUtils.getShockwaveHandler(user.level()).addShockwave(offsetHeightPos, rotVec, 2);
        }

        JUtils.serverPlaySound(JSoundRegistry.VAMPIRE_LASER_FIRE.get(), (ServerLevel) user.level(), user.position(), 96);
        return Set.of();
    }

    @Override
    protected @NonNull SpaceRipperAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull SpaceRipperAttack<A> copy() {
        return copyExtras(new SpaceRipperAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<SpaceRipperAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SpaceRipperAttack<?>>, SpaceRipperAttack<?>> buildCodec(RecordCodecBuilder.Instance<SpaceRipperAttack<?>> instance) {
            return baseDefault(instance, SpaceRipperAttack::new);
        }
    }
}
