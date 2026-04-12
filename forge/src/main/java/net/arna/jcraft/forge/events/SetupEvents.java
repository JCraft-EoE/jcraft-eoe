package net.arna.jcraft.forge.events;

import dev.architectury.platform.Platform;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.registry.JItemPropertiesRegistry;
import net.arna.jcraft.common.argumenttype.AttackArgumentType;
import net.arna.jcraft.common.argumenttype.SpecArgumentType;
import net.arna.jcraft.common.argumenttype.StandArgumentType;
import net.arna.jcraft.forge.capability.impl.entity.GrabCapability;
import net.arna.jcraft.forge.capability.impl.entity.TimeStopCapability;
import net.arna.jcraft.forge.capability.impl.living.*;
import net.arna.jcraft.forge.capability.impl.player.PhCapability;
import net.arna.jcraft.forge.capability.impl.living.SpecCapability;
import net.arna.jcraft.forge.capability.impl.world.ShockwaveHandlerCapability;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SetupEvents {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        //Player
        event.register(PhCapability.class);
        event.register(SpecCapability.class);

        //Entity
        event.register(GrabCapability.class);
        event.register(TimeStopCapability.class);

        //Living
        event.register(BombTrackerCapability.class);
        event.register(CooldownsCapability.class);
        event.register(HitPropertyCapability.class);
        event.register(MiscCapability.class);
        event.register(StandCapability.class);
        event.register(HamonCapability.class);
        event.register(VampireCapability.class);

        //World
        event.register(ShockwaveHandlerCapability.class);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void register(RegisterEvent event) {
        // Items are registered at this point (cuz priority is low, so this one runs later)
        if (event.getRegistryKey() == Registries.ITEM) {
            // Register properties for items if on client
            if (Platform.getEnv() == Dist.CLIENT) {
                JItemPropertiesRegistry.registerItemProperties();
            }
        }

        // Register argument type infos
        if (event.getRegistryKey() == Registries.COMMAND_ARGUMENT_TYPE) {
            SingletonArgumentInfo<StandArgumentType> standArgInfo = SingletonArgumentInfo.contextFree(StandArgumentType::stand);
            ArgumentTypeInfos.registerByClass(StandArgumentType.class, standArgInfo);
            event.register(Registries.COMMAND_ARGUMENT_TYPE, JCraft.id("stand"), () -> standArgInfo);

            SingletonArgumentInfo<SpecArgumentType> specArgInfo = SingletonArgumentInfo.contextFree(SpecArgumentType::spec);
            ArgumentTypeInfos.registerByClass(SpecArgumentType.class, specArgInfo);
            event.register(Registries.COMMAND_ARGUMENT_TYPE, JCraft.id("spec"), () -> specArgInfo);

            SingletonArgumentInfo<AttackArgumentType> attackArgInfo = SingletonArgumentInfo.contextFree(AttackArgumentType::attack);
            ArgumentTypeInfos.registerByClass(AttackArgumentType.class, attackArgInfo);
            event.register(Registries.COMMAND_ARGUMENT_TYPE, JCraft.id("attack"), () -> attackArgInfo);
        }
    }
}
