package net.arna.jcraft.client.registry;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.client.renderer.entity.*;
import net.arna.jcraft.client.renderer.entity.npc.*;
import net.arna.jcraft.client.renderer.entity.projectiles.*;
import net.arna.jcraft.client.renderer.entity.stands.*;
import net.arna.jcraft.client.renderer.entity.vehicles.RoadRollerRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public interface JEntityRendererRegister {
    record RendererData <T extends Entity> (RegistrySupplier<? extends EntityType<? extends T>> supplier, EntityRendererProvider<T> provider) {
        public void registerFabric() {
            EntityRendererRegistry.register(supplier, provider);
        }
    }

    RendererData<?>[] entries = {
            new RendererData<>(JEntityTypeRegistry.STAR_PLATINUM, StarPlatinumRenderer::new),
            new RendererData<>(JEntityTypeRegistry.SPTW, SPTWRenderer::new),
            new RendererData<>(JEntityTypeRegistry.KING_CRIMSON, KingCrimsonRenderer::new),

            new RendererData<>(JEntityTypeRegistry.D4C, D4CRenderer::new),

            new RendererData<>(JEntityTypeRegistry.CREAM, CreamRenderer::new),
            new RendererData<>(JEntityTypeRegistry.KILLER_QUEEN, context -> new StandEntityRenderer<>(context, JStandTypeRegistry.KILLER_QUEEN.get(), -0.1745329251f, -0.36f)),
            new RendererData<>(JEntityTypeRegistry.KILLER_QUEEN_BITES_THE_DUST, KQBTDRenderer::new),
            new RendererData<>(JEntityTypeRegistry.SHEER_HEART_ATTACK, SheerHeartAttackRenderer::new),

            new RendererData<>(JEntityTypeRegistry.WHITE_SNAKE, WhiteSnakeRenderer::new),
            new RendererData<>(JEntityTypeRegistry.C_MOON, CMoonRenderer::new),
            new RendererData<>(JEntityTypeRegistry.MADE_IN_HEAVEN, MadeInHeavenRenderer::new),

            new RendererData<>(JEntityTypeRegistry.SHADOW_THE_WORLD, ShadowTheWorldRenderer::of),
            new RendererData<>(JEntityTypeRegistry.THE_WORLD, context -> new StandEntityRenderer<>(context, JStandTypeRegistry.THE_WORLD.get(), -0.1745329251f, -0.1745329251f)),
            new RendererData<>(JEntityTypeRegistry.THE_WORLD_OVER_HEAVEN, TheWorldOverHeavenRenderer::new),

            new RendererData<>(JEntityTypeRegistry.SILVER_CHARIOT, SilverChariotRenderer::new),

            new RendererData<>(JEntityTypeRegistry.MAGICIANS_RED, MagiciansRedRenderer::new),

            new RendererData<>(JEntityTypeRegistry.THE_FOOL, context -> new StandEntityRenderer<>(context, JStandTypeRegistry.THE_FOOL.get(), 0.7854f, -0.349f, 30f)),

            new RendererData<>(JEntityTypeRegistry.GOLD_EXPERIENCE, GoldExperienceRenderer::new),
            new RendererData<>(JEntityTypeRegistry.GE_TREE, GETreeRenderer::new),
            new RendererData<>(JEntityTypeRegistry.GE_FROG, GEFrogRenderer::new),
            new RendererData<>(JEntityTypeRegistry.GE_SNAKE, GESnakeRenderer::new),
            new RendererData<>(JEntityTypeRegistry.GE_BUTTERFLY, GEButterflyRenderer::new),

            new RendererData<>(JEntityTypeRegistry.HIEROPHANT_GREEN, context -> new StandEntityRenderer<>(context, JStandTypeRegistry.HIEROPHANT_GREEN.get(), 0f, -0.2f)),
            new RendererData<>(JEntityTypeRegistry.EMERALD, context -> new ProjectileRenderer<>(context, "emerald")),
            new RendererData<>(JEntityTypeRegistry.BISECT, BisectRenderer::new),
            new RendererData<>(JEntityTypeRegistry.HG_NET, HGNetRenderer::new),

            new RendererData<>(JEntityTypeRegistry.THE_SUN, TheSunRenderer::new),

            new RendererData<>(JEntityTypeRegistry.GER, context -> new StandEntityRenderer<>(context, JStandTypeRegistry.GOLD_EXPERIENCE_REQUIEM.get())),
            new RendererData<>(JEntityTypeRegistry.GER_SCORPION, GERScorpionRenderer::new),

            new RendererData<>(JEntityTypeRegistry.PURPLE_HAZE_DISTORTION, context -> new StandEntityRenderer<>(context, JStandTypeRegistry.PURPLE_HAZE_DISTORTION.get())),
            new RendererData<>(JEntityTypeRegistry.PURPLE_HAZE, PurpleHazeRenderer::new),

            new RendererData<>(JEntityTypeRegistry.HORUS, HorusRenderer::new),
            new RendererData<>(JEntityTypeRegistry.ICICLE, IcicleRenderer::new),
            new RendererData<>(JEntityTypeRegistry.LARGE_ICICLE, LargeIcicleRenderer::new),
            new RendererData<>(JEntityTypeRegistry.ICE_BRANCH, IceBranchRenderer::new),

            new RendererData<>(JEntityTypeRegistry.CRAZY_DIAMOND, context -> new StandEntityRenderer<>(context, JStandTypeRegistry.CRAZY_DIAMOND.get())),
            new RendererData<>(JEntityTypeRegistry.AEROSMITH, AerosmithRenderer::of),

            new RendererData<>(JEntityTypeRegistry.CINDERELLA, context -> new StandEntityRenderer<>(context, JStandTypeRegistry.CINDERELLA.get())),
            new RendererData<>(JEntityTypeRegistry.OSIRIS, context -> new StandEntityRenderer<>(context, JStandTypeRegistry.OSIRIS.get())),
            new RendererData<>(JEntityTypeRegistry.ATUM, context -> new StandEntityRenderer<>(context, JStandTypeRegistry.ATUM.get())),
            new RendererData<>(JEntityTypeRegistry.DIVER_DOWN, context -> new StandEntityRenderer<>(context, JStandTypeRegistry.DIVER_DOWN.get())),
            new RendererData<>(JEntityTypeRegistry.CHARIOT_REQUIEM, ChariotRequiemRenderer::new),
            new RendererData<>(JEntityTypeRegistry.DRAGONS_DREAM, context -> new StandEntityRenderer<>(context, JStandTypeRegistry.DRAGONS_DREAM.get(), 0.0f, 1.5707f)),
            new RendererData<>(JEntityTypeRegistry.FOO_FIGHTERS, context -> new StandEntityRenderer<>(context, JStandTypeRegistry.FOO_FIGHTERS.get())),
            new RendererData<>(JEntityTypeRegistry.GOO_GOO_DOLLS, context -> new StandEntityRenderer<>(context, JStandTypeRegistry.GOO_GOO_DOLLS.get())),

            new RendererData<>(JEntityTypeRegistry.LASER_PROJECTILE, LaserProjectileRenderer::new),
            new RendererData<>(JEntityTypeRegistry.BLOOD_PROJECTILE, BloodProjectileRenderer::new),
            new RendererData<>(JEntityTypeRegistry.BLOCK_PROJECTILE, BlockProjectileRenderer::new),
            new RendererData<>(JEntityTypeRegistry.KNIFE, KnifeRenderer::new),
            new RendererData<>(JEntityTypeRegistry.SCALPEL, ScalpelRenderer::new),
            new RendererData<>(JEntityTypeRegistry.RAZOR, RazorRenderer::new),
            new RendererData<>(JEntityTypeRegistry.ANKH, AnkhRenderer::new),
            new RendererData<>(JEntityTypeRegistry.BUBBLE, BubbleRenderer::new),
            new RendererData<>(JEntityTypeRegistry.LIFE_DETECTOR, LifeDetectorRenderer::new),
            new RendererData<>(JEntityTypeRegistry.RED_BIND, RedBindRenderer::new),
            new RendererData<>(JEntityTypeRegistry.SAND_TORNADO, SandTornadoRenderer::new),
            new RendererData<>(JEntityTypeRegistry.WS_ACID_PROJECTILE, WSAcidRenderer::new),
            new RendererData<>(JEntityTypeRegistry.SUN_BEAM, SunBeamRenderer::new),
            new RendererData<>(JEntityTypeRegistry.BULLET, BulletRenderer::new),
            new RendererData<>(JEntityTypeRegistry.RAPIER, RapierRenderer::new),
            new RendererData<>(JEntityTypeRegistry.METEOR, MeteorRenderer::new),
            new RendererData<>(JEntityTypeRegistry.PH_CAPSULE, context -> new ProjectileRenderer<>(context, "ph_capsule")),
            new RendererData<>(JEntityTypeRegistry.PURPLE_HAZE_CLOUD, JEntityRendererRegister::createEmpty),
            new RendererData<>(JEntityTypeRegistry.STAND_ARROW_PROJECTILE, context -> new ProjectileRenderer<>(context, "stand_arrow")),
            new RendererData<>(JEntityTypeRegistry.AEROBOMB, context -> new ProjectileRenderer<>(context, "aerobomb")),
            new RendererData<>(JEntityTypeRegistry.HAMON_WAVE, JEntityRendererRegister::createEmpty),

            new RendererData<>(JEntityTypeRegistry.ROAD_ROLLER, RoadRollerRenderer::new),

            new RendererData<>(JEntityTypeRegistry.PETSHOP, PetshopRenderer::new),
            new RendererData<>(JEntityTypeRegistry.AYA_TSUJI, AyaTsujiRenderer::new),
            new RendererData<>(JEntityTypeRegistry.DARBY_OLDER, DarbyOlderRenderer::new),
            new RendererData<>(JEntityTypeRegistry.DARBY_YOUNGER, DarbyYoungerRenderer::new),
            new RendererData<>(JEntityTypeRegistry.TONPETTY, TonpettyRenderer::new),

            new RendererData<>(JEntityTypeRegistry.METALLICA, MetallicaRenderer::new),
            new RendererData<>(JEntityTypeRegistry.THE_HAND, TheHandRenderer::new),

            new RendererData<>(JEntityTypeRegistry.MANDOM, MandomRenderer::new),

            new RendererData<>(JEntityTypeRegistry.STAND_METEOR, StandMeteorRenderer::new),
            new RendererData<>(JEntityTypeRegistry.TRAINING_DUMMY, TrainingDummyRenderer::new),

            new RendererData<>(JEntityTypeRegistry.ITEM_TOSS_PROJECTILE, ItemTossProjectileRenderer::new),

            new RendererData<>(JEntityTypeRegistry.BRAWLER_SPEC_USER, context -> new SpecUserRenderer<>(context, JCraft.id("geo/humanoid.geo.json"), JCraft.id("textures/entity/jonathan.png"))),
            new RendererData<>(JEntityTypeRegistry.HAMON_SPEC_USER, context -> new SpecUserRenderer<>(context, JCraft.id("geo/hamon_monk.geo.json"), JCraft.id("textures/entity/hamon_spec_user.png"))),
            new RendererData<>(JEntityTypeRegistry.VAMPIRE_SPEC_USER, context -> new SpecUserRenderer<>(context, JCraft.id("geo/humanoid.geo.json"), JCraft.id("textures/entity/vampire_spec_user.png"))),
            new RendererData<>(JEntityTypeRegistry.ANUBIS_SPEC_USER, context -> new SpecUserRenderer<>(context, JCraft.id("geo/chaka.geo.json"), JCraft.id("textures/entity/chaka.png"))),
    };

    static void registerEntityRenderers(Consumer<RendererData<?>> consumer) {
        for (RendererData<?> entry : entries) consumer.accept(entry);
    }

    static <T extends Entity> EntityRenderer<T> createEmpty(EntityRendererProvider.Context ctx) {
        return new EntityRenderer<>(ctx) {
            @Override
            public ResourceLocation getTextureLocation(@NonNull T entity) {
                return null;
            }
        };
    }
}
