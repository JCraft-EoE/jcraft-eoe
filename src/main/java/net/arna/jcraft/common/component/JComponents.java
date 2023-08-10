package net.arna.jcraft.common.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.impl.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class JComponents implements EntityComponentInitializer {
    public static final ComponentKey<GravityComponent> GRAVITY_MODIFIER =
            ComponentRegistry.getOrCreate(JCraft.id("gravity_direction"), GravityComponent.class);
    public static final ComponentKey<StandComponent> STAND =
            ComponentRegistry.getOrCreate(JCraft.id("stand"), StandComponent.class);
    public static final ComponentKey<SpecComponent> SPEC =
            ComponentRegistry.getOrCreate(JCraft.id("spec"), SpecComponent.class);
    public static final ComponentKey<CooldownsComponent> COOLDOWNS =
            ComponentRegistry.getOrCreate(JCraft.id("cooldowns"), CooldownsComponent.class);
    public static final ComponentKey<TimeStopComponent> TIME_STOP =
            ComponentRegistry.getOrCreate(JCraft.id("time_stop"), TimeStopComponent.class);
    public static final ComponentKey<MiscComponent> MISC =
            ComponentRegistry.getOrCreate(JCraft.id("misc"), MiscComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(Entity.class, GRAVITY_MODIFIER, GravityComponentImpl::new);
        registry.registerFor(Entity.class, STAND, StandComponentImpl::new);
        registry.registerFor(PlayerEntity.class, SPEC, SpecComponentImpl::new);
        registry.registerFor(Entity.class, COOLDOWNS, CooldownsComponentImpl::new);
        registry.registerFor(Entity.class, TIME_STOP, TimeStopComponentImpl::new);
        registry.registerFor(Entity.class, MISC, MiscComponentImpl::new);
    }

    public static StandComponent getStandData(Entity entity) {
        return STAND.get(entity);
    }

    public static SpecComponent getSpecData(PlayerEntity player) {
        return SPEC.get(player);
    }

    public static CooldownsComponent getCooldowns(Entity entity) {
        return COOLDOWNS.get(entity);
    }

    public static TimeStopComponent getTimeStopData(Entity entity) {
        return TIME_STOP.get(entity);
    }

    public static MiscComponent getMiscData(Entity entity) {
        return MISC.get(entity);
    }
}
