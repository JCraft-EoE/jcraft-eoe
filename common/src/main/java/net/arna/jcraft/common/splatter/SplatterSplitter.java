package net.arna.jcraft.common.splatter;

import com.google.common.collect.Streams;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;
import net.arna.jcraft.common.util.extensions.VecExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static net.minecraft.core.Direction.*;

@ExtensionMethod(VecExtensions.class)
@UtilityClass
class SplatterSplitter {
    public static boolean isValidAnchor(Level world, BlockPos pos) {
        return world.getBlockState(pos).isCollisionShapeFullBlock(world, pos);
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

        boolean flipUvHorizontally = direction.getAxisDirection() == AxisDirection.NEGATIVE;
        Vec2 minUv = new Vec2(flipUvHorizontally ? 1 : 0, 0);
        Vec2 maxUv = new Vec2(flipUvHorizontally ? 0 : 1, 1);

        Pair<Axis, Axis> axes = getAxesForDirection(direction);
        Axis a1 = axes.first(), a2 = axes.second();

        float xRange = splatter.getXRange();
        float zRange = splatter.getZRange();

        Vec3 center = splatter.getPos();
        Vec3 min = add(add(center, a1, -xRange), a2, -zRange);
        Vec3 max = add(add(center, a1, xRange), a2, zRange);

        return Streams.stream(BlockPos.betweenClosed(BlockPos.containing(min), BlockPos.containing(max))).map(pos -> {
            double c1 = center.get(a1);
            double c2 = center.get(a2);
            int p1 = pos.get(a1);
            int p2 = pos.get(a2);

            double min1 = 1 - Mth.clamp(p1 + 1 - (c1 - xRange), 0, 1);
            double max1 = Mth.clamp(c1 + xRange - p1, 0, 1);
            double min2 = 1 - Mth.clamp(p2 + 1 - (c2 - zRange), 0, 1);
            double max2 = Mth.clamp(c2 + zRange - p2, 0, 1);

            // Ignore empty sections.
            if (Math.abs(max1 - min1) < 0.001 || Math.abs(max2 - min2) < 0.001) {
                return null;
            }

            min1 += pos.get(a1);
            max1 += pos.get(a1);
            min2 += pos.get(a2);
            max2 += pos.get(a2);

            float minU = calcUv(min1, c1, xRange, minUv.x, maxUv.x);
            float maxU = calcUv(max1, c1, xRange, minUv.x, maxUv.x);
            float minV = calcUv(min2, c2, zRange, minUv.y, maxUv.y);
            float maxV = calcUv(max2, c2, zRange, minUv.y, maxUv.y);

            Pair<Vec2, Vec2> uv = packUv(minU, maxU, minV, maxV, direction);
            return new SplatterSection(splatter.getWorld(), direction, pack((float) min1, (float) min2, a1, a2, center),
                    pack((float) max1, (float) max2, a1, a2, center), uv.left(), uv.right());
        })
        .filter(Objects::nonNull);
    }

    private static Vec3 add(Vec3 vec, Axis axis, double value) {
        return vec.add(axis == Axis.X ? value : 0, axis == Axis.Y ? value : 0, axis == Axis.Z ? value : 0);
    }

    private static void add(Vector3f vec, Axis axis, float value) {
        vec.add(axis == Axis.X ? value : 0, axis == Axis.Y ? value : 0, axis == Axis.Z ? value : 0);
    }

    private static Vector3f pack(float v1, float v2, Axis a1, Axis a2, Vec3 fallback) {
        Vector3f res = new Vector3f();
        for (Axis axis : Axis.values()) {
            if (axis == a1) {
                add(res, axis, v1);
            } else if (axis == a2) {
                add(res, axis, v2);
            } else {
                add(res, axis, (float) fallback.get(axis));
            }
        }
        return res;
    }

    private static Pair<Vec2, Vec2> packUv(float minU, float maxU, float minV, float maxV, Direction direction) {
        boolean flipU = direction == EAST;
        boolean flipV = direction == SOUTH;

        Vec2 min = new Vec2(flipU ? maxU : minU, flipV ? maxV : minV);
        Vec2 max = new Vec2(flipU ? minU : maxU, flipV ? minV : maxV);
        return Pair.of(min, max);
    }

    private static float calcUv(double sectionCoord, double centerCoord, float range, float min, float max) {
        return Mth.lerp((float) ((sectionCoord - centerCoord + range) / (2 * range)), min, max);
    }

    private static List<SplatterSection> wrap(Splatter splatter, Stream<SplatterSection> sections) {
        return sections.flatMap(section -> {
            // Wrap floating and covered sections around block faces.
            List<ObjectBooleanPair<Direction>> anchors = findAnchors(splatter, section);
            if (anchors == null) {
                return Stream.of(section); // No anchors, no special handling.
            }

            Stream.Builder<SplatterSection> res = Stream.builder();
            Vector3f minP = new Vector3f(section.getMinPos());
            Vector3f maxP = new Vector3f(section.getMaxPos());

            for (ObjectBooleanPair<Direction> anchor : anchors) {
                boolean inside = anchor.rightBoolean();
                res.add(wrapToFace(anchor.left(), section, minP, maxP, inside));
            }

            return res.build()
                    .peek(s -> {
                        // Try to get rid of the little seam caused by the offset between sections.
                        float offset = 2 * splatter.getOffset();
                        if (s.getDirection() == UP) {
                            s.getMinPos().add(offset, 0, -offset);
                            s.getMaxPos().add(offset, 0, offset);
                        } else {
                            s.getMaxPos().add(0, offset, 0);
                        }
                    });
        })
        .toList();
    }

