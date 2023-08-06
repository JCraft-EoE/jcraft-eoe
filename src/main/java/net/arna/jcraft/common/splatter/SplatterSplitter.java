package net.arna.jcraft.common.splatter;

import com.google.common.collect.Streams;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.floats.FloatFloatPair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;
import net.arna.jcraft.common.util.extensions.VecExtensions;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static net.minecraft.util.math.Direction.*;

@ExtensionMethod(VecExtensions.class)
@UtilityClass
class SplatterSplitter {
    public static boolean isValidAnchor(World world, BlockPos pos) {
        return world.getBlockState(pos).isFullCube(world, pos);
    }

    public List<SplatterSection> splitAndWrap(Splatter splatter) {
        // Split into two methods for better readability.
        return wrap(splatter, split(splatter));
    }

    private Stream<SplatterSection> split(Splatter splatter) {
        Direction direction = splatter.getDirection();

        // Min uv is at the min corner, max uv at the max corner.
        // Min = 0, 0; max = 1, 1 is no change
        // Min = 0, 1; max = 1, 0 is vertically flipped
        // Min = 1, 1; max = 0, 0 is vertically and horizontally flipped (180 ° rotation)
        // Min = 1, 0; max = 0, 1 is horizontally flipped

        boolean flipUvHorizontally = direction.getDirection() == Direction.AxisDirection.NEGATIVE;
        Vec2f minUv = new Vec2f(flipUvHorizontally ? 1 : 0, 0);
        Vec2f maxUv = new Vec2f(flipUvHorizontally ? 0 : 1, 1);

        Pair<Direction.Axis, Direction.Axis> axes = getAxesForDirection(direction);
        Direction.Axis a1 = axes.first(), a2 = axes.second();

        float xRange = splatter.getXRange();
        float zRange = splatter.getZRange();

        Vec3d center = splatter.getPos();
        Vec3d min = add(add(center, a1, -xRange), a2, -zRange);
        Vec3d max = add(add(center, a1, xRange), a2, zRange);

        return Streams.stream(BlockPos.iterate(new BlockPos(min), new BlockPos(max))).map(pos -> {
            double c1 = center.getComponentAlongAxis(a1);
            double c2 = center.getComponentAlongAxis(a2);
            int p1 = pos.getComponentAlongAxis(a1);
            int p2 = pos.getComponentAlongAxis(a2);

            double min1 = 1 - MathHelper.clamp(p1 + 1 - (c1 - xRange), 0, 1);
            double max1 = MathHelper.clamp(c1 + xRange - p1, 0, 1);
            double min2 = 1 - MathHelper.clamp(p2 + 1 - (c2 - zRange), 0, 1);
            double max2 = MathHelper.clamp(c2 + zRange - p2, 0, 1);

            // Ignore empty sections.
            if (Math.abs(max1 - min1) < 0.001 || Math.abs(max2 - min2) < 0.001) return null;

            min1 += pos.getComponentAlongAxis(a1);
            max1 += pos.getComponentAlongAxis(a1);
            min2 += pos.getComponentAlongAxis(a2);
            max2 += pos.getComponentAlongAxis(a2);

            float minU = calcUv(min1, c1, xRange, minUv.x, maxUv.x);
            float maxU = calcUv(max1, c1, xRange, minUv.x, maxUv.x);
            float minV = calcUv(min2, c2, zRange, minUv.y, maxUv.y);
            float maxV = calcUv(max2, c2, zRange, minUv.y, maxUv.y);

            Pair<Vec2f, Vec2f> uv = packUv(minU, maxU, minV, maxV, direction);
            return new SplatterSection(splatter.getWorld(), direction, pack((float) min1, (float) min2, a1, a2, center),
                    pack((float) max1, (float) max2, a1, a2, center), uv.left(), uv.right());
        })
        .filter(Objects::nonNull);
    }

    private static Vec3d add(Vec3d vec, Direction.Axis axis, double value) {
        return vec.add(axis == Direction.Axis.X ? value : 0, axis == Direction.Axis.Y ? value : 0, axis == Direction.Axis.Z ? value : 0);
    }

    private static void add(Vec3f vec, Direction.Axis axis, float value) {
        vec.add(axis == Direction.Axis.X ? value : 0, axis == Direction.Axis.Y ? value : 0, axis == Direction.Axis.Z ? value : 0);
    }

