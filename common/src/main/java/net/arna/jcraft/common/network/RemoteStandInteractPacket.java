package net.arna.jcraft.common.network;

import dev.architectury.networking.NetworkManager;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.FakePlayer;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class RemoteStandInteractPacket {

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        ServerPlayer serverPlayer = (ServerPlayer) context.getPlayer();

        StandEntity<?, ?> stand = JUtils.getStand(serverPlayer);
        if (stand == null || !stand.isRemote()) {
            return;
        }
        ServerLevel world = (ServerLevel) serverPlayer.level();

        Vec3 eyePos = RotationUtil.vecPlayerToWorld(stand.getEyePosition(), GravityChangerAPI.getGravityDirection(stand));

        BlockHitResult hitResult = world.clip(
                new ClipContext(
                        eyePos,
                        eyePos.add(serverPlayer.getLookAngle().scale(5.0)),
                        ClipContext.Block.OUTLINE,
                        ClipContext.Fluid.NONE,
                        stand
                )
        );

        if (hitResult.getType() == HitResult.Type.MISS) {
            return;
        }
        BlockPos hitPos = hitResult.getBlockPos();

        Objects.requireNonNull(serverPlayer.getServer()).execute(() ->
                world.getBlockState(hitPos).use(world, new FakePlayer(world), InteractionHand.MAIN_HAND, hitResult));
    }
}