    /**
     * Wraps a section from its current face onto a target face.
     * <p>
     * Three axes are involved in every wrap:
     * <ul>
     *   <li><b>Hinge axis</b> (shared by both faces) — coordinates stay unchanged.</li>
     *   <li><b>Target normal axis</b> — pinned to one edge of the section (which edge depends on
     *       the wrap direction and whether the section is inside a block).</li>
     *   <li><b>Source normal axis</b> — extended by the fold amount (the section's extent along
     *       the target's normal, which "folds" over onto the new face).</li>
     * </ul>
     */
    private static SplatterSection wrapToFace(Direction target, SplatterSection section, Vector3f min, Vector3f max, boolean inside) {
        Axis ta = target.getAxis();
        Axis sa = section.getDirection().getAxis();
        boolean tp = target.getAxisDirection() == AxisDirection.POSITIVE;
        boolean sp = section.getDirection().getAxisDirection() == AxisDirection.POSITIVE;

        float fold = max.getComponentAlongAxis(ta) - min.getComponentAlongAxis(ta);

        Vector3f newMin = new Vector3f(min);
        Vector3f newMax = new Vector3f(max);

        float pinValue = (inside == tp)
                ? max.getComponentAlongAxis(ta)
                : min.getComponentAlongAxis(ta);
        setAxis(newMin, ta, pinValue);
        setAxis(newMax, ta, pinValue);

        if (sp == inside) {
            setAxis(newMax, sa, max.getComponentAlongAxis(sa) + fold);
        } else {
            setAxis(newMin, sa, min.getComponentAlongAxis(sa) - fold);
        }

        return section.wrapped(target, newMin, newMax, getUvModification(target, section.getDirection()));
    }

    private static void setAxis(Vector3f vec, Axis axis, float value) {
        switch (axis) {
            case X -> vec.set(value, vec.y(), vec.z());
            case Y -> vec.set(vec.x(), value, vec.z());
            case Z -> vec.set(vec.x(), vec.y(), value);
        }
    }

    private static SplatterSection.UvModification getUvModification(Direction target, Direction source) {
        if (source == UP || target == UP) {
            return SplatterSection.UvModification.NONE;
        }

        if (source == DOWN || target == DOWN) {
            Direction horizontal = (source == DOWN) ? target : source;
            return horizontal.getAxis() == Axis.Z
                    ? SplatterSection.UvModification.V_FLIP
                    : SplatterSection.UvModification.U_FLIP;
        }

        return switch (target) {
            case NORTH -> source == WEST
                    ? SplatterSection.UvModification.FLIP_U_FLIP
                    : SplatterSection.UvModification.SWAP_U_FLIP;
            case SOUTH -> source == WEST
                    ? SplatterSection.UvModification.SWAP_U_FLIP
                    : SplatterSection.UvModification.SWAP_V_FLIP;
            case WEST -> source == NORTH
                    ? SplatterSection.UvModification.SWAP_U_FLIP
                    : SplatterSection.UvModification.SWAP_V_FLIP;
            case EAST -> source == NORTH
                    ? SplatterSection.UvModification.SWAP_V_FLIP
                    : SplatterSection.UvModification.SWAP_U_FLIP;
            default -> throw new IllegalStateException("Non-horizontal target in horizontal wrap: " + target);
        };
    }

    private static List<ObjectBooleanPair<Direction>> findAnchors(Splatter splatter, SplatterSection section) {
        // If the block below is a valid anchor and this section is not inside one, just render this section facing normally.
        BlockPos anchor = section.getBlockPos().relative(splatter.getDirection().getOpposite());
        if (isValidAnchor(splatter.getWorld(), anchor) && !isValidAnchor(splatter.getWorld(), section.getBlockPos())) {
            return null; // Null indicates no special anchor. Meaning just render facing the same direction as the splatter.
        }

        Pair<Axis, Axis> axes = getAxesForDirection(section.getDirection());
        Axis a1 = axes.first(), a2 = axes.second();

        // These directions are the directions the section will be facing when wrapped downwards.
        // They are in turn also the directions in which an anchor should be were this section wrapped upwards.
        Vector3f sectionCenter = section.getCenter();
        double dx = sectionCenter.getComponentAlongAxis(a1) - splatter.getPos().get(a1);
        double dz = sectionCenter.getComponentAlongAxis(a2) - splatter.getPos().get(a2);
        Direction horizontal = Direction.fromAxisAndDirection(a1, dx >= 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE);
        Direction vertical = Direction.fromAxisAndDirection(a2, dz >= 0 ? AxisDirection.POSITIVE : AxisDirection.NEGATIVE);

        return Stream.of(vertical, horizontal)
                .map(direction -> {
                    // First, check if we can wrap upwards.
                    if (isValidAnchor(splatter.getWorld(), section.getBlockPos())) {
                        return ObjectBooleanPair.of(direction.getOpposite(), true);
                    }

                    // Then, check if we can wrap downwards.
                    if (isValidAnchor(splatter.getWorld(), anchor.relative(direction.getOpposite()))) {
                        return ObjectBooleanPair.of(direction, false);
                    }

                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private static Pair<Axis, Axis> getAxesForDirection(Direction direction) {
        Axis axis = direction.getAxis();

        Axis a1 = switch (axis) {
            case X -> Axis.Y;
            case Y, Z -> Axis.X;
        };
        Axis a2 = switch (axis) {
            case Z -> Axis.Y;
            case X, Y -> Axis.Z;
        };

        return Pair.of(a1, a2);
    }
}
