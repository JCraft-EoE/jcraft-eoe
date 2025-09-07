package net.arna.jcraft.forge.client;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import me.shedaniel.autoconfig.AutoConfig;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.client.JClientConfig;
import net.arna.jcraft.client.JCraftClient;
import net.arna.jcraft.client.gui.hud.EpitaphOverlay;
import net.arna.jcraft.client.particle.*;
import net.arna.jcraft.client.renderer.block.CoffinTileRenderer;
import net.arna.jcraft.client.particle.DamageNumberParticle;
import net.arna.jcraft.client.rendering.DamageIndicatorManager;
import net.arna.jcraft.forge.JCraftForge;
import net.arna.jcraft.forge.capability.impl.entity.GrabCapability;
import net.arna.jcraft.forge.capability.impl.entity.GravityCapability;
import net.arna.jcraft.forge.capability.impl.entity.TimeStopCapability;
import net.arna.jcraft.forge.capability.impl.living.*;
import net.arna.jcraft.forge.capability.impl.player.PhCapability;
import net.arna.jcraft.forge.capability.impl.player.SpecCapability;
import net.arna.jcraft.forge.capability.impl.world.ShockwaveHandlerCapability;
import net.arna.jcraft.api.registry.JBlockEntityTypeRegistry;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static net.arna.jcraft.forge.capability.impl.entity.GrabCapability.GRAB_S2C;
import static net.arna.jcraft.forge.capability.impl.entity.GravityCapability.G_S2C;
import static net.arna.jcraft.forge.capability.impl.entity.TimeStopCapability.TIME_S2C;
import static net.arna.jcraft.forge.capability.impl.living.BombTrackerCapability.BOMB_S2C;
import static net.arna.jcraft.forge.capability.impl.living.CooldownsCapability.CD_S2C;
import static net.arna.jcraft.forge.capability.impl.living.GravityShiftCapability.GS_S2C;
import static net.arna.jcraft.forge.capability.impl.living.HitPropertyCapability.HIT_S2C;
import static net.arna.jcraft.forge.capability.impl.living.MiscCapability.MISC_S2C;
import static net.arna.jcraft.forge.capability.impl.living.StandCapability.STAND_S2C;
import static net.arna.jcraft.forge.capability.impl.living.VampireCapability.VAMP_S2C;
import static net.arna.jcraft.forge.capability.impl.player.PhCapability.PH_S2C;
import static net.arna.jcraft.forge.capability.impl.player.SpecCapability.SPEC_S2C;
import static net.arna.jcraft.forge.capability.impl.world.ShockwaveHandlerCapability.SHOCK_S2C;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = JCraft.MOD_ID)
public class JCraftForgeClient {

    @SubscribeEvent
    public static void handleClientSetup(final FMLClientSetupEvent event) {
        JCraftClient.init();
        ClientTickEvent.ClientLevel.CLIENT_LEVEL_POST.register(JCraftForge::tickWorldCaps);
        //JModelPredicateProviderRegistry.register();

        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(
                (minecraft, screen) -> AutoConfig.getConfigScreen(JClientConfig.class, screen).get()));

        BlockEntityRenderers.register(JBlockEntityTypeRegistry.COFFIN_TILE.get(), CoffinTileRenderer::new);

        registerClientCapabilityReceivers();

