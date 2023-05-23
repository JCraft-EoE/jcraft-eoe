package net.arna.jcraft.mixin;

import net.arna.jcraft.entity.StandEntity;
import net.arna.jcraft.util.ITimeStop;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class JCraftEntityMixin implements ITimeStop {
    // Timestop duration storage
    private int timeStopTicks = 0;
    @Override
    public int getTimeStopTicks() { return timeStopTicks; }
    @Override
    public void setTimeStopTicks(int tsTicks) { this.timeStopTicks = tsTicks; }

    // Two possible functions, therefore use signature
    @Inject(cancellable = true, at = @At("HEAD"), method = "Lnet/minecraft/entity/Entity;updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V")
    // Synced stand pos setup
    private void jcraft$updatePassengerPosition(Entity passenger, Entity.PositionUpdater positionUpdater, CallbackInfo info) {
        if (passenger instanceof StandEntity stand) {
            if (stand.getFree()) {
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

            double heightOffset = stand.getState() > 1 ? passenger.getVehicle().getRotationVector().y : 0;
            positionUpdater.accept(passenger, e.getX() + MathHelper.cos(y) * dist, d + heightOffset, e.getZ() + MathHelper.sin(y) * dist);
            info.cancel();
        }
    }

    /*
    @Inject(cancellable = true, at = @At("HEAD"), method = "Lnet/minecraft/entity/Entity;shouldRender(D)Z")
    public void shouldRender(double distance, CallbackInfoReturnable info) {
        if ( ((Entity)(Object)this).getFirstPassenger() instanceof KingCrimsonEntity kingCrimson ) {
            info.setReturnValue(kingCrimson.shouldRender(distance));
        }
        if ( ((ITrueInvis)this).getTrueInvis() ) { info.setReturnValue(false); }
    }
     */

    //POLISHING: stand position autosolver
}