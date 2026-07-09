package net.arna.jcraft.forge;

import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.forge.EventBuses;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.events.EntityTickEvent;
import net.arna.jcraft.forge.capability.impl.entity.GrabCapability;
import net.arna.jcraft.forge.capability.impl.entity.GravityCapability;
import net.arna.jcraft.forge.capability.impl.living.*;
import net.arna.jcraft.forge.capability.impl.world.ShockwaveHandlerCapability;
import net.arna.jcraft.forge.events.ClientSetupEvents;
import net.arna.jcraft.forge.loot.JForgeLootModifiers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.Bindings;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static net.arna.jcraft.JCraft.MOD_ID;

@Mod(MOD_ID)
public final class JCraftForge {

    public JCraftForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = Bindings.getForgeBus().get();

        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(MOD_ID, modBus);

        JCraft.init();

        modBus.addListener(this::onInitializeCommon);
        modBus.addListener(ClientSetupEvents::onInitializeClient);

        JForgeLootModifiers.register(modBus);

        //DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> JCraftClient::init);
        JNetworkingForge.initServer();

        EntityTickEvent.ENTITY_PRE.register(JCraftForge::tickEntityCaps);
        TickEvent.ServerLevelTick.SERVER_LEVEL_POST.register(JCraftForge::tickWorldCaps);
    }

    @SubscribeEvent
    public void onInitializeCommon(final FMLCommonSetupEvent event) {
        JCraft.postInit();
    }

    public static void tickWorldCaps(Level world) {
        ShockwaveHandlerCapability.getCapability(world).tick();
    }

    public static void tickEntityCaps(Entity entity) {
        GrabCapability.getCapability(entity).tick();
        GravityCapability.getCapability(entity).tick();

        if (entity instanceof LivingEntity living) {
            GravityShiftCapability.getCapability(living).tick();
            BombTrackerCapability.getCapability(living).tick();
            CooldownsCapability.getCapability(living).tick();
            HitPropertyCapability.getCapability(living).tick();
            MiscCapability.getCapability(living).tick();

            HamonCapability.getCapability(living).tick();
            VampireCapability.getCapability(living).tick();
        }
    }
}
