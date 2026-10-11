package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.math.V3;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Unique
    private boolean jcraft$wasDetached = false;
    @Unique
    private final V3 jcraft$inertia = new V3();

    @Shadow
    private boolean detached;
    @Shadow
    private float eyeHeightOld;
    @Shadow
    private float eyeHeight;

    @Shadow @Final private BlockPos.MutableBlockPos blockPosition;

    @Shadow private Vec3 position;

    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V"))
    public void jcraft$prevSetPosUpdate(final Camera instance, final double x, final double y, final double z, final Operation<Void> original, final BlockGetter level, final Entity entity, final boolean detached, final boolean thirdPersonReverse, final float partialTick) {
        if (entity instanceof final LivingEntity living && living.hasEffect(JStatusRegistry.OUT_OF_BODY.get())) {
            this.detached = true;

            if (detached) {
                if (!jcraft$wasDetached) {
                    jcraft$wasDetached = true;
                    return;
                }
                // slide back
                jcraft$inertia.scale(0.98);

                CameraInvoker cameraInvoker = (CameraInvoker) this;
                cameraInvoker.invokeSetPos(
                        position.x + jcraft$inertia.x * partialTick,
                        position.y + jcraft$inertia.y * partialTick,
                        position.z + jcraft$inertia.z * partialTick
                );

                // bounce out of wall
                if (level.getBlockState(blockPosition).isSuffocating(level, blockPosition)) {
                    cameraInvoker.invokeSetPos(
                            position.x - 1.1 * jcraft$inertia.x * partialTick,
                            position.y - 1.1 * jcraft$inertia.y * partialTick,
                            position.z - 1.1 * jcraft$inertia.z * partialTick
                    );

                    jcraft$inertia.scale(-0.7);
                }
            } else { // initial launch
                jcraft$inertia.set(living.getLookAngle(), -0.4);
            }
        }
        else {
            original.call(instance, x, y, z);
        }

        jcraft$wasDetached = detached;
    }

    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", shift = At.Shift.AFTER))
    public void jcraft$afterSetPosUpdate(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        StandEntity<?, ?> stand = focusedEntity instanceof LivingEntity living ? JUtils.getStand(living) : null;
        if (stand != null && stand.isRemoteAndControllable()) {
            CameraInvoker cameraInvoker = (CameraInvoker) this;
            cameraInvoker.invokeSetPos(
                    Mth.lerp(tickDelta, stand.xo, stand.getX()),
                    Mth.lerp(tickDelta, stand.yo, stand.getY()) + (double) Mth.lerp(tickDelta, this.eyeHeightOld, this.eyeHeight),
                    Mth.lerp(tickDelta, stand.zo, stand.getZ())
            );

            detached = true;
        }
    }

}
