package net.arna.jcraft.common.network.c2s;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.arna.jcraft.JCraft.dashCD;

public class InputSyncPacket {
    public static final Identifier ID = JCraft.id("ispacket");

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        //JCraft.LOGGER.info("SERVER: Handling input packet from: " + player);
        int forward = 0;
        int side = 0;
        boolean jump;
        boolean dash;

        // W A S D Space Dash
        if (buf.readBoolean())
            forward += 1;
        if (buf.readBoolean())
            side += 1;
        if (buf.readBoolean())
            forward -= 1;
        if (buf.readBoolean())
            side -= 1;
        final int fF = forward;
        final int fS = side;
        jump = buf.readBoolean();
        dash = buf.readBoolean();

        server.execute(() -> {
            IEntityDataSaver playerData = ((IEntityDataSaver) player);
            playerData.updateRemoteInputs(fF, fS, jump);

            StandEntity stand = playerData.getStand();
            if (stand != null) stand.updateRemoteInputs(fF, fS, jump);

            if (dash) JCraft.tryDash(fF, fS, player);

            if (jump && JCraft.isDashing(player))
                playerData.getPersistentData().putInt(dashCD, 100); // 5s cooldown for superjumping
        });
    }
}
