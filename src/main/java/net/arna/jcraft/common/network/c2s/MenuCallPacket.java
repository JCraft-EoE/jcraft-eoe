package net.arna.jcraft.common.network.c2s;

import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.screenhandler.MenuScreenHandler;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class MenuCallPacket {

    public static PacketByteBuf openScreenPacket() {
        final PacketByteBuf result = PacketByteBufs.create();
        result.writeBoolean(true);
        return result;
    }

    public static PacketByteBuf closeScreenPacket() {
        final PacketByteBuf result = PacketByteBufs.create();
        result.writeBoolean(false);
        return result;
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler network, PacketByteBuf buf, PacketSender sender) {
        if (buf.readBoolean()) { // if open
            player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                @Override
                public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                    final StandEntity<?, ?> stand = JUtils.getStand(player);
                    if (stand == null) {
                        buf.writeBoolean(false); // indicates no stand
                    }
                    else {
                        buf.writeBoolean(true);
                        // id
                        buf.writeInt(stand.getStandType().getId());
                    }
                }

                @Override
                public Text getDisplayName() {
                    return Text.literal("JCraft Info Screen");
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new MenuScreenHandler(syncId, null);
                }
            });
        }
        else {
            player.closeHandledScreen();
        }
    }
}
