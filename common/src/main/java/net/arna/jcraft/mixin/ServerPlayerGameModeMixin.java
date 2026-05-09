package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.arna.jcraft.api.misc.JBlockBreaker;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
    @Shadow @Final protected ServerPlayer player;
    @Shadow protected ServerLevel level;
    private @Unique float breakageAtStart;

    /**
     * Desummon stands when going in spectator mode.
     */
    @Inject(method = "changeGameModeForPlayer(Lnet/minecraft/world/level/GameType;)Z", at = @At(value = "TAIL"))
    public void jcraft$changeGameModeForPlayer(GameType gameModeForPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && gameModeForPlayer == GameType.SPECTATOR) {
            var stand = JUtils.getStand(player);
            if (stand != null) {
                stand.desummon();
            }
        }
    }

    @ModifyReturnValue(method = "incrementDestroyProgress", at = @At("RETURN"))
    private float addProgressFromBlockBreaker(float original, @Local(argsOnly = true) BlockPos pos) {
        return original + breakageAtStart;
    }

    @Inject(method = "handleBlockBreakAction", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayerGameMode;lastSentState:I", opcode = Opcodes.PUTFIELD))
    private void getBreakageAtBreakStart(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction face, int maxBuildHeight, int sequence, CallbackInfo ci) {
        breakageAtStart = JBlockBreaker.getBreakage(level, pos);
    }
}
