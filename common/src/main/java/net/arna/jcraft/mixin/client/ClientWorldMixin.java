package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.common.util.IJSplatterManagerHolder;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Environment(EnvType.CLIENT)
@Mixin(ClientLevel.class)
public abstract class ClientWorldMixin implements IJSplatterManagerHolder {

    // Clientside timestop handling
    @WrapMethod(method = "tickNonPassenger")
    private void timestopTick(final Entity entity, final Operation<Void> original) {
        if (JComponentPlatformUtils.getTimeStopData(entity).isEmpty() || !JComponentPlatformUtils.getTimeStopData(entity).get().tick()) {
            original.call(entity);
        }
    }

    // Cream void deafness
    @WrapMethod(method = "playSound")
    private void playSound(final double x, final double y, final double z, final SoundEvent soundEvent, final SoundSource source, final float volume, final float pitch, final boolean distanceDelay, final long seed, final Operation<Void> original) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !(player.getFirstPassenger() instanceof CreamEntity cream) || cream.getVoidTime() <= 0) {
            original.call(x, y, z, soundEvent, source, volume, pitch, distanceDelay, seed);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tickSplatters(final BooleanSupplier hasTimeLeft, final CallbackInfo ci) {
        jcraft$getSplatterManager().tick();
    }

}
