package net.arna.jcraft.common.splatter;

import it.unimi.dsi.fastutil.Pair;
import net.arna.jcraft.registry.JPacketRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;

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
    public Splatter addSplatter(Vec3d pos, SplatterType type) {
        return addSplatter(pos, type, .5f, null);
    }

    /**
     * Adds a new splatter to the world with the given range in both the x and z direction.
     * @param pos The position of this splatter
     * @param type The type of this splatter
     * @param range The range of this splatter in both directions
     */
    public Splatter addSplatter(Vec3d pos, SplatterType type, float range, @Nullable Entity creator) {
        return addSplatter(pos, type, range, range, creator);
    }

    /**
     * Adds a new splatter to the world with optionally a different range in the x and z direction.
     * @param pos The position of this splatter
     * @param type The type of this splatter
     * @param xRange The range of this splatter on the x-axis
     * @param zRange The range of this splatter on the z-axis
     */
    public Splatter addSplatter(Vec3d pos, SplatterType type, float xRange, float zRange, @Nullable Entity creator) {
        Pair<Vec3d, Direction> anchoredPos = anchorPos(pos);
        pos = anchoredPos.left();

        Splatter splatter = new Splatter(world, pos, anchoredPos.right().getOpposite(), type, xRange, zRange, creator);
        splatters.add(splatter);
        if (world.isClient) return splatter;

        PacketByteBuf buf = PacketByteBufs.create();
        writeSplatter(splatter, buf);

        // We already confirmed this is a server-world.
        for (ServerPlayerEntity player : PlayerLookup.around((ServerWorld) world, pos, 64))
            ServerPlayNetworking.send(player, JPacketRegistry.S2C_SPLATTER, buf);

        return splatter;
    }

    private Pair<Vec3d, Direction> anchorPos(Vec3d position) {
        BlockPos bPos = BlockPos.ofFloored(position);
        // Find the direction with the closest anchor
        Direction direction = Direction.stream()
                .filter(d -> SplatterSplitter.isValidAnchor(world, bPos.offset(d)))
                .min((d1, d2) -> Double.compare(getDistanceToAnchor(position, d1), getDistanceToAnchor(position, d2)))
                .orElse(Direction.DOWN);

        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();

        // Anchor the position to the block in the found direction.
        DoubleUnaryOperator modifier = direction.getDirection() == Direction.AxisDirection.POSITIVE ? Math::ceil : Math::floor;
        switch (direction.getAxis()) {
            case X -> x = modifier.applyAsDouble(x);
            case Y -> y = modifier.applyAsDouble(y);
            case Z -> z = modifier.applyAsDouble(z);
        }

        return Pair.of(new Vec3d(x, y, z), direction);
    }

    private double getDistanceToAnchor(Vec3d position, Direction direction) {
        DoubleUnaryOperator modifier = direction.getDirection() == Direction.AxisDirection.POSITIVE ? Math::ceil : Math::floor;
        return Math.abs(direction.getAxis().choose(
                position.getX() - modifier.applyAsDouble(position.getX()),
                position.getY() - modifier.applyAsDouble(position.getY()),
                position.getZ() - modifier.applyAsDouble(position.getZ())));
    }

    public void writeSplatter(Splatter splatter, PacketByteBuf buf) {
        Vec3d pos = splatter.getPos();
        buf.writeDouble(pos.getX());
        buf.writeDouble(pos.getY());
        buf.writeDouble(pos.getZ());
        buf.writeEnumConstant(splatter.getDirection());
        buf.writeEnumConstant(splatter.getType());
        buf.writeFloat(splatter.getXRange());
        buf.writeFloat(splatter.getZRange());
    }

    public Splatter readSplatter(PacketByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        Direction direction = buf.readEnumConstant(Direction.class);
        SplatterType type = buf.readEnumConstant(SplatterType.class);
        float xRange = buf.readFloat();
        float zRange = buf.readFloat();

        Splatter splatter = new Splatter(world, new Vec3d(x, y, z), direction, type, xRange, zRange, null); // At the moment, clients do not need to know who made a splatter
        splatters.add(splatter);
        return splatter;
    }

    public void tick() {
        splatters.forEach(Splatter::tick);
        splatters.removeIf(Splatter::isRemoved);
    }

    public void iterateSplatters(Consumer<Splatter> consumer) {
        splatters.forEach(consumer);
    }
}
