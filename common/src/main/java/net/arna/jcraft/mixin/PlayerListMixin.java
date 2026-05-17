package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerListMixin {

    @Definition(id = "dx", local = @Local(type = double.class, ordinal = 4))
    @Definition(id = "dy", local = @Local(type = double.class, ordinal = 5))
    @Definition(id = "dz", local = @Local(type = double.class, ordinal = 6))
    @Definition(id = "radius", local = @Local(type = double.class, ordinal = 3))
    @Expression("dx * dx + dy * dy + dz * dz < radius * radius")
    @ModifyExpressionValue(method = "broadcast", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean sendSoundsToPlayerIfStandIsInRange(boolean original,
                                                       @Local(name = "serverPlayer") ServerPlayer serverPlayer,
                                                       @Local(argsOnly = true, ordinal = 0) double soundX,
                                                       @Local(argsOnly = true, ordinal = 1) double soundY,
                                                       @Local(argsOnly = true, ordinal = 2) double soundZ,
                                                       @Local(argsOnly = true, ordinal = 3) double radius,
                                                       @Local(argsOnly = true) Packet<?> packet) {
        // This method usually sends a packet if the player is within the radius of the sound source.
        // We want the player to hear sounds if their stand is able to hear them too,
        // so we check the stand's distance to the sound source (not to the player).
        if (original || !(packet instanceof ClientboundSoundPacket)) return original;

        StandEntity<?, ?> stand = JUtils.getStand(serverPlayer);
        if (stand == null) return false;

        double radiusSq = radius * radius;
        double standDistSq = stand.position().distanceToSqr(soundX, soundY, soundZ);

        return standDistSq < radiusSq;
    }
}
