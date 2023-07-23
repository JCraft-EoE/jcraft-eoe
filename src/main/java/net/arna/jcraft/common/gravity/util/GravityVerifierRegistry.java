package net.arna.jcraft.common.gravity.util;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.gravity.util.packet.GravityPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class GravityVerifierRegistry<T extends GravityPacket> {
    private final Map<Identifier, VerifierFunction<T>> map = new HashMap<>();

    public void register(Identifier id, VerifierFunction<T> func) {
        if (map.containsKey(id))
            JCraft.LOGGER.error(new Exception("Verifier function already set for identifier " + id));
        map.put(id, func);
    }

    @Nullable
    public VerifierFunction<T> get(Identifier id) {
        return map.get(id);
    }

    @FunctionalInterface
    public interface VerifierFunction<V extends GravityPacket> {
        boolean check(ServerPlayerEntity player, PacketByteBuf verifierInfo, V packet);
    }
}
