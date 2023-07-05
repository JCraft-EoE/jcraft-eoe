package net.arna.jcraft.mixin;

import net.arna.jcraft.common.util.ITimeStop;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.arna.jcraft.common.util.JUtils.stopTick;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    // Serverside timestop handling
    @Inject(cancellable = true, at = @At("HEAD"), method = "tickEntity")
    private void timestopTick(Entity entity, CallbackInfo ci) {
        ITimeStop timeStop = (ITimeStop) entity;
        int tsTicks = timeStop.getTimeStopTicks();

        if (tsTicks > 0) {
            stopTick(entity);

            List<Entity> passengers = entity.getPassengerList();

            for (Entity passenger :
                    passengers) {
                stopTick(passenger);
            }

            timeStop.setTimeStopTicks(tsTicks - 1);
            ci.cancel();
        }
    }
}
