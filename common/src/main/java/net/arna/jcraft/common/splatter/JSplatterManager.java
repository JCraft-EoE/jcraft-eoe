package net.arna.jcraft.common.splatter;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.Pair;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;

public class JSplatterManager {
    private final Level world;
    private final Set<Splatter> splatters = ConcurrentHashMap.newKeySet();

    public JSplatterManager(Level world) {
        this.world = world;
    }

    /**
     * Adds a new splatter to the world with a default range of 0.5.
     *
     * @param pos  The position of this splatter
     * @param type The type of this splatter
     */
    public Splatter addSplatter(Vec3 pos, SplatterType type) {
        return addSplatter(pos, type, .5f, null);
    }

    /**
     * Adds a new splatter to the world with the given range in both the x and z direction.
     *
     * @param pos   The position of this splatter
     * @param type  The type of this splatter
     * @param range The range of this splatter in both directions
     */
    public Splatter addSplatter(Vec3 pos, SplatterType type, float range, @Nullable LivingEntity creator) {
        return addSplatter(pos, type, range, range, creator);
    }

    /**
     * Adds a new splatter to the world with optionally a different range in the x and z direction.
     *
     * @param pos    The position of this splatter
     * @param type   The type of this splatter
     * @param xRange The range of this splatter on the x-axis
     * @param zRange The range of this splatter on the z-axis
     */
    public Splatter addSplatter(Vec3 pos, final SplatterType type, final float xRange, final float zRange,
                                @Nullable final LivingEntity creator) {
        final Pair<Vec3, Direction> anchoredPos = anchorPos(pos);
        pos = anchoredPos.left();

        final Splatter splatter = new Splatter(world, pos, anchoredPos.right().getOpposite(), type, xRange, zRange, creator);
        splatters.add(splatter);
        if (world.isClientSide) {
            return splatter;
        }

        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        writeSplatter(splatter, buf);

        // We already confirmed this is a server-world.
        NetworkManager.sendToPlayers(JUtils.around((ServerLevel) world, pos, 64), JPacketRegistry.S2C_SPLATTER, buf);

        return splatter;
    }

    private Pair<Vec3, Direction> anchorPos(Vec3 position) {
        BlockPos bPos = BlockPos.containing(position);
        // Find the direction with the closest anchor
        Direction direction = Direction.stream()
                .filter(d -> SplatterSplitter.isValidAnchor(world, bPos.relative(d)))
                .min((d1, d2) -> Double.compare(getDistanceToAnchor(position, d1), getDistanceToAnchor(position, d2)))
                .orElse(Direction.DOWN);

        double x = position.x();
        double y = position.y();
        double z = position.z();

        // Anchor the position to the block in the found direction.
        DoubleUnaryOperator modifier = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? Math::ceil : Math::floor;
        switch (direction.getAxis()) {
            case X -> x = modifier.applyAsDouble(x);
            case Y -> y = modifier.applyAsDouble(y);
            case Z -> z = modifier.applyAsDouble(z);
        }

        return Pair.of(new Vec3(x, y, z), direction);
    }

    private double getDistanceToAnchor(Vec3 position, Direction direction) {
        DoubleUnaryOperator modifier = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ? Math::ceil : Math::floor;
        return Math.abs(direction.getAxis().choose(
                position.x() - modifier.applyAsDouble(position.x()),
                position.y() - modifier.applyAsDouble(position.y()),
                position.z() - modifier.applyAsDouble(position.z())));
    }

    public void writeSplatter(Splatter splatter, FriendlyByteBuf buf) {
        Vec3 pos = splatter.getPos();
        buf.writeDouble(pos.x());
        buf.writeDouble(pos.y());
        buf.writeDouble(pos.z());
        buf.writeEnum(splatter.getDirection());
        buf.writeEnum(splatter.getType());
        buf.writeFloat(splatter.getXRange());
        buf.writeFloat(splatter.getZRange());
    }

    public Splatter readSplatter(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        Direction direction = buf.readEnum(Direction.class);
        SplatterType type = buf.readEnum(SplatterType.class);
        float xRange = buf.readFloat();
        float zRange = buf.readFloat();

        Splatter splatter = new Splatter(world, new Vec3(x, y, z), direction, type, xRange, zRange, null); // At the moment, clients do not need to know who made a splatter
        splatters.add(splatter);
        return splatter;
    }

    public void tick() {
        splatters.stream()
                .flatMap(s -> s.tick().stream())
                .toList()
                .forEach(Runnable::run);
        splatters.removeIf(Splatter::isRemoved);
    }

    public void iterateSplatters(Consumer<Splatter> consumer) {
        splatters.forEach(consumer);
    }
}