    private static Vec3f pack(float v1, float v2, Direction.Axis a1, Direction.Axis a2, Vec3d fallback) {
        Vec3f res = new Vec3f();
        for (Direction.Axis axis : Direction.Axis.values()) {
            if (axis == a1) add(res, axis, v1);
            else if (axis == a2) add(res, axis, v2);
            else add(res, axis, (float) fallback.getComponentAlongAxis(axis));
        }
        return res;
    }

    private static Pair<Vec2f, Vec2f> packUv(float minU, float maxU, float minV, float maxV, Direction direction) {
        // TODO I'm not sure why these are necessary.
        boolean flipU = direction == EAST;
        boolean flipV = direction == SOUTH;

        Vec2f min = new Vec2f(flipU ? maxU : minU, flipV ? maxV : minV);
        Vec2f max = new Vec2f(flipU ? minU : maxU, flipV ? minV : maxV);
        return Pair.of(min, max);
    }

    private static float calcUv(double sectionCoord, double centerCoord, float range, float min, float max) {
        return MathHelper.lerp((float) ((sectionCoord - centerCoord + range) / (2 * range)), min, max);
    }

    private static List<SplatterSection> wrap(Splatter splatter, Stream<SplatterSection> sections) {
        return sections.flatMap(section -> {
            // Wrap floating and covered sections around block faces.
            List<ObjectBooleanPair<Direction>> anchors = findAnchors(splatter, section);
            if (anchors == null) return Stream.of(section); // No anchors, no special handling.

            Stream.Builder<SplatterSection> res = Stream.builder();
            Vec3f minP = section.getMinPos().copy();
            Vec3f maxP = section.getMaxPos().copy();

            for (ObjectBooleanPair<Direction> anchor : anchors) {
                boolean inside = anchor.rightBoolean();
                res.add(switch (anchor.left()) {
                    case UP -> wrapUp(section, minP, maxP, inside);
                    case DOWN -> wrapDown(section, minP, maxP, inside);
                    case NORTH -> wrapNorth(section, minP, maxP, inside);
                    case WEST -> wrapWest(section, minP, maxP, inside);
                    case SOUTH -> wrapSouth(section, minP, maxP, inside);
                    case EAST -> wrapEast(section, minP, maxP, inside);
                });
            }

            return res.build()
                    .peek(s -> {
                        // Try to get rid of the little seam caused by the offset between sections.
                        float offset = 2 * splatter.getOffset();
                        if (s.getDirection() == UP) {
                            s.getMinPos().add(offset, 0, -offset);
                            s.getMaxPos().add(offset, 0, offset);
                        } else s.getMaxPos().add(0, offset, 0);
                    });
        })
        .toList();
    }

    private static SplatterSection wrapUp(SplatterSection section, Vec3f min, Vec3f max, boolean inside) {
        FloatFloatPair dims = getDims(section.getDirection(), min, max);
        float width = dims.leftFloat(), height = dims.rightFloat();

        return switch (section.getDirection()) {
            case NORTH -> inside ?
                    section.wrapped(UP, new Vec3f(min.getX(), max.getY(), min.getZ() - height), max) :
                    section.wrapped(UP, min, new Vec3f(max.getX(), min.getY(), max.getZ() + height));
            case SOUTH -> inside ?
                    section.wrapped(UP, new Vec3f(min.getX(), max.getY(), min.getZ()),
                            new Vec3f(max.getX(), max.getY(), max.getZ() + height)) :
                    section.wrapped(UP, new Vec3f(min.getX(), min.getY(), min.getZ() - height),
                            new Vec3f(max.getX(), min.getY(), max.getZ()));
            case WEST -> inside ?
                    section.wrapped(UP, new Vec3f(min.getX() - width, max.getY(), min.getZ()), max) :
                    section.wrapped(UP, min, new Vec3f(max.getX() + width, min.getY(), max.getZ()));
            case EAST -> inside ?
                    section.wrapped(UP, new Vec3f(min.getX(), max.getY(), min.getZ()),
                            new Vec3f(max.getX() + width, max.getY(), max.getZ())) :
                    section.wrapped(UP, new Vec3f(min.getX() - width, min.getY(), min.getZ()),
                            new Vec3f(max.getX(), min.getY(), max.getZ()));
            // Up and down should be impossible
            default -> throw new IllegalStateException("Unexpected value: " + section.getDirection());
        };
    }

