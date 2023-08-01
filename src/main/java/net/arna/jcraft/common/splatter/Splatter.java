package net.arna.jcraft.common.splatter;

import com.google.common.collect.Streams;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import lombok.Data;
import lombok.Getter;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Data
public class Splatter {
    public static final int MAX_AGE = 80;
    private final World world;
    private final Vec3d pos;
    private final SplatterType type;
    // Half of the width on the x-axis and half of the width on the z-axis.
    private final float xRange, zRange;
    private final List<SplatterSection> sections;
    @Getter(lazy = true)
    private final BlockPos anchor = new BlockPos(pos).down();
    private final float offset = (float) (Math.random() * 0.0019 + 0.0001); // To prevent z-fighting with anchor block and other splatters
    private int age;
    private boolean removed;

    Splatter(World world, Vec3d pos, SplatterType type, float xRange, float zRange) {
        this.world = world;
        this.pos = pos;
        this.type = type;
        this.xRange = xRange;
        this.zRange = zRange;
        sections = splitAndWrap();
    }

    public float getStrength(float tickDelta) {
        // For ages 0 to 60: 1f
        // For ages 61 to 80: lerp to 0
        return MathHelper.lerp(tickDelta, getStrength(age - 1), getStrength(age));
    }

    private static float getStrength(int age) {
        return MathHelper.clamp((MAX_AGE - age) / 20f, 0f, 1f);
    }

    public void tick() {
        if (removed) return;

        if (age++ == MAX_AGE) {
            removed = true;
            return;
        }

        if (!isValidAnchor(getAnchor())) removed = true;
        sections.stream()
                .filter(section -> !section.isRemoved())
                .forEach(SplatterSection::tick);
    }

    private boolean isValidAnchor(BlockPos pos) {
        return world.getBlockState(pos).isFullCube(world, pos);
    }

    private List<SplatterSection> splitAndWrap() {
        // Split into two methods for better readability.
        return wrap(split());
    }

    private Stream<SplatterSection> split() {
        // Min uv is at the min corner, max uv at the max corner.
        // Min = 0, 0; max = 1, 1 is no change
        // Min = 0, 1; max = 1, 0 is vertically flipped
        // Min = 1, 1; max = 0, 0 is vertically and horizontally flipped (180 ° rotation)
        // Min = 1, 0; max = 0, 1 is horizontally flipped
        Vec2f minUv = new Vec2f(0, 0);
        Vec2f maxUv = new Vec2f(1, 1);

        Vec3d center = this.pos;
        Vec3d min = center.add(-xRange, 0, -zRange);
        Vec3d max = center.add(xRange, 0, zRange);

        return Streams.stream(BlockPos.iterate(new BlockPos(min), new BlockPos(max)))
                .map(pos -> {
                    double minX = pos.getX() + (1 - MathHelper.clamp(pos.getX() + 1 - (center.getX() - xRange), 0, 1));
                    double maxX = pos.getX() + MathHelper.clamp(center.getX() + xRange - pos.getX(), 0, 1);
                    double minZ = pos.getZ() + (1 - MathHelper.clamp(pos.getZ() + 1 - (center.getZ() - zRange), 0, 1));
                    double maxZ = pos.getZ() + MathHelper.clamp(center.getZ() + zRange - pos.getZ(), 0, 1);

                    // Ignore empty sections.
                    if (Math.abs(maxX - minX) < 0.001 || Math.abs(maxZ - minZ) < 0.001) return null;

                    float minU = calcUv(minX, center.getX(), xRange, minUv.x, maxUv.x);
                    float maxU = calcUv(maxX, center.getX(), xRange, minUv.x, maxUv.x);
                    float minV = calcUv(minZ, center.getZ(), zRange, minUv.y, maxUv.y);
                    float maxV = calcUv(maxZ, center.getZ(), zRange, minUv.y, maxUv.y);

                    return new SplatterSection(world, Direction.UP, new Vec3f((float) minX, (float) pos.getY(), (float) minZ),
                            new Vec3f((float) maxX, (float) pos.getY(), (float) maxZ),
                            new Vec2f(minU, minV), new Vec2f(maxU, maxV));
                })
                .filter(Objects::nonNull);
    }

