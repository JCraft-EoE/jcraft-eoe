package net.arna.jcraft.mixin;

import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.util.IJSplatterManagerHolder;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements IJSplatterManagerHolder {
    // Serverside timestop handling
    @Inject(cancellable = true, at = @At("HEAD"), method = "tickEntity")
    private void timestopTick(Entity entity, CallbackInfo ci) {
        JComponents.getTimeStopData(entity).tick(ci);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tickSplatters(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        jcraft$getSplatterManager().tick();
    }
}
