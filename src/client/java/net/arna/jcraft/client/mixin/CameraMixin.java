package net.arna.jcraft.client.mixin;

import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    private BlockView area;
    @Shadow
    private Vec3d pos;
    @Final
    @Shadow
    private Vector3f verticalPlane;
    @Shadow
    private Entity focusedEntity;
    @Shadow
    private boolean thirdPerson;
    @Shadow
    private float lastCameraY;
    @Shadow
    private float cameraY;

    private double clipToSpaceVertical(double desiredCameraDistance) {
        for (int i = 0; i < 8; ++i) {
            float f = (float) ((i & 1) * 2 - 1);
            float g = (float) ((i >> 1 & 1) * 2 - 1);
            float h = (float) ((i >> 2 & 1) * 2 - 1);
            f *= 0.1F;
            g *= 0.1F;
            h *= 0.1F;
            Vec3d vec3d = pos.add(f, g, h);
            Vec3d vec3d2 = new Vec3d(pos.x - (double) verticalPlane.x() * desiredCameraDistance + (double) f + (double) h, pos.y - (double) verticalPlane.y() * desiredCameraDistance + (double) g, pos.z - (double) verticalPlane.z() * desiredCameraDistance + (double) h);
            HitResult hitResult = area.raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, this.focusedEntity));
            if (hitResult.getType() != HitResult.Type.MISS) {
                double d = hitResult.getPos().distanceTo(this.pos);
                if (d < desiredCameraDistance) {
                    desiredCameraDistance = d;
                }
            }
        }

        return desiredCameraDistance;
    }

    @Inject(method = "update", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift = At.Shift.BEFORE))
    public void jcraft$prevSetPosUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        if (focusedEntity instanceof LivingEntity living) {
            if (living.hasStatusEffect(JStatusRegistry.OUTOFBODY)) {
                this.thirdPerson = true;
                info.cancel();
            }
        }
    }

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V", shift = At.Shift.AFTER))
    public void jcraft$afterSetPosUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo info) {
        StandEntity<?, ?> stand = focusedEntity instanceof LivingEntity living ? JUtils.getStand(living) : null;
        if (stand != null && stand.isRemoteAndControllable()) {
            CameraInvoker cameraInvoker = (CameraInvoker) this;
            cameraInvoker.invokeSetPos(
                    new Vec3d(
                            MathHelper.lerp(tickDelta, stand.prevX, stand.getX()),
                            MathHelper.lerp(tickDelta, stand.prevY, stand.getY()) + (double) MathHelper.lerp(tickDelta, this.lastCameraY, this.cameraY),
                            MathHelper.lerp(tickDelta, stand.prevZ, stand.getZ())
                    )
            );
            this.thirdPerson = true;
        }

        /*
        if (!inverseView) {
            CameraInvoker cameraInvoker = (CameraInvoker) this;
            cameraInvoker.invokeMoveBy(-cameraInvoker.invokeClipToSpace(2.5D), 0, 0);
            cameraInvoker.invokeMoveBy(0, clipToSpaceVertical(0.75D), 0);
            info.cancel();
        }*/
    }
}
