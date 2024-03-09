package net.arna.jcraft.common.attack.moves.vampire;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.projectile.AnkhProjectile;
import net.arna.jcraft.common.entity.projectile.LaserProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.spec.VampireSpec;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class SpaceRipperAttack extends AbstractMove<SpaceRipperAttack, VampireSpec> {
    public SpaceRipperAttack(int cooldown, int windup, int duration, float attackDistance) {
        super(cooldown, windup, duration, attackDistance);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(VampireSpec attacker, LivingEntity user, MoveContext ctx) {
        Vec3d rotVec = user.getRotationVector();

        for (int i = -1; i < 3; i += 2) {
            LaserProjectile laser = new LaserProjectile(attacker.getEntityWorld(), user);
            laser.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 2F, 0);

            Vec3d sideOffset = rotVec.rotateY(1.57079632679f * i).multiply(0.125);
            Vec3d offset = RotationUtil.vecPlayerToWorld(sideOffset.x, sideOffset.y + (double) user.getStandingEyeHeight(), sideOffset.z, GravityChangerAPI.getGravityDirection(user));
            Vec3d offsetHeightPos = attacker.getBaseEntity().getPos().add(offset);
            laser.setPosition(offsetHeightPos);

            attacker.getEntityWorld().spawnEntity(laser);
        }

        return Set.of();
    }

    @Override
    protected @NonNull SpaceRipperAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SpaceRipperAttack copy() {
        return copyExtras(new SpaceRipperAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
