package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arna.jcraft.common.entity.stand.CreamEntity;
import net.arna.jcraft.mixin_logic.AbilitiesAddon;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Abilities.class)
public abstract class AbilitiesMixin implements AbilitiesAddon {

    private @Unique Player jcraft$player;

    @WrapMethod(method = "getFlyingSpeed")
    private float jcraft$overrideFlightSpeedIfCreaming(final Operation<Float> original) {
        if (CreamEntity.isCreaming(jcraft$player)) {
            return CreamEntity.VOIDING_FLIGHT_SPEED;
        }
        return original.call();
    }

    @Override
    public Player jcraft$getPlayer() {
        return jcraft$player;
    }

    @Override
    public void jcraft$setPlayer(Player player) {
        this.jcraft$player = player;
    }

}
