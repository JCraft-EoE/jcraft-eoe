package net.arna.jcraft.common.network.s2c;

import net.arna.jcraft.common.util.JExplosionModifier;
import net.arna.jcraft.registry.JPacketRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.explosion.Explosion;

public class JExplosionPacket {
    public static void send(ServerPlayerEntity player, double x, double y, double z, float power, Explosion explosion, JExplosionModifier modifier) {
        ExplosionS2CPacket nativePacket = new ExplosionS2CPacket(x, y, z, power, explosion.getAffectedBlocks(), explosion.getAffectedPlayers().get(player));

        PacketByteBuf buf = PacketByteBufs.create();
        nativePacket.write(buf);
        if (modifier == null) buf.writeBoolean(false);
        else {
            buf.writeBoolean(true);
            modifier.write(buf, player.getWorld().random);
        }

        ServerPlayNetworking.send(player, JPacketRegistry.S2C_J_EXPLOSION, buf);
    }
}
