package net.arna.jcraft.client.mixin;

import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.common.util.IJSplatterManagerHolder;
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

import java.util.function.BooleanSupplier;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin implements IJSplatterManagerHolder {

    // Clientside timestop handling
    @Inject(cancellable = true, at = @At("HEAD"), method = "tickEntity")
    private void timestopTick(Entity entity, CallbackInfo ci) {
        JComponents.getTimeStopData(entity).tick(ci);
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

    @Inject(method = "tick", at = @At("RETURN"))
    private void tickSplatters(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        jcraft$getSplatterManager().tick();
    }
}
