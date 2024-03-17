package net.arna.jcraft.common.attack.moves.vampire;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.IntMoveVariable;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.projectile.LaserProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.spec.VampireSpec;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class SpaceRipperAttack extends AbstractMove<SpaceRipperAttack, VampireSpec> {
    public static final IntMoveVariable CHARGE_TIME = new IntMoveVariable();

    public SpaceRipperAttack(int cooldown, int windup, int duration, float attackDistance) {
        super(cooldown, windup, duration, attackDistance);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(VampireSpec attacker, LivingEntity user, MoveContext ctx) {
        Vec3d rotVec = user.getRotationVector();

        int chargeTime = ctx.getInt(CHARGE_TIME);

        for (int i = -1; i < 3; i += 2) {
            LaserProjectile laser = new LaserProjectile(attacker.getEntityWorld(), user);
            laser.setVelocity(getRotVec(attacker).multiply(2 + (chargeTime - 15) / 10.0));

            Vec3d sideOffset = rotVec.rotateY(1.57079632679f * i).multiply(0.125);
            Vec3d offset = RotationUtil.vecPlayerToWorld(sideOffset.x, sideOffset.y + (double) user.getStandingEyeHeight(), sideOffset.z, GravityChangerAPI.getGravityDirection(user));
            Vec3d offsetHeightPos = attacker.getBaseEntity().getPos().add(offset);
            laser.setPosition(offsetHeightPos);

            if (chargeTime > 24)
                laser.setUnblockable(true);

            attacker.getEntityWorld().spawnEntity(laser);

            JComponents.getShockwaveHandler(user.getWorld()).addShockwave(offsetHeightPos, rotVec, 2);
        }

        return Set.of();
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(CHARGE_TIME);
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
