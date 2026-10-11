package net.arna.jcraft.mixin.gravity;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Direction.class)
public abstract class DirectionMixin {

    @WrapOperation(
            method = "orderedByNearest",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"
            )
    )
    private static float jcraft$getEntityFacingOrder_getYaw_0(final Entity instance, final float partialTick, final Operation<Float> original) {
        if (instance == null) {
            return 1f;
        }
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance, partialTick);
        }

        return RotationUtil.rotPlayerToWorld(original.call(instance, partialTick), instance.getViewXRot(partialTick), gravityDirection).x;
    }

    @WrapOperation(
            method = "orderedByNearest",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F"
            )
    )
    private static float jcraft$getEntityFacingOrder_getPitch_0(final Entity instance, final float partialTicks, final Operation<Float> original) {
        if (instance == null) {
            return 1f;
        }
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance, partialTicks);
        }
        return RotationUtil.rotPlayerToWorld(instance.getViewYRot(partialTicks), original.call(instance, partialTicks), gravityDirection).y;
    }

    @WrapOperation(
            method = "getFacingAxis",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F",
                    ordinal = 0
            )
    )
    private static float jcraft$getLookDirectionForAxis_getYaw_0(final Entity instance, final float partialTick, final Operation<Float> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance, partialTick);
        }
        return RotationUtil.rotPlayerToWorld(original.call(instance, partialTick), instance.getViewXRot(partialTick), gravityDirection).x;
    }

    @WrapOperation(
            method = "getFacingAxis",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F",
                    ordinal = 1
            )
    )
    private static float jcraft$getLookDirectionForAxis_getYaw_1(final Entity instance, final float partialTick, final Operation<Float> original) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance, partialTick);
        }
        return RotationUtil.rotPlayerToWorld(original.call(instance, partialTick), instance.getViewXRot(partialTick), gravityDirection).x;
    }

    @WrapOperation(
            method = "getFacingAxis",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F"
            )
    )
    private static float jcraft$getLookDirectionForAxis_getPitch_0(final Entity instance, final float partialTicks, final Operation<Float> original) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(instance);
        if (gravityDirection == Direction.DOWN) {
            return original.call(instance, partialTicks);
        }

        return RotationUtil.rotPlayerToWorld(instance.getViewYRot(partialTicks), original.call(instance, partialTicks), gravityDirection).y;
    }

}
