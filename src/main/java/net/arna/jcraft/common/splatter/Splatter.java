package net.arna.jcraft.common.splatter;

import lombok.Data;
import lombok.Getter;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiFunction;

@Data
public class Splatter {
    public static final int MAX_AGE = 80;
    private final World world;
    private final Vec3d pos;
    private final Direction direction;
    private final SplatterType type;
    @Nullable
    private final Entity creator;
    // Half of the width on the x-axis and half of the width on the z-axis.
    private final float xRange, zRange;
    private final List<SplatterSection> sections;
    @Getter(lazy = true)
    private final BlockPos anchor = BlockPos.ofFloored(pos).down();
    private final float offset = (float) (Math.random() * 0.0019 + 0.0001); // To prevent z-fighting with anchor block and other splatters
    private final Box mainBox;
    private int age;
    private boolean removed;

    Splatter(World world, Vec3d pos, Direction direction, SplatterType type, float xRange, float zRange, @Nullable Entity creator) {
        this.world = world;
        this.pos = pos;
        this.direction = direction;
        this.type = type;
        this.xRange = xRange;
        this.zRange = zRange;
        this.creator = creator;
        sections = SplatterSplitter.splitAndWrap(this);

        Vector3f min = findEdge(sections, false);
        Vector3f max = findEdge(sections, true);
        mainBox = new Box(new Vec3d(min), new Vec3d(max)).expand(.1);
    }

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
        if (tickDelta <= 0.001) return getStrength(type.getMaxAge(), age);
        return MathHelper.lerp(tickDelta, getStrength(type.getMaxAge(), age - 1), getStrength(type.getMaxAge(), age));
    }

    private static float getStrength(int maxAge, int age) {
        return MathHelper.clamp((maxAge - age) / 20f, 0f, 1f);
    }

    public void tick() {
        if (removed) return;

        if (age++ == type.getMaxAge()) {
            removed = true;
            return;
        }

        if (!world.isClient) {
            if (type == SplatterType.ACID && age % 4 == 0)
                for (LivingEntity hit : world.getEntitiesByClass(LivingEntity.class, mainBox, EntityPredicates.VALID_LIVING_ENTITY))
                    if (intersects(hit.getBoundingBox())) {
                        if (hit.isConnectedThroughVehicle(creator) || (hit instanceof StandEntity<?,?> stand && stand.getUser() == creator) )
                            continue;
                        hit.addStatusEffect(new StatusEffectInstance(JStatusRegistry.WSPOISON, 20, 0, true, false));
                        hit.damage(JDamageSources.whitesnakePoison(creator), 2f);
                    }
        }

        removed = sections.stream()
                .filter(section -> !section.isRemoved())
                .peek(SplatterSection::tick)
                .allMatch(SplatterSection::isRemoved);
    }

    public boolean intersects(Box box) {
        if (box == null || !mainBox.intersects(box)) return false;

        return sections.stream()
                .filter(section -> !section.isRemoved())
                .map(SplatterSection::getHitBox)
                .anyMatch(box::intersects);
    }
}
