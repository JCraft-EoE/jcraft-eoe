package net.arna.jcraft.api.splatter;

import dev.architectury.registry.registries.RegistrySupplier;
import lombok.Data;
import lombok.Getter;
import net.arna.jcraft.common.splatter.SplatterSection;
import net.arna.jcraft.common.splatter.SplatterSplitter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;

@Data
public abstract class Splatter {
    public static final int MAX_AGE = 80;
    private final Level world;
    private final Vec3 pos;
    private final Direction direction;
    private final int maxAge;
    @Nullable
    private final LivingEntity creator;
    // Half of the width on the x-axis and half of the width on the z-axis.
    private final float xRange, zRange;
    private final List<SplatterSection> sections;
    @Getter(lazy = true)
    private final BlockPos anchor = BlockPos.containing(pos).below();
    private final float offset = (float) (Math.random() * 0.0019 + 0.0001); // To prevent z-fighting with anchor block and other splatters
    private final AABB mainBox;
    private int age;
    private boolean removed;
    // Number between 0 and 4 that determines the rotation of the texture.
    @Getter
    protected int rotation = new Random().nextInt(4);

    protected Splatter(Level world, Vec3 pos, Direction direction, float xRange, float zRange, int maxAge, @Nullable LivingEntity creator) {
        this.world = world;
        this.pos = pos;
        this.direction = direction;
        this.xRange = xRange;
        this.zRange = zRange;
        this.maxAge = maxAge;
        this.creator = creator;
        sections = SplatterSplitter.splitAndWrap(this);

        Vector3f min = findEdge(sections, false);
        Vector3f max = findEdge(sections, true);
        mainBox = new AABB(new Vec3(min), new Vec3(max)).inflate(.1);
    }

    public abstract ResourceLocation getTexture();

    public abstract RegistrySupplier<SplatterFactory> getType();

    private static Vector3f findEdge(List<SplatterSection> sections, boolean max) {
        float f = max ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
        BiFunction<Float, Float, Float> function = max ? Math::max : Math::min;

        return sections.stream()
                .map(max ? SplatterSection::getMaxPos : SplatterSection::getMinPos)
                .reduce(new Vector3f(f, f, f), (current, vec) -> {
                    float x = function.apply(current.x(), vec.x());
                    float y = function.apply(current.y(), vec.y());
                    float z = function.apply(current.z(), vec.z());
                    current.set(x, y, z);
                    return current;
                });
    }

    public float getStrength(float tickDelta) {
        if (tickDelta <= 0.001) {
            return getStrength(maxAge, age);
        }
        return Mth.lerp(tickDelta, getStrength(maxAge, age - 1), getStrength(maxAge, age));
    }

    private static float getStrength(int maxAge, int age) {
        // Always returns 1, except for the last 20 ticks of its lifetime.
        return Mth.clamp((maxAge - age) / 20f, 0f, 1f);
    }

    public List<Runnable> tick() {
        List<Runnable> toRunAfterTick = new ArrayList<>();

        if (removed) {
            return toRunAfterTick;
        }

        if (age++ == maxAge) {
            removed = true;
            return toRunAfterTick;
        }

        if (!world.isClientSide)
            baseTick(toRunAfterTick);

        removed = sections.stream()
                .filter(section -> !section.isRemoved())
                .peek(SplatterSection::tick)
                .allMatch(SplatterSection::isRemoved);

        return toRunAfterTick;
    }

    protected void baseTick(List<Runnable> toRunAfterTick) {}

    public boolean intersects(AABB box) {
        if (box == null || !mainBox.intersects(box)) {
            return false;
        }

        return sections.stream()
                .filter(section -> !section.isRemoved())
                .map(SplatterSection::getHitBox)
                .anyMatch(box::intersects);
    }
}
