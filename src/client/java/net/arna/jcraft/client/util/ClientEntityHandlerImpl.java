package net.arna.jcraft.client.util;

import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.network.c2s.StandControlPacket;
import net.arna.jcraft.common.util.IClientEntityHandler;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;

public class ClientEntityHandlerImpl implements IClientEntityHandler {
    public static final ClientEntityHandlerImpl INSTANCE = new ClientEntityHandlerImpl();

    private ClientEntityHandlerImpl() {}

    @Override
    public void playerCloneEntityClientTick(PlayerCloneEntity entity) {
        if (entity.age != 1 || entity.getType() != JEntityTypeRegister.PLAYER_ENTITY_CLONE) return;

        // If the one running this instance of tick() is the owner of the clone, check for a thin model and apply if found via server message
        // This is in fact an entirely clientside process and can be considered a "security flaw",
        // but I really doubt anyone would care if someone turned all their clones thin
        ClientPlayerEntity clientPlayer = MinecraftClient.getInstance().player;
        if (clientPlayer == null || !entity.getOwnerName().equals(clientPlayer.getName().getString()) || !clientPlayer.getModel().equals("slim"))
            return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(12);
        buf.writeUuid(entity.getUuid());
        ClientPlayNetworking.send(StandControlPacket.ID, buf);
    }
}
