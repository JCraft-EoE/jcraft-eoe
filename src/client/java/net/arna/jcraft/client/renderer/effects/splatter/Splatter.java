package net.arna.jcraft.client.renderer.effects.splatter;

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
    private final List<SplatterSection> sections;
    @Getter(lazy = true)
    private final BlockPos anchor = new BlockPos(pos).down();
    private final float offset = (float) (Math.random() * 0.0004 + 0.0001); // To prevent z-fighting with anchor block and other splatters
    private int age;
    private boolean removed;

    public Splatter(World world, Vec3d pos, SplatterType type) {
        this.world = world;
        this.pos = pos;
        this.type = type;
        sections = split();
    }

    public float getStrength() {
        // For ages 0 to 60: 1f
        // For ages 61 to 80: lerp to 0
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

    private List<SplatterSection> split() {
        Stream.Builder<SplatterSection> sb = Stream.builder();
        Vec3d c, min, max;

        // c1 is top right, from there they follow a counter-clockwise order.
        // The uv values were found mostly through trial and error with a little bit of logic on the side.
        c = pos.add(.5, 0, .5);
        min = new Vec3d(getMinEdge(c.x), c.y, getMinEdge(c.z));
        max = c;
        sb.add(new SplatterSection(world, min, max, new Vec2f(1f - (float) (max.x - min.x), 1f - (float) (max.z - min.z)), new Vec2f(1f, 1f)));

        c = pos.add(.5, 0, -.5);
        min = new Vec3d(getMinEdge(c.x), c.y, c.z);
        max = new Vec3d(c.x, c.y, getMaxEdge(c.z));
        sb.add(new SplatterSection(world, min, max, new Vec2f(1f - (float) (max.x - min.x), 0f), new Vec2f(1f, (float) (max.z - min.z))));

        c = pos.add(-.5, 0, -.5);
        min = c;
        max = new Vec3d(getMaxEdge(c.x), c.y, getMaxEdge(c.z));
        sb.add(new SplatterSection(world, min, max, new Vec2f(0f, 0f), new Vec2f((float) (max.x - min.x), (float) (max.z - min.z))));

        c = pos.add(-.5, 0, .5);
        min = new Vec3d(c.x, c.y, getMinEdge(c.z));
        max = new Vec3d(getMaxEdge(c.x), c.y, c.z);
        sb.add(new SplatterSection(world, min, max, new Vec2f(0f, 1f - (float) (max.z - min.z)), new Vec2f((float) (max.x - min.x), 1f)));

        return sb.build()
                .distinct() // Some sections may be identical.
                .flatMap(section -> {
                    // Wrap floating sections around block faces.
                    List<ObjectBooleanPair<Direction>> anchors = findAnchors(section);
                    if (anchors == null) return Stream.of(section); // No anchors, no special handling.

                    Stream.Builder<SplatterSection> res = Stream.builder();
                    Vec3f minP = section.getMinPos();
                    Vec3f maxP = section.getMaxPos();

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
                                        section.wrapped(Direction.WEST, maxP, new Vec3f(minP.getX(), maxP.getY() + height, maxP.getZ())) :
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

                    return res.build();
                })
                .toList();

    }

    private float getMinEdge(double coord) {
        return (float) Math.max(Math.floor(coord), coord - 1);
    }

    private float getMaxEdge(double coord) {
        return (float) Math.min(Math.ceil(coord), coord + 1);
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
