package net.arna.jcraft.client.renderer.effects.splatter;

import lombok.Data;
import lombok.NonNull;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

@Data
public class SplatterSection {
    private final World world;
    private final Direction direction;
    private final @NonNull Vec3f minPos, maxPos;
    private final Vec3f center;
    private final BlockPos blockPos;
    private final Vec2f minUv, maxUv;
    private boolean removed;

    public SplatterSection(World world, Direction direction, @NotNull Vec3f minPos, @NotNull Vec3f maxPos, Vec2f minUv, Vec2f maxUv) {
        this.world = world;
        this.direction = direction;
        this.minPos = minPos;
        this.maxPos = maxPos;
        center = calcCenter(minPos, maxPos);
        blockPos = getAnchor(center, direction);
        this.minUv = minUv;
        this.maxUv = maxUv;
    }

    public SplatterSection(World world, Vec3d minPos, Vec3d maxPos, Vec2f minUv, Vec2f maxUv) {
        this(world, Direction.UP, new Vec3f(minPos), new Vec3f(maxPos), minUv, maxUv);
    }

    public static BlockPos getAnchor(Vec3f center, Direction facing) {
        return new BlockPos(new Vec3d(center).add(new Vec3d(facing.getUnitVector()).multiply(0.05)));
    }

    public static Vec3f calcCenter(Vec3f min, Vec3f max) {
        Vec3f center = min.copy();
        Vec3f delta = max.copy(); // Delta = (max - min) / 2
        delta.subtract(min);
        delta.modify(x -> x / 2f);
        center.add(delta); // (max - min) / 2 + min = center

        return center;
    }

    /**
     * Returns a version of this section wrapped around a vertical face.
     * Uses the same UVs as this section, but with different (vertical) coordinates.
     *
     * @param direction The direction this section faces
     * @param min       The minimum coordinates
     * @param max       The maximum coordinates
     * @return A wrapped version of this section
     */
    public SplatterSection wrapped(Direction direction, Vec3f min, Vec3f max) {
        return new SplatterSection(world, direction, min, max, minUv, maxUv);
    }

    public boolean hasValidAnchor() {
        // Direction offset check has to do with the section technically already being
        // in the block that's supposed to be its anchor in the case of north and west.
        BlockPos pos = blockPos.offset(direction.getOpposite(), 1);
        return world.getBlockState(pos).isFullCube(world, pos);
    }

    public void tick() {
        if (removed) return;
//        if (!hasValidAnchor()) removed = true;
    }

    @Override
    public String toString() {
        return "Section{" +
                "direction=" + direction +
                ", minPos=" + minPos +
                ", maxPos=" + maxPos +
                ", center=" + center +
                ", minUv=" + String.format("[%f, %f]", minUv.x, minUv.y) +
                ", maxUv=" + String.format("[%f, %f]", maxUv.x, maxUv.y) +
                '}';
    }
}
