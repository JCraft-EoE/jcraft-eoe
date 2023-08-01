package net.arna.jcraft.common.splatter;

import net.arna.jcraft.registry.JPacketRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class JSplatterManager {
    private final World world;
    private final Set<Splatter> splatters = ConcurrentHashMap.newKeySet();

    public JSplatterManager(World world) {
        this.world = world;
    }

    /**
     * Adds a new splatter to the world with a default range of 0.5.
     * @param pos The position of this splatter
     * @param type The type of this splatter
     */
    public void addSplatter(Vec3d pos, SplatterType type) {
        addSplatter(pos, type, .5f);
    }

    /**
     * Adds a new splatter to the world with the given range in both the x and z direction.
     * @param pos The position of this splatter
     * @param type The type of this splatter
     * @param range The range of this splatter in both directions
     */
    public void addSplatter(Vec3d pos, SplatterType type, float range) {
        addSplatter(pos, type, range, range);
    }

    /**
     * Adds a new splatter to the world with optionally a different range in the x and z direction.
     * @param pos The position of this splatter
     * @param type The type of this splatter
     * @param xRange The range of this splatter on the x-axis
     * @param zRange The range of this splatter on the z-axis
     */
    public void addSplatter(Vec3d pos, SplatterType type, float xRange, float zRange) {
        pos = new Vec3d(pos.getX(), Math.floor(pos.getY()), pos.getZ());
        splatters.add(new Splatter(world, pos, type, xRange, zRange));
        if (world.isClient) return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeDouble(pos.getX());
        buf.writeDouble(pos.getY());
        buf.writeDouble(pos.getZ());
        buf.writeEnumConstant(type);
        buf.writeFloat(xRange);
        buf.writeFloat(zRange);

        // We already confirmed this is a server-world.
        for (ServerPlayerEntity player : PlayerLookup.around((ServerWorld) world, pos, 64))
            ServerPlayNetworking.send(player, JPacketRegistry.S2C_SPLATTER, buf);
    }

    public void tick() {
        splatters.forEach(Splatter::tick);
        splatters.removeIf(Splatter::isRemoved);
    }

    public void iterateSplatters(Consumer<Splatter> consumer) {
        splatters.forEach(consumer);
    }
}
