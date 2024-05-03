package net.arna.jcraft.common.network;

import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.FakePlayer;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class RemoteStandInteractPacket {

    public static void handle(MinecraftServer server, ServerPlayerEntity serverPlayer, ServerPlayNetworkHandler serverPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
        StandEntity<?, ?> stand = JUtils.getStand(serverPlayer);
        if (stand == null || !stand.isRemote()) return;
        ServerWorld world = (ServerWorld) serverPlayer.getWorld();

        Vec3d eyePos = RotationUtil.vecPlayerToWorld(stand.getEyePos(), GravityChangerAPI.getGravityDirection(stand));

        BlockHitResult hitResult = world.raycast(
                new RaycastContext(
                        eyePos,
                        eyePos.add(serverPlayer.getRotationVector().multiply(5.0)),
                        RaycastContext.ShapeType.OUTLINE,
                        RaycastContext.FluidHandling.NONE,
                        stand
                )
        );

        if (hitResult.getType() == HitResult.Type.MISS) return;
        BlockPos hitPos = hitResult.getBlockPos();
        world.getBlockState(hitPos).onUse(world, new FakePlayer(world), Hand.MAIN_HAND, hitResult);
    }
}