    private static SplatterSection wrapDown(SplatterSection section, Vec3f min, Vec3f max, boolean inside) {
        FloatFloatPair dims = getDims(section.getDirection(), min, max);
        float width = dims.leftFloat(), height = dims.rightFloat();

        return switch (section.getDirection()) {
            case NORTH -> inside ?
                    section.wrapped(DOWN, new Vec3f(min.getX(), min.getY(), min.getZ() - height),
                            new Vec3f(max.getX(), min.getY(), max.getZ()), SplatterSection.UvModification.V_FLIP) :
                    section.wrapped(DOWN, new Vec3f(min.getX(), max.getY(), min.getZ()),
                            new Vec3f(max.getX(), max.getY(), max.getZ() + height), SplatterSection.UvModification.V_FLIP);
            case SOUTH -> inside ?
                    section.wrapped(DOWN, min, new Vec3f(max.getX(), min.getY(), max.getZ() + height),
                            SplatterSection.UvModification.V_FLIP) :
                    section.wrapped(DOWN, new Vec3f(min.getX(), max.getY(), min.getZ() - height), max,
                            SplatterSection.UvModification.V_FLIP);
            case WEST -> inside ?
                    section.wrapped(DOWN, new Vec3f(min.getX() - width, min.getY(), min.getZ()),
                            new Vec3f(max.getX(), min.getY(), max.getZ()), SplatterSection.UvModification.U_FLIP) :
                    section.wrapped(DOWN, new Vec3f(min.getX(), max.getY(), min.getZ()),
                            new Vec3f(max.getX() + width, max.getY(), max.getZ()), SplatterSection.UvModification.U_FLIP);
            case EAST -> inside ?
                    section.wrapped(DOWN, min, new Vec3f(max.getX() + width, min.getY(), max.getZ()),
                            SplatterSection.UvModification.U_FLIP) :
                    section.wrapped(DOWN, new Vec3f(min.getX() - width, max.getY(), min.getZ()), max,
                            SplatterSection.UvModification.U_FLIP);
            // North and south should be impossible
            default -> throw new IllegalStateException("Unexpected value: " + section.getDirection());
        };
    }

    private static SplatterSection wrapNorth(SplatterSection section, Vec3f min, Vec3f max, boolean inside) {
        FloatFloatPair dims = getDims(section.getDirection(), min, max);
        float height = dims.rightFloat();

        return switch (section.getDirection()) {
            case UP -> inside ?
                    section.wrapped(NORTH, min, new Vec3f(max.getX(), max.getY() + height, min.getZ())) :
                    section.wrapped(NORTH, new Vec3f(min.getX(), min.getY() - height, max.getZ()), max);
            case DOWN -> inside ?
                    section.wrapped(NORTH, new Vec3f(min.getX(), min.getY() - height, min.getZ()),
                            new Vec3f(max.getX(), max.getY(), max.getZ() - height), SplatterSection.UvModification.V_FLIP) :
                    section.wrapped(NORTH, new Vec3f(min.getX(), min.getY(), max.getZ()),
                            new Vec3f(max.getX(), max.getY() + height, min.getZ()), SplatterSection.UvModification.V_FLIP);
            case WEST -> inside ?
                    section.wrapped(NORTH, new Vec3f(min.getX() - height, min.getY(), min.getZ()),
                            new Vec3f(max.getX(), max.getY(), min.getZ()), SplatterSection.UvModification.FLIP_U_FLIP) :
                    section.wrapped(NORTH, new Vec3f(min.getX() + height, min.getY(), max.getZ()), max,
                            SplatterSection.UvModification.FLIP);
            case EAST -> inside ?
                    section.wrapped(NORTH, min, new Vec3f(max.getX() + height, max.getY(), min.getZ()),
                            SplatterSection.UvModification.SWAP_U_FLIP) :
                    section.wrapped(NORTH, new Vec3f(min.getX() - height, min.getY(), max.getZ()),
                            new Vec3f(max.getX(), max.getY(), max.getZ()), SplatterSection.UvModification.SWAP_U_FLIP);
            // North and south should be impossible
            default -> throw new IllegalStateException("Unexpected value: " + section.getDirection());
        };
    }

