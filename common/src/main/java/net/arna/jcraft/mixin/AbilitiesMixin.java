package net.arna.jcraft.mixin;

import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.mixin_logic.AbilitiesAddon;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Abilities.class)
public class AbilitiesMixin implements AbilitiesAddon {
    private @Unique Player player;

    @Inject(method = "getFlyingSpeed", at = @At("HEAD"), cancellable = true)
    private void jcraft$overrideFlightSpeedIfCreaming(CallbackInfoReturnable<Float> cir) {
        if (CreamEntity.isCreaming(player))
            cir.setReturnValue(CreamEntity.VOIDING_FLIGHT_SPEED);
    }

    @Override
    public Player jcraft$getPlayer() {
        return player;
    }

    @Override
    public void jcraft$setPlayer(Player player) {
        this.player = player;
    }
}
