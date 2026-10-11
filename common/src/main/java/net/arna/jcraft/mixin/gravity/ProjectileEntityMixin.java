package net.arna.jcraft.mixin.gravity;

import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Projectile.class)
public abstract class ProjectileEntityMixin {

    @ModifyVariable(
            method = "shootFromRotation",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private float jcraft$modify_setProperties_pitch(final float value, final Entity user, final float yaw, final float roll, final float speed, final float divergence) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(user);
        if (gravityDirection == Direction.DOWN) {
            return value;
        }
        return RotationUtil.rotPlayerToWorld(user.getYRot(), user.getXRot(), gravityDirection).y;
    }

    @ModifyVariable(
            method = "shootFromRotation",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true
    )
    private float jcraft$modify_setProperties_yaw(final float value, final Entity user, final float pitch, final float roll, final float speed, final float divergence) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(user);
        if (gravityDirection == Direction.DOWN) {
            return value;
        }
        return RotationUtil.rotPlayerToWorld(user.getYRot(), user.getXRot(), gravityDirection).x;
    }

}