    private static SplatterSection wrapWest(SplatterSection section, Vec3f min, Vec3f max, boolean inside) {
        FloatFloatPair dims = getDims(section.getDirection(), min, max);
        float width = dims.leftFloat();

        return switch (section.getDirection()) {
            case UP -> inside ?
                    section.wrapped(WEST, min, new Vec3f(max.getX(), max.getY() + width, max.getZ())) :
                    section.wrapped(WEST, new Vec3f(min.getX() + width, min.getY() - width, min.getZ()), max);
            case DOWN -> inside ?
                    section.wrapped(WEST, new Vec3f(min.getX(), min.getY() - width, min.getZ()),
                            max, SplatterSection.UvModification.U_FLIP) :
                    section.wrapped(WEST, new Vec3f(min.getX() + width, min.getY(), min.getZ()),
                            new Vec3f(max.getX(), max.getY() + width, max.getZ()), SplatterSection.UvModification.U_FLIP);
            case NORTH -> inside ?
                    section.wrapped(WEST, new Vec3f(min.getX(), min.getY(), min.getZ() - width),
                            new Vec3f(min.getX(), max.getY(), max.getZ()), SplatterSection.UvModification.SWAP_U_FLIP) :
                    section.wrapped(WEST, new Vec3f(max.getX(), min.getY(), min.getZ()),
                            new Vec3f(max.getX(), max.getY(), max.getZ() + width), SplatterSection.UvModification.SWAP_U_FLIP);
            case SOUTH -> inside ?
                    section.wrapped(WEST, min, new Vec3f(min.getX(), max.getY(), max.getZ() + width),
                            SplatterSection.UvModification.SWAP_V_FLIP) :
                    section.wrapped(WEST, new Vec3f(max.getX(), min.getY(), min.getZ()),
                            new Vec3f(max.getX(), max.getY(), max.getZ() - width), SplatterSection.UvModification.SWAP);
            // West and East should be impossible
            default -> throw new IllegalStateException("Unexpected value: " + section.getDirection());
        };
    }

    private static SplatterSection wrapSouth(SplatterSection section, Vec3f min, Vec3f max, boolean inside) {
        FloatFloatPair dims = getDims(section.getDirection(), min, max);
        float height = dims.rightFloat();

        return switch (section.getDirection()) {
            case UP -> inside ?
                    section.wrapped(SOUTH, new Vec3f(min.getX(), min.getY(), max.getZ()),
                            new Vec3f(max.getX(), max.getY() + height, max.getZ())) :
                    section.wrapped(SOUTH, new Vec3f(min.getX(), min.getY() - height, min.getZ()),
                            new Vec3f(max.getX(), max.getY(), min.getZ()));
            case DOWN -> inside ?
                    section.wrapped(SOUTH, new Vec3f(min.getX(), min.getY() - height, max.getZ()), max,
                            SplatterSection.UvModification.V_FLIP) :
                    section.wrapped(SOUTH, min, new Vec3f(max.getX(), max.getY() + height, min.getZ()),
                            SplatterSection.UvModification.V_FLIP);
            case WEST -> inside ?
                    section.wrapped(SOUTH, new Vec3f(min.getX() - height, min.getY(), max.getZ()), max,
                            SplatterSection.UvModification.SWAP_U_FLIP) :
                    section.wrapped(SOUTH, min, new Vec3f(max.getX() + height, max.getY(), min.getZ()),
                            SplatterSection.UvModification.SWAP_U_FLIP);
            case EAST -> inside ?
                    section.wrapped(SOUTH, new Vec3f(min.getX(), min.getY(), max.getZ()),
                            new Vec3f(max.getX() + height, max.getY(), max.getZ()), SplatterSection.UvModification.SWAP_V_FLIP) :
                    section.wrapped(SOUTH, new Vec3f(min.getX() - height, min.getY(), min.getZ()),
                            new Vec3f(max.getX(), max.getY(), min.getZ()), SplatterSection.UvModification.SWAP_V_FLIP);
            // South and north should be impossible
            default -> throw new IllegalStateException("Unexpected value: " + section.getDirection());
        };
    }

