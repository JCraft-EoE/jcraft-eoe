package net.arna.jcraft.common.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.entity.GrabComponent;
import net.arna.jcraft.common.component.entity.GravityComponent;
import net.arna.jcraft.common.component.entity.TimeStopComponent;
import net.arna.jcraft.common.component.impl.GravityShiftComponentImpl;
import net.arna.jcraft.common.component.impl.VampireComponentImpl;
import net.arna.jcraft.common.component.impl.entity.GrabComponentImpl;
import net.arna.jcraft.common.component.impl.entity.GravityComponentImpl;
import net.arna.jcraft.common.component.impl.entity.TimeStopComponentImpl;
import net.arna.jcraft.common.component.impl.living.*;
import net.arna.jcraft.common.component.impl.player.PhComponentImpl;
import net.arna.jcraft.common.component.impl.player.SpecComponentImpl;
import net.arna.jcraft.common.component.impl.world.ShockwaveHandlerComponentImpl;
import net.arna.jcraft.common.component.living.*;
import net.arna.jcraft.common.component.player.PhComponent;
import net.arna.jcraft.common.component.player.SpecComponent;
import net.arna.jcraft.common.component.world.ShockwaveHandlerComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class JComponents implements EntityComponentInitializer, WorldComponentInitializer {
    public static final ComponentKey<GravityComponent> GRAVITY_MODIFIER =
            ComponentRegistry.getOrCreate(JCraft.id("gravity_direction"), GravityComponent.class);
    public static final ComponentKey<StandComponent> STAND =
            ComponentRegistry.getOrCreate(JCraft.id("stand"), StandComponent.class);
    public static final ComponentKey<SpecComponent> SPEC =
            ComponentRegistry.getOrCreate(JCraft.id("spec"), SpecComponent.class);
    public static final ComponentKey<PhComponent> PH =
            ComponentRegistry.getOrCreate(JCraft.id("ph"), PhComponent.class);
    public static final ComponentKey<CooldownsComponent> COOLDOWNS =
            ComponentRegistry.getOrCreate(JCraft.id("cooldowns"), CooldownsComponent.class);
    public static final ComponentKey<TimeStopComponent> TIME_STOP =
            ComponentRegistry.getOrCreate(JCraft.id("time_stop"), TimeStopComponent.class);
    public static final ComponentKey<MiscComponent> MISC =
            ComponentRegistry.getOrCreate(JCraft.id("misc"), MiscComponent.class);
    public static final ComponentKey<BombTrackerComponent> BOMB_TRACKER =
            ComponentRegistry.getOrCreate(JCraft.id("bomb_tracker"), BombTrackerComponent.class);
    public static final ComponentKey<GrabComponent> GRAB =
            ComponentRegistry.getOrCreate(JCraft.id("grab"), GrabComponent.class);
    public static final ComponentKey<HitPropertyComponent> HIT_PROPERTY =
            ComponentRegistry.getOrCreate(JCraft.id("hit_property"), HitPropertyComponent.class);
    public static final ComponentKey<GravityShiftComponent> GRAVITY_SHIFT =
            ComponentRegistry.getOrCreate(JCraft.id("gravity_shift"), GravityShiftComponent.class);
    public static final ComponentKey<ShockwaveHandlerComponent> SHOCKWAVE_HANDLER =
            ComponentRegistry.getOrCreate(JCraft.id("shockwave_handler"), ShockwaveHandlerComponent.class);
    public static final ComponentKey<VampireComponent> VAMPIRE =
            ComponentRegistry.getOrCreate(JCraft.id("vampire"), VampireComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(Entity.class, GRAVITY_MODIFIER, GravityComponentImpl::new);
        registry.beginRegistration(LivingEntity.class, STAND)
                .respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY)
                .impl(StandComponentImpl.class)
                .end(StandComponentImpl::new);
        registry.registerForPlayers(SPEC, SpecComponentImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerForPlayers(PH, PhComponentImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.beginRegistration(LivingEntity.class, COOLDOWNS)
                .respawnStrategy(RespawnCopyStrategy.LOSSLESS_ONLY)
                .impl(CooldownsComponentImpl.class)
                .end(CooldownsComponentImpl::new);
        registry.registerFor(Entity.class, TIME_STOP, TimeStopComponentImpl::new);
        registry.beginRegistration(LivingEntity.class, MISC)
                .respawnStrategy(RespawnCopyStrategy.LOSSLESS_ONLY)
                .impl(MiscComponentImpl.class)
                .end(MiscComponentImpl::new);
        registry.beginRegistration(LivingEntity.class, BOMB_TRACKER)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .impl(BombTrackerComponentImpl.class)
                .end(BombTrackerComponentImpl::new);
        registry.beginRegistration(Entity.class, GRAB)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .impl(GrabComponentImpl.class)
                .end(GrabComponentImpl::new);
        registry.beginRegistration(LivingEntity.class, HIT_PROPERTY)
                .respawnStrategy(RespawnCopyStrategy.NEVER_COPY)
                .impl(HitPropertyComponentImpl.class)
                .end(HitPropertyComponentImpl::new);
        registry.beginRegistration(LivingEntity.class, GRAVITY_SHIFT)
                .respawnStrategy(RespawnCopyStrategy.CHARACTER)
                .impl(GravityShiftComponentImpl.class)
                .end(GravityShiftComponentImpl::new);
        registry.beginRegistration(LivingEntity.class, VAMPIRE)
                .respawnStrategy(RespawnCopyStrategy.CHARACTER)
                .impl(VampireComponentImpl.class)
                .end(VampireComponentImpl::new);
    }

    @Override
    public void registerWorldComponentFactories(@NotNull WorldComponentFactoryRegistry registry) {
        registry.register(SHOCKWAVE_HANDLER, ShockwaveHandlerComponentImpl::new);
    }

    public static StandComponent getStandData(LivingEntity entity) {
        return STAND.get(entity);
    }

    public static SpecComponent getSpecData(PlayerEntity player) {
        return SPEC.get(player);
    }

    public static PhComponent getPhData(PlayerEntity player) {
        return PH.get(player);
    }

    public static CooldownsComponent getCooldowns(LivingEntity entity) {
        return COOLDOWNS.get(entity);
    }

    public static TimeStopComponent getTimeStopData(Entity entity) {
        return TIME_STOP.get(entity);
    }

    public static MiscComponent getMiscData(LivingEntity entity) {
        return MISC.get(entity);
    }

    public static BombTrackerComponent getBombTracker(LivingEntity entity) {
        return BOMB_TRACKER.get(entity);
    }

    public static GrabComponent getGrab(LivingEntity entity) {
        return GRAB.get(entity);
    }

    public static HitPropertyComponent getHitProperties(LivingEntity livingEntity) {
        return HIT_PROPERTY.get(livingEntity);
    }

    public static GravityShiftComponent getGravityShift(LivingEntity livingEntity) {
        return GRAVITY_SHIFT.get(livingEntity);
    }

    public static ShockwaveHandlerComponent getShockwaveHandler(World world) {
        return SHOCKWAVE_HANDLER.get(world);
    }

    public static VampireComponent getVampirism(LivingEntity living) {
        return VAMPIRE.get(living);
    }
}
