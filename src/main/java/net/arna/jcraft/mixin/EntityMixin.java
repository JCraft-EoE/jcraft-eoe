package net.arna.jcraft.mixin;

import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    /**
     * Stand positioning mixin function
     * @param passenger stand entity
     */
    @Inject(method = "updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V", at = @At("HEAD"), cancellable = true)
    private void jcraft$updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater, CallbackInfo info) {
        if (passenger instanceof StandEntity<?, ?> stand) {
            if (stand.isFree() && !stand.isRemote()) {
                Vector3f freePos = stand.getFreePos();
                positionUpdater.accept(passenger, freePos.x(), freePos.y(), freePos.z());
                info.cancel();
                return;
            }

            Entity e = ((Entity) (Object) this);
            double dist = stand.getDistanceOffset();

            float y = e.getYaw() + stand.getRotationOffset();
            y *= (float) Math.PI / 180;

            double heightOffset = stand.shouldOffsetHeight() ? Vec3d.fromPolar(e.getPitch(), e.getYaw()).y : 0;
            Vec3d adjustedOffset = RotationUtil.vecPlayerToWorld(
                    MathHelper.cos(y) * dist,
                    passenger.getHeightOffset() + heightOffset,
                    MathHelper.sin(y) * dist,
                    GravityChangerAPI.getGravityDirection(e)
            );
            positionUpdater.accept(passenger, e.getX() + adjustedOffset.x, e.getY() + adjustedOffset.y, e.getZ() + adjustedOffset.z);
            info.cancel();
        }
    }

    /**
     * Disables sprinting particles during time erase
     */
    @SuppressWarnings("ConstantValue")
    @Inject(method = "shouldSpawnSprintingParticles", at = @At("HEAD"), cancellable = true)
    private void jcraft$shouldSpawnSprintingParticles(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LivingEntity living && JUtils.getStand(living) instanceof KingCrimsonEntity kc && kc.getTETime() > 0)
            cir.setReturnValue(false);
    }
    //todo (polishing): stand position autosolver

    @SuppressWarnings("ConstantValue")
    @Inject(method = "moveToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;copyFrom(Lnet/minecraft/entity/Entity;)V"))
    private void doNotPlayDesummonSoundWhenMovingWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        if (!((Object) this instanceof LivingEntity living)) return;

        StandEntity<?, ?> stand = JUtils.getStand(living);
        if (stand == null) return;

        stand.setPlayDesummonSound(false);
    }
}
