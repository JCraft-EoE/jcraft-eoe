package net.arna.jcraft.common.splatter;

import lombok.Data;
import lombok.Getter;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiFunction;

@Data
public class Splatter {
    public static final int MAX_AGE = 80;
    private final Level world;
    private final Vec3 pos;
    private final Direction direction;
    private final SplatterType type;
    @Nullable
    private final Entity creator;
    // Half of the width on the x-axis and half of the width on the z-axis.
    private final float xRange, zRange;
    private final List<SplatterSection> sections;
    @Getter(lazy = true)
    private final BlockPos anchor = BlockPos.containing(pos).below();
    private final float offset = (float) (Math.random() * 0.0019 + 0.0001); // To prevent z-fighting with anchor block and other splatters
    private final AABB mainBox;
    private int age;
    private boolean removed;

    Splatter(Level world, Vec3 pos, Direction direction, SplatterType type, float xRange, float zRange, @Nullable Entity creator) {
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
        mainBox = new AABB(new Vec3(min), new Vec3(max)).inflate(.1);
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
        if (tickDelta <= 0.001) {
            return getStrength(type.getMaxAge(), age);
        }
        return Mth.lerpInt(tickDelta, (int) getStrength(type.getMaxAge(), age - 1), (int) getStrength(type.getMaxAge(), age));
    }

    private static float getStrength(int maxAge, int age) {
        return Mth.clamp((maxAge - age) / 20f, 0f, 1f);
    }

    public void tick() {
        if (removed) {
            return;
        }

        if (age++ == type.getMaxAge()) {
            removed = true;
            return;
        }

        if (!world.isClientSide) {
            if (type == SplatterType.ACID && age % 4 == 0) {
                for (LivingEntity hit : world.getEntitiesOfClass(LivingEntity.class, mainBox, EntitySelector.LIVING_ENTITY_STILL_ALIVE)) {
                    if (intersects(hit.getBoundingBox())) {
                        if (creator != null && (hit.isPassengerOfSameVehicle(creator) ||
                                (hit instanceof StandEntity<?, ?> stand && stand.getUser() == creator))) {
                            continue;
                        }
                        hit.addEffect(new MobEffectInstance(JStatusRegistry.WSPOISON.get(), 20, 0, true, false));
                        hit.hurt(JDamageSources.whitesnakePoison(creator), 2f);
                    }
                }
            }
        }

        removed = sections.stream()
                .filter(section -> !section.isRemoved())
                .peek(SplatterSection::tick)
                .allMatch(SplatterSection::isRemoved);
    }

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
