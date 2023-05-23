package net.arna.jcraft.mixin.client;

import net.arna.jcraft.common.entity.CreamEntity;
import net.arna.jcraft.common.util.ITimeStop;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.arna.jcraft.common.util.JCraftUtils.stopTick;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

    // Clientside timestop handling
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

    // Cream void deafness
    @Inject(method = "playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZ)V", at = @At("HEAD"), cancellable = true)
    private void playSound(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance, CallbackInfo info) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            if (player.getFirstPassenger() instanceof CreamEntity cream) {
                if (cream.getVoidTime() > 0) {
                    info.cancel();
                }
            }
        }
    }
}
