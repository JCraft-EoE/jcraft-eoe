package net.arna.jcraft.mixin.client;

import net.arna.jcraft.common.item.Peacemaker;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class SwingSuppressionMixin {
    /**
     * Left click fires the gun through jcraft's own input map, but vanilla's attack still runs
     * alongside it and swings the arm. Cancelling here on the local player also stops the swing
     * packet, so nobody else sees it either.
     */
    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), cancellable = true)
    private void jcraft$suppressGunSwing(InteractionHand hand, boolean updateSelf, CallbackInfo ci) {
        if (!((Object) this instanceof LocalPlayer player)) {
            return;
        }

        if (player.getItemInHand(hand).getItem() instanceof Peacemaker) {
            ci.cancel();
        }
    }
}
