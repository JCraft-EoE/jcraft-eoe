package net.arna.jcraft.common.network.c2s;

import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.registry.JStatusRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class CooldownCancelPacket {

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        if (player.isCreative() || !player.hasStatusEffect(JStatusRegistry.DAZED))
            JComponents.getCooldowns(player).cooldownCancel();
    }
}
