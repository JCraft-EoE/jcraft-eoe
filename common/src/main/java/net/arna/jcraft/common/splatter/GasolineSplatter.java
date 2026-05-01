package net.arna.jcraft.common.splatter;

import dev.architectury.event.events.common.TickEvent;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JSplatterTypeRegistry;
import net.arna.jcraft.api.splatter.Splatter;
import net.arna.jcraft.api.splatter.SplatterFactory;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GasolineSplatter extends Splatter {
    private static final ResourceLocation texture = JCraft.id("textures/effect/splatter/gasoline.png");
    private static long gasolineTickCount;
    private boolean litBefore = false;

    static {
        TickEvent.SERVER_POST.register(s -> gasolineTickCount++);
    }

    public GasolineSplatter(Level world, Vec3 pos, Direction direction, float xRange, float zRange, int maxAge, @Nullable LivingEntity creator) {
        super(world, pos, direction, xRange, zRange, maxAge, creator);
    }

    @Override
    public ResourceLocation getTexture() {
        return texture;
    }

    @Override
    public RegistrySupplier<SplatterFactory> getType() {
        return JSplatterTypeRegistry.GASOLINE_SPLATTER_TYPE;
    }

    @Override
    protected void baseTick(List<Runnable> toRunAfterTick) {
        if (gasolineTickCount % 2 != 0) return;

        List<BlockPos> posList = BlockPos.betweenClosedStream(getMainBox().inflate(1)).map(BlockPos::new).toList();
        boolean light = false;
        for (BlockPos pos : posList) {
            BlockState state = getWorld().getBlockState(pos);
            if (!state.is(BlockTags.FIRE)) continue;

            light = true;
            break;
        }

        if (!light) {
            // Found no fire, check whether there's an entity that's on fire in the vicinity.
            light = !getWorld().getEntities(EntityTypeTest.forClass(Entity.class),
                    getMainBox(), Entity::isOnFire).isEmpty();
        }

        if (!light) return;

        // We're lighting this baby up.
        for (SplatterSection section : getSections()) {
            lightSection(toRunAfterTick, section);
        }
    }

    public void lightOnFire() {
        List<Runnable> toRun = new ArrayList<>();
        for (SplatterSection section : getSections()) {
            lightSection(toRun, section);
        }

        toRun.forEach(Runnable::run);
    }

    private void lightSection(List<Runnable> toRunAfterTick, SplatterSection section) {
        Direction d = section.getDirection().getOpposite();
        List<BlockPos> toLight = BlockPos.betweenClosedStream(section.getHitBox())
                // Ensure we can place fire here
                .filter(p -> FireBlock.canBePlacedAt(getWorld(), p, d))
                // Ensure we're allowed to place fire here
                .filter(p -> getCreator() != null && JUtils.mayAlter(getWorld(), getCreator(), p, null, false))
                .map(BlockPos::new) // They're re-using a mutable object
                .toList();

        for (BlockPos posToLight : toLight) {
            BlockState toLightState = getWorld().getBlockState(posToLight);

            BlockState newState = toLightState.is(BlockTags.FIRE)
                    ? toLightState : Blocks.FIRE.defaultBlockState();

            // All directions false is down, there's no separate down prop on fire.
            if (d != Direction.DOWN) {
                BooleanProperty dirProp = PipeBlock.PROPERTY_BY_DIRECTION.get(d);
                newState = newState.setValue(dirProp, true);
            }

            // We place the fire after all splatters have been ticked so
            // a new fire block placed by this splatter won't cause another splatter
            // to light on fire in the same tick.
            final BlockState finalNewState = newState;
            toRunAfterTick.add(() -> {
                getWorld().setBlockAndUpdate(posToLight, finalNewState);
                if (!litBefore) {
                    getWorld().playSound(null, posToLight, SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 1f, 1f);
                    litBefore = true;
                }
            });
        }
    }
}
