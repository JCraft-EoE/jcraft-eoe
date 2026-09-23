package net.arna.jcraft.mixin.client;

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
    private boolean wasDetached = false;
    @Unique
    private final V3 inertia = new V3();

    @Shadow
    private boolean detached;
    @Shadow
    private float eyeHeightOld;
    @Shadow
    private float eyeHeight;
    @Shadow
    private BlockGetter level;

    @Shadow @Final private BlockPos.MutableBlockPos blockPosition;

    @Shadow private Vec3 position;

    @Inject(method = "setup", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", shift = At.Shift.BEFORE))
    public void jcraft$prevSetPosUpdate(BlockGetter area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {

        if (focusedEntity instanceof LivingEntity living) {
            if (living.hasEffect(JStatusRegistry.OUT_OF_BODY.get())) {
                detached = true;

                info.cancel();

                if (detached) {
                    if (wasDetached) { // slide back
                        inertia.scale(0.98);

                        CameraInvoker cameraInvoker = (CameraInvoker) this;
                        cameraInvoker.invokeSetPos(
                                position.x + inertia.x * tickDelta,
                                position.y + inertia.y * tickDelta,
                                position.z + inertia.z * tickDelta
                        );

                        // bounce out of wall
                        if (level.getBlockState(blockPosition).isSuffocating(area, blockPosition)) {
                            cameraInvoker.invokeSetPos(
                                    position.x - 1.1 * inertia.x * tickDelta,
                                    position.y - 1.1 * inertia.y * tickDelta,
                                    position.z - 1.1 * inertia.z * tickDelta
                            );

                            inertia.scale(-0.7);
                        }
                    } else { // initial launch
                        inertia.set(living.getLookAngle(), -0.4);
                    }
                }
            }
        }

        wasDetached = detached;
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
