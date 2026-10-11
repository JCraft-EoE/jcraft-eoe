package net.arna.jcraft.mixin.gravity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PistonMovingBlockEntity.class)
public abstract class PistonBlockEntityMixin {

    @WrapOperation(
            method = "moveEntityByPiston",
            at = @At(
                    value = "NEW",
                    target = "(DDD)Lnet/minecraft/world/phys/Vec3;",
                    ordinal = 0
            )
    )
    private static Vec3 jcraft$moveEntity_Vec3d_0(final double x, final double y, final double z, final Operation<Vec3> original, final Direction noClipDirection, final Entity entity, final double progress, final Direction direction) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        if (gravityDirection == Direction.DOWN) {
            return original.call(x, y, z);
        }
        return RotationUtil.vecWorldToPlayer(x, y, z, gravityDirection);
    }

}
