package net.arna.jcraft.common.splatter;

import lombok.Data;
import lombok.Getter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

@Data
public class Splatter {
    public static final int MAX_AGE = 80;
    public static boolean shouldTick = true; // TODO debug helper, remove this
    private final World world;
    private final Vec3d pos;
    private final Direction direction;
    private final SplatterType type;
    // Half of the width on the x-axis and half of the width on the z-axis.
    private final float xRange, zRange;
    private final List<SplatterSection> sections;
    @Getter(lazy = true)
    private final BlockPos anchor = new BlockPos(pos).down();
    private final float offset = (float) (Math.random() * 0.0019 + 0.0001); // To prevent z-fighting with anchor block and other splatters
    private int age;
    private boolean removed;

    Splatter(World world, Vec3d pos, Direction direction, SplatterType type, float xRange, float zRange) {
        this.world = world;
        this.pos = pos;
        this.direction = direction;
        this.type = type;
        this.xRange = xRange;
        this.zRange = zRange;
        sections = SplatterSplitter.splitAndWrap(this);
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
        if (removed || !shouldTick) return;

        if (age++ == MAX_AGE) {
            removed = true;
            return;
        }

        removed = sections.stream()
                .filter(section -> !section.isRemoved())
                .peek(SplatterSection::tick)
                .allMatch(SplatterSection::isRemoved);
    }
}
