package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.util.IJSplatterManagerHolder;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin implements IJSplatterManagerHolder {

    // Serverside timestop handling
    @WrapMethod(method = "tickNonPassenger")
    private void timestopTick(final Entity entity, final Operation<Void> original) {
        if (JComponentPlatformUtils.getTimeStopData(entity).isEmpty() || !JComponentPlatformUtils.getTimeStopData(entity).get().tick()) {
            original.call(entity);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tickSplatters(final BooleanSupplier hasTimeLeft, final CallbackInfo ci) {
        jcraft$getSplatterManager().tick();
    }

}