    private static SplatterSection wrapEast(SplatterSection section, Vec3f min, Vec3f max, boolean inside) {
        FloatFloatPair dims = getDims(section.getDirection(), min, max);
        float width = dims.leftFloat();

        return switch (section.getDirection()) {
            case UP -> inside ?
                    section.wrapped(EAST, new Vec3f(max.getX(), min.getY(), min.getZ()),
                            new Vec3f(max.getX(), max.getY() + width, max.getZ())) :
                    section.wrapped(EAST, new Vec3f(min.getX(), min.getY() - width, min.getZ()),
                            new Vec3f(min.getX(), max.getY(), max.getZ()));
            case DOWN -> inside ?
                    section.wrapped(EAST, new Vec3f(max.getX(), min.getY() - width, min.getZ()), max,
                            SplatterSection.UvModification.U_FLIP) :
                    section.wrapped(EAST, min, new Vec3f(min.getX(), max.getY() + width, max.getZ()),
                            SplatterSection.UvModification.U_FLIP);
            case NORTH -> inside ?
                    section.wrapped(EAST, new Vec3f(max.getX(), min.getY(), min.getZ() - width),
                            max, SplatterSection.UvModification.SWAP_V_FLIP) :
                    section.wrapped(EAST, min, new Vec3f(min.getX(), max.getY(), max.getZ() + width),
                            SplatterSection.UvModification.SWAP_V_FLIP);
            case SOUTH -> inside ?
                    section.wrapped(EAST, new Vec3f(max.getX(), min.getY(), min.getZ()),
                            new Vec3f(max.getX(), max.getY(), max.getZ() + width), SplatterSection.UvModification.SWAP_U_FLIP) :
                    section.wrapped(EAST, new Vec3f(min.getX(), min.getY(), min.getZ() - width),
                            new Vec3f(min.getX(), max.getY(), max.getZ()), SplatterSection.UvModification.SWAP_U_FLIP);
            // East and west should be impossible
            default -> throw new IllegalStateException("Unexpected value: " + section.getDirection());
        };
    }

    private static List<ObjectBooleanPair<Direction>> findAnchors(Splatter splatter, SplatterSection section) {
        // If the block below is a valid anchor and this section is not inside one, just render this section facing normally.
        BlockPos anchor = section.getBlockPos().offset(splatter.getDirection().getOpposite());
        if (isValidAnchor(splatter.getWorld(), anchor) && !isValidAnchor(splatter.getWorld(), section.getBlockPos()))
            return null; // Null indicates no special anchor. Meaning just render facing the same direction as the splatter.

        Pair<Direction.Axis, Direction.Axis> axes = getAxesForDirection(section.getDirection());
        Direction.Axis a1 = axes.first(), a2 = axes.second();

        // These directions are the directions the section will be facing when wrapped downwards.
        // They are in turn also the directions in which an anchor should be were this section wrapped upwards.
        Vec3f sectionCenter = section.getCenter();
        double dx = sectionCenter.getComponentAlongAxis(a1) - splatter.getPos().getComponentAlongAxis(a1);
        double dz = sectionCenter.getComponentAlongAxis(a2) - splatter.getPos().getComponentAlongAxis(a2);
        Direction horizontal = Direction.from(a1, dx >= 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
        Direction vertical = Direction.from(a2, dz >= 0 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);

        return Stream.of(vertical, horizontal)
                .map(direction -> {
                    // First, check if we can wrap upwards.
                    if (isValidAnchor(splatter.getWorld(), section.getBlockPos()))
                        return ObjectBooleanPair.of(direction.getOpposite(), true);

                    // Then, check if we can wrap downwards.
                    if (isValidAnchor(splatter.getWorld(), anchor.offset(direction.getOpposite())))
                        return ObjectBooleanPair.of(direction, false);

                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private static Pair<Direction.Axis, Direction.Axis> getAxesForDirection(Direction direction) {
        Direction.Axis axis = direction.getAxis();

        Direction.Axis a1 = switch (axis) {
            case X -> Direction.Axis.Y;
            case Y, Z -> Direction.Axis.X;
        };
        Direction.Axis a2 = switch (axis) {
            case Z -> Direction.Axis.Y;
            case X, Y -> Direction.Axis.Z;
        };

        return Pair.of(a1, a2);
    }

    private FloatFloatPair getDims(Direction facing, Vec3f min, Vec3f max) {
        Pair<Axis, Axis> axes = getAxesForDirection(facing);
        Axis a1 = axes.first(), a2 = axes.second();

        float width = max.getComponentAlongAxis(a1) - min.getComponentAlongAxis(a1);
        float height = max.getComponentAlongAxis(a2) - min.getComponentAlongAxis(a2);
        //noinspection SuspiciousNameCombination // Not the case here
        return FloatFloatPair.of(width, height);
    }
}
