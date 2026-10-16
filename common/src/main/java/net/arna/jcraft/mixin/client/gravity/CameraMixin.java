package net.arna.jcraft.mixin.client.gravity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.common.gravity.RotationAnimation;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.CompatMath;
import net.minecraft.client.Camera;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = Camera.class, priority = 1001)
public abstract class CameraMixin {

    @Shadow private Entity entity;

    @Shadow @Final private Quaternionf rotation;

    @Shadow private float eyeHeightOld; // lastCameraY

    @Shadow private float eyeHeight; // cameraY

    @Unique
    private float jcraft$storedTickDelta = 0f;

    @Inject(method = "setup", at = @At("HEAD"))
    private void jcraft$update(final BlockGetter level, final Entity entity, final boolean detached, final boolean thirdPersonReverse, float partialTick, final CallbackInfo ci) {
        jcraft$storedTickDelta = partialTick;
    }

    @WrapOperation(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setPosition(DDD)V",
                    ordinal = 0
            )
    )
    private void wrapOperation_update_setPos_0(final Camera camera, final double x, final double y, final double z, final Operation<Void> original, final BlockGetter level, final Entity entity, final boolean detached, final boolean thirdPersonReverse, final float partialTick) {
        final Direction gravityDirection = GravityChangerAPI.getGravityDirection(entity);
        final Optional<RotationAnimation> animationOptional = GravityChangerAPI.getGravityAnimation(entity);
        if (animationOptional.isEmpty()) {
            original.call(camera, x, y, z);
            return;
        }
        final RotationAnimation animation = animationOptional.get();
        if (gravityDirection == Direction.DOWN && !animation.isInAnimation()) {
            original.call(camera, x, y, z);
            return;
        }
        long timeMs = entity.level().getGameTime() * 50 + (long)(jcraft$storedTickDelta * 50);
        final Quaternionf gravityRotation = animation.getCurrentGravityRotation(gravityDirection, timeMs).conjugate();

        double entityX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double entityY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double entityZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());

        double currentCameraY = Mth.lerp(partialTick, this.eyeHeightOld, this.eyeHeight);

        Vector3f eyeOffset = new Vector3f(0, (float) currentCameraY, 0);
        eyeOffset.rotate(gravityRotation);

        original.call(
                camera,
                entityX + eyeOffset.x(),
                entityY + eyeOffset.y(),
                entityZ + eyeOffset.z()
        );
    }

    @Inject(
            method = "setRotation",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;",
                    shift = At.Shift.AFTER,
                    remap = false
            )
    )
    private void inject_setRotation(final CallbackInfo ci) {
        if (this.entity != null) {
            Direction gravityDirection = GravityChangerAPI.getGravityDirection(this.entity);
            Optional<RotationAnimation> animationOptional = GravityChangerAPI.getGravityAnimation(entity);
            if(animationOptional.isEmpty()) return;
            RotationAnimation animation = animationOptional.get();
            if (gravityDirection == Direction.DOWN && !animation.isInAnimation()) return;
            long timeMs = entity.level().getGameTime() * 50 + (long)(jcraft$storedTickDelta * 50);
            Quaternionf rotation = animation.getCurrentGravityRotation(gravityDirection, timeMs).conjugate();
            Quaternionf product = CompatMath.hamiltonProduct(rotation,this.rotation);
            this.rotation.set(product.x(), product.y(), product.z(), product.w());
        }
    }

}
