package net.arna.jcraft.common.splatter;

import dev.architectury.event.events.common.TickEvent;
import lombok.Data;
import lombok.Getter;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

@Data
public class Splatter {
    public static final int MAX_AGE = 80;
    private static long gasolineTickCount;
    private final Level world;
    private final Vec3 pos;
    private final Direction direction;
    private final SplatterType type;
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
    private boolean litThisTick = false;

    static {
        TickEvent.SERVER_POST.register(s -> gasolineTickCount++);
    }

    Splatter(Level world, Vec3 pos, Direction direction, SplatterType type, float xRange, float zRange, @Nullable LivingEntity creator) {
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

        if (age++ == type.getMaxAge()) {
            removed = true;
            return toRunAfterTick;
        }

        if (!world.isClientSide) {
            if (type == SplatterType.ACID && age % 4 == 0) {
                tickAcid();
            } else if (type == SplatterType.GASOLINE && gasolineTickCount % 2 == 0) {
                toRunAfterTick.addAll(tickGasoline());
            }
        }

        removed = sections.stream()
                .filter(section -> !section.isRemoved())
                .peek(SplatterSection::tick)
                .allMatch(SplatterSection::isRemoved);

        return toRunAfterTick;
    }

    private List<Runnable> tickGasoline() {
        List<Runnable> toRunAfterTick = new ArrayList<>();

        List<BlockPos> posList = BlockPos.betweenClosedStream(mainBox.inflate(1)).map(BlockPos::new).toList();
        boolean light = false;
        for (BlockPos pos : posList) {
            BlockState state = world.getBlockState(pos);
            if (!state.is(BlockTags.FIRE)) continue;

            light = true;
            break;
        }

        if (!light) {
            // Found no fire, check whether there's an entity that's on fire in the vicinity.
            light = !world.getEntities(EntityTypeTest.forClass(Entity.class),
                    getMainBox(), Entity::isOnFire).isEmpty();
        }

        if (!light) return toRunAfterTick;

        // We're lighting this baby up.
        for (SplatterSection section : sections) {
            Direction d = section.getDirection().getOpposite();
            List<BlockPos> toLight = BlockPos.betweenClosedStream(section.getHitBox())
                    // Ensure we can place fire here
                    .filter(p -> FireBlock.canBePlacedAt(world, p, d))
                    // Ensure we're allowed to place fire here
                    .filter(p -> creator != null && JUtils.mayAlter(world, creator, p, null))
                    .map(BlockPos::new) // They're re-using a mutable object
                    .toList();

            for (BlockPos posToLight : toLight) {
                BlockState toLightState = world.getBlockState(posToLight);

                BlockState newState = toLightState.is(BlockTags.FIRE)
                        ? toLightState : Blocks.FIRE.defaultBlockState();

                // All directions false is down, there's no separate down prop on fire.
                if (d != Direction.DOWN) {
                    BooleanProperty dirProp = PipeBlock.PROPERTY_BY_DIRECTION.get(d);
                    newState.setValue(dirProp, true);
                }

                // We place the fire after all splatters have been ticked so
                // a new fire block placed by this splatter won't cause another splatter
                // to light on fire in the same tick.
                toRunAfterTick.add(() -> {
                    world.setBlockAndUpdate(posToLight, newState);
                    world.playSound(null, posToLight, SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 1f, 1f);
                });
            }
        }

        return toRunAfterTick;
    }

    private void tickAcid() {
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
