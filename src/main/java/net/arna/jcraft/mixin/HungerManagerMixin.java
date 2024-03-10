package net.arna.jcraft.mixin;

import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.VampireComponent;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @Unique
    private VampireComponent vampireComponent;
    @Unique
    private boolean isVampire;
    @Unique private PlayerEntity player;

    @Inject(method = "getFoodLevel", at = @At("HEAD"), cancellable = true)
    void jcraft$getBloodLevel(CallbackInfoReturnable<Integer> cir) {
        if (this.isVampire)
            cir.setReturnValue((int) Math.floor(vampireComponent.getBlood()));
    }

    @Inject(method = "getSaturationLevel", at = @At("HEAD"), cancellable = true)
    void jcraft$getSaturationLevel(CallbackInfoReturnable<Float> cir) {
        if (this.isVampire)
            cir.setReturnValue(0f);
    }

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    void jcraft$updateVampirism(PlayerEntity player, CallbackInfo ci) {
        this.vampireComponent =JComponents.getVampirism(player);
        this.isVampire = vampireComponent.isVampire();
        this.player = player;

        if (isVampire) ci.cancel();
    }
}