    private List<SplatterSection> wrap(Stream<SplatterSection> sections) {
        return sections.flatMap(section -> {
            // Wrap floating and covered sections around block faces.
            List<ObjectBooleanPair<Direction>> anchors = findAnchors(section);
            if (anchors == null) return Stream.of(section); // No anchors, no special handling.

            Stream.Builder<SplatterSection> res = Stream.builder();
            Vec3f minP = section.getMinPos().copy();
            Vec3f maxP = section.getMaxPos().copy();

            for (ObjectBooleanPair<Direction> anchor : anchors) {
                boolean up = anchor.rightBoolean();
                res.add(switch (anchor.left()) {
                    case NORTH -> {
                        float height = maxP.getZ() - minP.getZ();
                        yield up ?
                                section.wrapped(Direction.NORTH, minP, new Vec3f(maxP.getX(), maxP.getY() + height, minP.getZ())) :
                                section.wrapped(Direction.NORTH, new Vec3f(minP.getX(), minP.getY() - height, maxP.getZ()), maxP);
                    }
                    case WEST -> {
                        float height = maxP.getX() - minP.getX();
                        yield up ?
                                section.wrapped(Direction.WEST, minP, new Vec3f(minP.getX(), maxP.getY() + height, maxP.getZ())) :
                                section.wrapped(Direction.WEST, new Vec3f(maxP.getX(), minP.getY() - height, minP.getZ()), maxP);
                    }
                    case SOUTH -> {
                        float height = maxP.getZ() - minP.getZ();
                        yield up ?
                                section.wrapped(Direction.SOUTH, new Vec3f(minP.getX(), minP.getY(), maxP.getZ()),
                                        new Vec3f(maxP.getX(), maxP.getY() + height, maxP.getZ())) :
                                section.wrapped(Direction.SOUTH, new Vec3f(minP.getX(), minP.getY() - height, minP.getZ()),
                                        new Vec3f(maxP.getX(), maxP.getY(), minP.getZ()));
                    }
                    case EAST -> {
                        float height = maxP.getX() - minP.getX();
                        yield up ?
                                section.wrapped(Direction.EAST, new Vec3f(maxP.getX(), minP.getY(), minP.getZ()),
                                        new Vec3f(maxP.getX(), maxP.getY() + height, maxP.getZ())) :
                                section.wrapped(Direction.EAST, new Vec3f(minP.getX(), minP.getY() - height, minP.getZ()),
                                        new Vec3f(minP.getX(), maxP.getY(), maxP.getZ()));
                    }
                    // Down and up should be impossible.
                    default -> throw new IllegalStateException("Unexpected value: " + anchor);
                });
            }

            return res.build()
                    .peek(s -> {
                        // Try to get rid of the little seam caused by the offset between sections.
                        float offset = 2 * this.offset;
                        if (s.getDirection() == Direction.UP) {
                            s.getMinPos().add(offset, 0, -offset);
                            s.getMaxPos().add(offset, 0, offset);
                        } else s.getMaxPos().add(0, offset, 0);
                    });
        })
        .toList();
    }

    private static float calcUv(double sectionCoord, double centerCoord, float range, float min, float max) {
        return MathHelper.lerp((float) ((sectionCoord - centerCoord + range) / (2 * range)), min, max);
    }

    private List<ObjectBooleanPair<Direction>> findAnchors(SplatterSection section) {
        // If the block below is a valid anchor and this section is not inside one, just render this section facing up.
        BlockPos anchor = section.getBlockPos().down();
        if (isValidAnchor(anchor) && !isValidAnchor(section.getBlockPos()))
            return null; // Null indicates no special anchor. Meaning just render facing up.

        // These directions are the directions the section will be facing when wrapped downwards.
        // They are in turn also the directions in which an anchor should be were this section wrapped upwards.
        Vec3f sectionCenter = section.getCenter();
        double dx = sectionCenter.getX() - pos.getX();
        double dz = sectionCenter.getZ() - pos.getZ();
        Direction vertical = dz >= 0 ? Direction.SOUTH : Direction.NORTH;
        Direction horizontal = dx >= 0 ? Direction.EAST : Direction.WEST;

        return Stream.of(vertical, horizontal)
                .map(direction -> {
                    // First, check if we can wrap upwards.
                    if (isValidAnchor(section.getBlockPos()))
                        return ObjectBooleanPair.of(direction.getOpposite(), true);

                    // Then, check if we can wrap downwards.
                    if (isValidAnchor(anchor.offset(direction.getOpposite())))
                        return ObjectBooleanPair.of(direction, false);

                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
