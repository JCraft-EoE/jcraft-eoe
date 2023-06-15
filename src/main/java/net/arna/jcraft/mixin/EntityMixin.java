package net.arna.jcraft.mixin;

import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.util.ITimeStop;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin implements ITimeStop {
    // Timestop duration storage
    private int timeStopTicks = 0;
    @Override
    public int getTimeStopTicks() {
        return timeStopTicks;
    }
    @Override
    public void setTimeStopTicks(int tsTicks) {
        this.timeStopTicks = tsTicks;
    }

    /**
     * Stand positioning mixin function
     * @param passenger stand entity
     */
    @Inject(method = "updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V", at = @At("HEAD"), cancellable = true)
    private void jcraft$updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater, CallbackInfo info) {
        if (passenger instanceof StandEntity stand) {
            if (stand.getFree() && !stand.getRemote()) {
                Vec3f freePos = stand.getFreePos();
                positionUpdater.accept(passenger, freePos.getX(), freePos.getY(), freePos.getZ());
                info.cancel();
                return;
            }

            Entity e = ((Entity) (Object) this);
            double d = e.getY() + passenger.getHeightOffset();
            double dist = stand.getDistanceOffset();

            float y = e.getYaw() + stand.getRotationOffset();
            y *= (float) Math.PI / 180;

            double heightOffset = stand.shouldOffsetHeight() ? e.getRotationVector().y : 0;
            positionUpdater.accept(passenger, e.getX() + MathHelper.cos(y) * dist, d + heightOffset, e.getZ() + MathHelper.sin(y) * dist);
            info.cancel();
        }
    }
    //todo (polishing): stand position autosolver
}