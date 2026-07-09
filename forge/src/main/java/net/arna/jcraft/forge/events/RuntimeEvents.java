package net.arna.jcraft.forge.events;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.block.tile.CoffinTileEntity;
import net.arna.jcraft.api.component.JComponent;
import net.arna.jcraft.forge.capability.api.JCapabilityProvider;
import net.arna.jcraft.forge.capability.impl.entity.GrabCapability;
import net.arna.jcraft.forge.capability.impl.entity.GravityCapability;
import net.arna.jcraft.forge.capability.impl.entity.TimeStopCapability;
import net.arna.jcraft.forge.capability.impl.living.*;
import net.arna.jcraft.forge.capability.impl.player.PhCapability;
import net.arna.jcraft.forge.capability.impl.living.SpecCapability;
import net.arna.jcraft.forge.capability.impl.world.ShockwaveHandlerCapability;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RuntimeEvents {

    @SubscribeEvent
    public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity object = event.getObject();
        // When adding a new capability, make sure to add it to the syncEntityCapability and playerClone methods.
        if (object instanceof Player player) {
            event.addCapability(JCraft.id("ph_capability"), new JCapabilityProvider<>(PhCapability.CAPABILITY, () -> new PhCapability(player)));
        }

        if (object instanceof LivingEntity living) {
            event.addCapability(JCraft.id("bomb_capability"), new JCapabilityProvider<>(BombTrackerCapability.CAPABILITY, () -> new BombTrackerCapability(living)));
            event.addCapability(JCraft.id("cd_capability"), new JCapabilityProvider<>(CooldownsCapability.CAPABILITY, () -> new CooldownsCapability(living)));
            event.addCapability(JCraft.id("hit_capability"), new JCapabilityProvider<>(HitPropertyCapability.CAPABILITY, () -> new HitPropertyCapability(living)));
            event.addCapability(JCraft.id("misc_capability"), new JCapabilityProvider<>(MiscCapability.CAPABILITY, () -> new MiscCapability(living)));

            event.addCapability(JCraft.id("stand_capability"), new JCapabilityProvider<>(StandCapability.CAPABILITY, () -> new StandCapability(living)));

            event.addCapability(JCraft.id("hamon_capability"), new JCapabilityProvider<>(HamonCapability.CAPABILITY, () -> new HamonCapability(living)));
            event.addCapability(JCraft.id("vampire_capability"), new JCapabilityProvider<>(VampireCapability.CAPABILITY, () -> new VampireCapability(living)));

            event.addCapability(JCraft.id("gravity_shift_capability"), new JCapabilityProvider<>(GravityShiftCapability.CAPABILITY, () -> new GravityShiftCapability(living)));

            event.addCapability(JCraft.id("spec_capability"), new JCapabilityProvider<>(SpecCapability.CAPABILITY, () -> new SpecCapability(living)));
        }

        event.addCapability(JCraft.id("gravity_capability"), new JCapabilityProvider<>(GravityCapability.CAPABILITY, () -> new GravityCapability(object)));
        event.addCapability(JCraft.id("grab_capability"), new JCapabilityProvider<>(GrabCapability.CAPABILITY, () -> new GrabCapability(object)));
        event.addCapability(JCraft.id("time_capability"), new JCapabilityProvider<>(TimeStopCapability.CAPABILITY, () -> new TimeStopCapability(object)));
    }

    @SubscribeEvent
    public static void attachWorldCapability(AttachCapabilitiesEvent<Level> event) {
        event.addCapability(JCraft.id("shock_capability"), new JCapabilityProvider<>(ShockwaveHandlerCapability.CAPABILITY, () -> new ShockwaveHandlerCapability(event.getObject())));
    }

    @SubscribeEvent
    public static void entityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PhCapability.syncEntityCapability(serverPlayer);
            SpecCapability.syncEntityCapability(serverPlayer);
        }
        TimeStopCapability.getCapability(event.getEntity());
    }

    @SubscribeEvent
    public static void syncEntityCapability(PlayerEvent.StartTracking event) {
        // GrabCapability.syncEntityCapability(event);
        TimeStopCapability.syncEntityCapability(event);

        // BombTrackerCapability.syncEntityCapability(event);
        CooldownsCapability.syncEntityCapability(event);
        // HitPropertyCapability.syncEntityCapability(event);
        // MiscCapability.syncEntityCapability(event);
        StandCapability.syncEntityCapability(event);
        // VampireCapability.syncEntityCapability(event);
        PhCapability.syncEntityCapability(event);
        SpecCapability.syncEntityCapability(event);
        GravityCapability.syncEntityCapability(event);
        // GravityShiftCapability.syncEntityCapability(event);
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        PhCapability.getCapabilityOptional(event.player).ifPresent(c -> {});
        SpecCapability.getCapabilityOptional(event.player).ifPresent(c -> {});
    }

    @SubscribeEvent
    public static void playerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        copyCapability(PhCapability.CAPABILITY, event.getOriginal(), event.getEntity());
        copyCapability(SpecCapability.CAPABILITY, event.getOriginal(), event.getEntity());
        copyCapability(BombTrackerCapability.CAPABILITY, event.getOriginal(), event.getEntity());
        copyCapability(CooldownsCapability.CAPABILITY, event.getOriginal(), event.getEntity());
        copyCapability(HitPropertyCapability.CAPABILITY, event.getOriginal(), event.getEntity());
        copyCapability(MiscCapability.CAPABILITY, event.getOriginal(), event.getEntity());
        copyCapability(StandCapability.CAPABILITY, event.getOriginal(), event.getEntity());
        copyCapability(HamonCapability.CAPABILITY, event.getOriginal(), event.getEntity());
        copyCapability(VampireCapability.CAPABILITY, event.getOriginal(), event.getEntity());
        copyCapability(GrabCapability.CAPABILITY, event.getOriginal(), event.getEntity());
        copyCapability(TimeStopCapability.CAPABILITY, event.getOriginal(), event.getEntity());
        event.getOriginal().invalidateCaps();
    }

    private static <C extends JComponent> void copyCapability(Capability<C> cap, Entity from, Entity to) {
        from.getCapability(cap).ifPresent(c -> to.getCapability(cap).ifPresent(c2 -> {
            CompoundTag tag = new CompoundTag();
            c.writeToNbt(tag);
            c2.readFromNbt(tag);
        }));
    }

    @SubscribeEvent
    public static void coffinSleepCheck(SleepingTimeCheckEvent event) {
        Level world = event.getEntity().level();
        if (event.getSleepingLocation().isPresent()) {
            BlockPos pos = event.getSleepingLocation().get();
            BlockEntity bed = world.getBlockEntity(pos);
            if (bed instanceof CoffinTileEntity) {
                if (world.isDay()) {
                    event.setResult(Event.Result.ALLOW);
                }
                else {
                    event.setResult(Event.Result.DENY);
                }
            }
        }
    }

    @SubscribeEvent
    public static void coffinWakeUpTime(SleepFinishedTimeEvent event) {
        LevelAccessor world = event.getLevel();
        if (world.dayTime() % 24_000 < 12_000) { // before dusk
            event.setTimeAddition((world.dayTime() / 24_000) * 24_000 + 13_000); // nighttime
        }
    }

    @SubscribeEvent
    public static void stopBreathing(LivingBreatheEvent event) {
        event.setCanRefillAir(!event.getEntity().hasEffect(JStatusRegistry.HYPOXIA.get()));
    }
}
