package net.arna.jcraft.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.arna.jcraft.common.attack.moves.ranger.RangerFocusMove;
import net.arna.jcraft.common.spec.RangerSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin {

    // Colors the ranger's Focus targets red instead of the default white outline, only on the focusing player's client
    @ModifyReturnValue(method = "getTeamColor", at = @At("RETURN"))
    private int jcraft$rangerFocusOutlineColor(int original) {
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && JUtils.getSpec(player) instanceof RangerSpec &&
                JComponentPlatformUtils.getGunslinger(player).isFocusActive() &&
                RangerFocusMove.selectOutlineTarget(player) == (Entity) (Object) this) {
            return RangerFocusMove.GLOW_COLOR;
        }
        return original;
    }
}
