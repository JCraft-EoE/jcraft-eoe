package net.arna.jcraft.mixin.client.gravity;

import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemPickupParticle.class)
public abstract class ItemPickupParticleMixin {

    @Shadow
    @Final
    private Entity target;

    @ModifyVariable(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getX()D",
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            ),
            ordinal = 0
    )
    private double jcraft$modify_buildGeometry_double_0(final double value) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.target);
        if (gravityDirection == Direction.DOWN) {
            return value;
        }
        return value + RotationUtil.vecPlayerToWorld(0d, 0.5, 0d, gravityDirection).x;
    }

    @ModifyVariable(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getX()D",
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            ),
            ordinal = 1
    )
    private double jcraft$modify_buildGeometry_double_1(final double value) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.target);
        if (gravityDirection == Direction.DOWN) {
            return value;
        }

        return value - 0.5D + RotationUtil.vecPlayerToWorld(0.0D, 0.5D, 0.0D, gravityDirection).y;
    }

    @ModifyVariable(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getX()D",
                    ordinal = 1,
                    shift = At.Shift.BEFORE
            ),
            ordinal = 2
    )
    private double jcraft$modify_buildGeometry_double_2(final double value) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.target);
        if (gravityDirection == Direction.DOWN) {
            return value;
        }
        return value + RotationUtil.vecPlayerToWorld(0d, 0.5, 0d, gravityDirection).z;
    }

}
