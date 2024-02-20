package net.arna.jcraft.client.mixin;

import net.arna.jcraft.common.util.JUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Inject(method = "dropSelectedItem", at = @At(value = "HEAD"), cancellable = true)
    private void jcraft$dropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        if (!JUtils.canAct((ClientPlayerEntity) (Object) this))
            cir.cancel();
    }
}