        // Run when the MinecraftClient instance is fully initialized.
        Minecraft.getInstance().tell(EpitaphOverlay::preload);
    }

    @SubscribeEvent
    public static void onParticleFactoryRegistration(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(JParticleTypeRegistry.COMBO_BREAK.get(), ComboBreakerParticle.Factory::new);
        event.registerSpriteSet(JParticleTypeRegistry.COOLDOWN_CANCEL.get(), CooldownCancelParticle.Factory::new);
        event.registerSpriteSet(JParticleTypeRegistry.HITSPARK_1.get(), provider -> new HitsparkParticle.Factory(provider, 0.4f, 5));
        event.registerSpriteSet(JParticleTypeRegistry.HITSPARK_2.get(), provider -> new HitsparkParticle.Factory(provider, 0.66f, 6));
        event.registerSpriteSet(JParticleTypeRegistry.HITSPARK_3.get(), provider -> new HitsparkParticle.Factory(provider, 1f, 8));
        event.registerSpriteSet(JParticleTypeRegistry.INVERTED_HITSPARK_3.get(), provider -> new InvertedHitsparkParticle.Factory(provider, 1f, 8));
        event.registerSpriteSet(JParticleTypeRegistry.STUN_SLASH.get(), provider -> new HitsparkParticle.Factory(provider, 0.6f, 6));
        event.registerSpriteSet(JParticleTypeRegistry.STUN_PIERCE.get(), provider -> new HitsparkParticle.Factory(provider, 0.6f, 6));
        event.registerSpriteSet(JParticleTypeRegistry.KCPARTICLE.get(), KCParticle.Factory::new);
        event.registerSpriteSet(JParticleTypeRegistry.BACKSTAB.get(), BackstabParticle.Factory::new);
        event.registerSpriteSet(JParticleTypeRegistry.SPEED_PARTICLE.get(), SpeedParticle.Factory::new);
        event.registerSpriteSet(JParticleTypeRegistry.BITES_THE_DUST.get(), BitesTheDustParticle.Factory::new);
        event.registerSpriteSet(JParticleTypeRegistry.BOOM_1.get(), BoomParticle.Factory::new);
        event.registerSpriteSet(JParticleTypeRegistry.PIXEL.get(), PixelParticle.Factory::new);
        event.registerSpriteSet(JParticleTypeRegistry.BLOCKSPARK.get(), provider -> new BlocksparkParticle.Factory(provider, 0.15f));
        event.registerSpriteSet(JParticleTypeRegistry.GO.get(), GoParticle.Factory::new);
        event.registerSpriteSet(JParticleTypeRegistry.AURA_ARC.get(), AuraArcParticle.Factory::new);
        event.registerSpriteSet(JParticleTypeRegistry.AURA_BLOB.get(), AuraBlobParticle.Factory::new);
        event.registerSpriteSet(JParticleTypeRegistry.INVERSION.get(), InversionParticle.Factory::new);
        event.registerSpriteSet(JParticleTypeRegistry.SUN_LOCK_ON.get(), BackstabParticle.Factory::new); // 9 frames, reusing
        event.registerSpriteSet(JParticleTypeRegistry.PURPLE_HAZE_CLOUD.get(), PurpleHazeCloudParticle.Factory::new);
        event.registerSpriteSet(JParticleTypeRegistry.PURPLE_HAZE_PARTICLE.get(), PurpleHazeErraticParticle.Factory::new);
        event.registerSpriteSet(JParticleTypeRegistry.DAMAGE_NUMBER.get(), DamageNumberParticle.Factory::new);
        DamageIndicatorManager.setDamageNumberParticle(JParticleTypeRegistry.DAMAGE_NUMBER.get());
    }

    private static void registerClientCapabilityReceivers() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, GRAB_S2C, (buf, context) -> {
            int id = buf.readVarInt();
            if (Minecraft.getInstance().level != null) {
                Entity entity = Minecraft.getInstance().level.getEntity(id);
                if (entity == null) return;
                GrabCapability.getCapabilityOptional(entity).ifPresent(c -> c.applySyncPacket(buf));
            }
        });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, G_S2C, (buf, context) -> {
            int id = buf.readInt();
            CompoundTag nbt = buf.readNbt();

            if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(id) instanceof Player player) {
                GravityCapability.getCapabilityOptional(player).ifPresent(c -> c.deserializeNBT(nbt));
            }
        });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, TIME_S2C, (buf, context) -> {
            int id = buf.readInt();
            CompoundTag nbt = buf.readNbt();

            if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(id) instanceof Player player) {
                TimeStopCapability.getCapabilityOptional(player).ifPresent(c -> c.deserializeNBT(nbt));
            }

        });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, BOMB_S2C, (buf, context) -> {
            // Received by the cap holder and only them
            LocalPlayer localPlayer = Minecraft.getInstance().player;
            if (localPlayer != null) {
                BombTrackerCapability.getCapabilityOptional(localPlayer).ifPresent(c -> c.applySyncPacket(buf));
            }
        });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CD_S2C, (buf, context) -> {
            int id = buf.readInt();
            CompoundTag nbt = buf.readNbt();

            if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(id) instanceof Player player) {
                CooldownsCapability.getCapabilityOptional(player).ifPresent(c -> c.deserializeNBT(nbt));
            }

        });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, GS_S2C, (buf, context) -> {
            int id = buf.readInt();
            CompoundTag nbt = buf.readNbt();

            if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(id) instanceof LivingEntity living) {
                GravityShiftCapability.getCapability(living).deserializeNBT(nbt);
            }
        });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, HIT_S2C, (buf, context) -> {
            int id = buf.readVarInt();

            if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(id) instanceof LivingEntity livingEntity) {
                HitPropertyCapability.getCapabilityOptional(livingEntity).ifPresent(
                        capability -> capability.applySyncPacket(buf)
                );
            }
        });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, MISC_S2C, (buf, context) -> {
            int id = buf.readInt();
            CompoundTag nbt = buf.readNbt();

            if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(id) instanceof Player player) {
                MiscCapability.getCapabilityOptional(player).ifPresent(c -> c.deserializeNBT(nbt));
            }

        });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, STAND_S2C, (buf, context) -> {
            int id = buf.readInt();
            boolean noStand = buf.readBoolean();
            ResourceLocation standTypeId = noStand ? null : buf.readResourceLocation();
            StandType standType = standTypeId == null ? null : JRegistries.STAND_TYPE_REGISTRY.get(standTypeId);
            int skin = buf.readInt();

            if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(id) instanceof LivingEntity livingEntity) {
                StandCapability.getCapabilityOptional(livingEntity).ifPresent(c -> {
                    c.setTypeAndSkin(standType, skin);
                    c.applySyncPacket(buf);
                });
            }
        });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, VAMP_S2C, (buf, context) -> {
            // Received by the VampireCapability holder and only them
            LocalPlayer localPlayer = Minecraft.getInstance().player;
            if (localPlayer != null) {
                VampireCapability.getCapabilityOptional(localPlayer).ifPresent(c -> c.applySyncPacket(buf));
            }
        });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, PH_S2C, (buf, context) -> {

            int id = buf.readInt();
            CompoundTag nbt = buf.readNbt();

            if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(id) instanceof Player player) {
                PhCapability.getCapabilityOptional(player).ifPresent(c -> c.deserializeNBT(nbt));
            }
        });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SPEC_S2C, (buf, context) -> {

            int id = buf.readInt();
            CompoundTag nbt = buf.readNbt();

            if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getEntity(id) instanceof Player player) {
                SpecCapability.getCapabilityOptional(player).ifPresent(c -> c.deserializeNBT(nbt));
            }
        });

        NetworkManager.registerReceiver(NetworkManager.Side.S2C, SHOCK_S2C, (buf, context) -> {
            ClientLevel clientWorld = Minecraft.getInstance().level;
            if (clientWorld == null) return;
            ShockwaveHandlerCapability.getCapabilityOptional(clientWorld).ifPresent(shockwaveCap -> shockwaveCap.applySyncPacket(buf));
        });
    }
}
