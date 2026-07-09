package net.arna.jcraft.fabric.common.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.fabric.common.component.entity.GrabComponent;
import net.arna.jcraft.fabric.common.component.entity.GravityComponent;
import net.arna.jcraft.fabric.common.component.entity.TimeStopComponent;
import net.arna.jcraft.fabric.common.component.impl.GravityShiftComponentImpl;
import net.arna.jcraft.fabric.common.component.impl.living.VampireComponentImpl;
import net.arna.jcraft.fabric.common.component.impl.entity.GrabComponentImpl;
import net.arna.jcraft.fabric.common.component.impl.entity.GravityComponentImpl;
import net.arna.jcraft.fabric.common.component.impl.entity.TimeStopComponentImpl;
import net.arna.jcraft.fabric.common.component.impl.living.BombTrackerComponentImpl;
import net.arna.jcraft.fabric.common.component.impl.living.CooldownsComponentImpl;
import net.arna.jcraft.fabric.common.component.impl.living.HamonComponentImpl;
import net.arna.jcraft.fabric.common.component.impl.living.HitPropertyComponentImpl;
import net.arna.jcraft.fabric.common.component.impl.living.MiscComponentImpl;
import net.arna.jcraft.fabric.common.component.impl.living.StandComponentImpl;
import net.arna.jcraft.fabric.common.component.impl.player.PhComponentImpl;
import net.arna.jcraft.fabric.common.component.impl.player.SpecComponentImpl;
import net.arna.jcraft.fabric.common.component.impl.world.ShockwaveHandlerComponentImpl;
import net.arna.jcraft.fabric.common.component.impl.world.TexasHoldEmComponentImpl;
import net.arna.jcraft.fabric.common.component.living.BombTrackerComponent;
import net.arna.jcraft.fabric.common.component.living.CooldownsComponent;
import net.arna.jcraft.fabric.common.component.living.GravityShiftComponent;
import net.arna.jcraft.fabric.common.component.living.HamonComponent;
import net.arna.jcraft.fabric.common.component.living.HitPropertyComponent;
import net.arna.jcraft.fabric.common.component.living.MiscComponent;
import net.arna.jcraft.fabric.common.component.living.StandComponent;
import net.arna.jcraft.fabric.common.component.living.VampireComponent;
import net.arna.jcraft.fabric.common.component.player.PhComponent;
import net.arna.jcraft.fabric.common.component.player.SpecComponent;
import net.arna.jcraft.fabric.common.component.world.ShockwaveHandlerComponent;
import net.arna.jcraft.fabric.common.component.world.TexasHoldEmComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

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
    public static final ComponentKey<TexasHoldEmComponent> TEXAS_HOLD_EM =
            ComponentRegistry.getOrCreate(JCraft.id("texas_hold_em"), TexasHoldEmComponent.class);
    public static final ComponentKey<HamonComponent> HAMON =
            ComponentRegistry.getOrCreate(JCraft.id("hamon"), HamonComponent.class);
    public static final ComponentKey<VampireComponent> VAMPIRE =
            ComponentRegistry.getOrCreate(JCraft.id("vampire"), VampireComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(Entity.class, GRAVITY_MODIFIER, GravityComponentImpl::new);

        registry.beginRegistration(LivingEntity.class, STAND)
                .respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY)
                .impl(StandComponentImpl.class)
                .end(StandComponentImpl::new);
        registry.beginRegistration(LivingEntity.class, SPEC)
                .respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY)
                .impl(SpecComponentImpl.class)
                .end(SpecComponentImpl::new);

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
        registry.beginRegistration(LivingEntity.class, HAMON)
                .respawnStrategy(RespawnCopyStrategy.CHARACTER)
                .impl(HamonComponentImpl.class)
                .end(HamonComponentImpl::new);
    }

    @Override
    public void registerWorldComponentFactories(@NonNull WorldComponentFactoryRegistry registry) {
        registry.register(SHOCKWAVE_HANDLER, ShockwaveHandlerComponentImpl::new);
        registry.register(TEXAS_HOLD_EM, TexasHoldEmComponentImpl::new);
    }
}
