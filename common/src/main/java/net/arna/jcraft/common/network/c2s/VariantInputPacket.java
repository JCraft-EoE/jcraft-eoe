package net.arna.jcraft.common.network.c2s;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.WeakHashMap;

public class VariantInputPacket {
    private static final Map<ServerPlayer, State> VARIANT_STATES = new WeakHashMap<>();
    private static final State DEFAULT = new State(false, false);

    public static void handle(FriendlyByteBuf buf, NetworkManager.PacketContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.getPlayer();
        byte b = buf.readByte();
        boolean aerial = (b & 0x01) == 0x01;
        boolean crouch = (b & 0x02) == 0x02;
        State state = new State(aerial, crouch);
        VARIANT_STATES.put(player, state);
    }

    public static FriendlyByteBuf write(boolean aerial, boolean crouch) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        byte b = 0;
        if (aerial) b |= 0x01;
        if (crouch) b |= 0x02;
        buf.writeByte(b);
        return buf;
    }

    @NonNull
    public static State getState(ServerPlayer player) {
        return VARIANT_STATES.getOrDefault(player, DEFAULT);
    }

    public record State(boolean aerial, boolean crouch) {}
}
