package net.arna.jcraft.mixin_logic;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.api.RotationParameters;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ServerPlayerEntityMixinLogic {


    public static void inject_moveToWorld_sendPacket_1(ServerPlayer serverPlayer) {
        Direction gravityDirection = GravityChangerAPI.getGravityDirection(serverPlayer);
        if (gravityDirection != GravityChangerAPI.getDefaultGravityDirection(serverPlayer) && JCraft.gravityConfig.resetGravityOnDimensionChange) {
            GravityChangerAPI.setDefaultGravityDirection(serverPlayer, Direction.DOWN, new RotationParameters().rotationTime(0));
        } else {
            GravityChangerAPI.setDefaultGravityDirection(serverPlayer, GravityChangerAPI.getDefaultGravityDirection(serverPlayer), new RotationParameters().rotationTime(0));
        }
    }

    public static void resummonStandAfterWorldMove(ServerPlayer serverPlayer, boolean hadStand, ServerLevel destination, CallbackInfoReturnable<Entity> cir) {
        if (!hadStand) {
            return;
        }
        StandEntity<?, ?> stand = JCraft.summon(destination, serverPlayer);
        if (stand != null) {
            stand.setPlaySummonSound(false);
        }
    }

    public static void doNotPlayDesummonSoundWhenMovingWorld(ServerPlayer serverPlayer, ServerLevel destination, CallbackInfoReturnable<Entity> cir) {
        StandEntity<?, ?> stand = JUtils.getStand(serverPlayer);
        if (stand == null) {
            return;
        }

        stand.setPlayDesummonSound(false);
    }


}
