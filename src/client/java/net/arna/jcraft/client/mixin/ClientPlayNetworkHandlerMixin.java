package net.arna.jcraft.client.mixin;

import net.arna.jcraft.client.JClientConfig;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @ModifyArg(method = "onEntityPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;updateTrackedPositionAndAngles(DDDFFIZ)V"), index = 5)
    private int modifyInterpolationSteps(int original) {
        if (JClientConfig.getInstance().isClientsidePrediction())
            return 2; // 3 -> 2
        return original;
    }
}
