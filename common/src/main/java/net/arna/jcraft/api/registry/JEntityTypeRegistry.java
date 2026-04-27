package net.arna.jcraft.api.registry;

import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.spec.SpecUserMob;
import net.arna.jcraft.common.entity.*;
import net.arna.jcraft.common.entity.npc.AyaTsujiEntity;
import net.arna.jcraft.common.entity.npc.DarbyOlderEntity;
import net.arna.jcraft.common.entity.npc.DarbyYoungerEntity;
import net.arna.jcraft.common.entity.npc.PetshopEntity;
import net.arna.jcraft.common.entity.npc.TonpettyEntity;
import net.arna.jcraft.common.entity.projectile.*;
import net.arna.jcraft.common.entity.spec.AnubisSpecUser;
import net.arna.jcraft.common.entity.spec.BrawlerSpecUser;
import net.arna.jcraft.common.entity.spec.HamonSpecUser;
import net.arna.jcraft.common.entity.spec.VampireSpecUser;
import net.arna.jcraft.common.entity.stand.*;
import net.arna.jcraft.common.entity.vehicle.RoadRollerEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;


public interface JEntityTypeRegistry {

    DeferredRegister<EntityType<?>> ENTITY_TYPE_REGISTRY = DeferredRegister.create(JCraft.MOD_ID, Registries.ENTITY_TYPE);

    RegistrySupplier<EntityType<StarPlatinumEntity>> STAR_PLATINUM = ENTITY_TYPE_REGISTRY.register(JCraft.id("starplatinum"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(StarPlatinumEntity::new),
                    MobCategory.CREATURE
            ).sized(
                    0.6f,
                    1.8f
            ).build("starplatinum")
    );

    RegistrySupplier<EntityType<SPTWEntity>> SPTW = ENTITY_TYPE_REGISTRY.register(JCraft.id("sptw"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(SPTWEntity::new),
                    MobCategory.CREATURE
            ).sized(
                    0.6f,
                    1.8f
            ).build("sptw")
    );

    RegistrySupplier<EntityType<KingCrimsonEntity>> KING_CRIMSON = ENTITY_TYPE_REGISTRY.register(JCraft.id("kingcrimson"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(KingCrimsonEntity::new),
                    MobCategory.CREATURE
            ).sized(
                    0.6f,
                    1.8f
            ).build("kingcrimson")
    );

    RegistrySupplier<EntityType<ShadowTheWorldEntity>> SHADOW_THE_WORLD = ENTITY_TYPE_REGISTRY.register(JCraft.id("shadow_the_world"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(ShadowTheWorldEntity::new),
                    MobCategory.CREATURE
            ).sized(
                    0.6f,
                    1.8f
            ).build("shadow_the_world")
    );

    RegistrySupplier<EntityType<TheWorldEntity>> THE_WORLD = ENTITY_TYPE_REGISTRY.register(JCraft.id("theworld"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(TheWorldEntity::new),
                    MobCategory.CREATURE
            ).sized(
                    0.6f,
                    1.8f
            ).build("theworld")
    );

    RegistrySupplier<EntityType<D4CEntity>> D4C = ENTITY_TYPE_REGISTRY.register(JCraft.id("d4c"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(D4CEntity::new),
                    MobCategory.CREATURE
            ).sized(
                    0.6f,
                    1.8f
            ).build("d4c")
    );

    RegistrySupplier<EntityType<CreamEntity>> CREAM = ENTITY_TYPE_REGISTRY.register(JCraft.id("cream"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(CreamEntity::new),
                    MobCategory.CREATURE
            ).sized(
                    0.6f,
                    1.8f
            ).build("cream")
    );

    RegistrySupplier<EntityType<KillerQueenEntity>> KILLER_QUEEN = ENTITY_TYPE_REGISTRY.register(JCraft.id("killerqueen"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(KillerQueenEntity::new),
                    MobCategory.CREATURE
            ).sized(
                    0.6f,
                    1.8f
            ).build("killerqueen")
    );

    RegistrySupplier<EntityType<KQBTDEntity>> KILLER_QUEEN_BITES_THE_DUST = ENTITY_TYPE_REGISTRY.register(JCraft.id("kqbtd"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(KQBTDEntity::new),
                    MobCategory.CREATURE
            ).sized(
                    0.6f,
                    1.8f
            ).build("kqbtd")
    );

    RegistrySupplier<EntityType<SheerHeartAttackEntity>> SHEER_HEART_ATTACK = ENTITY_TYPE_REGISTRY.register(JCraft.id("sha"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(SheerHeartAttackEntity::new),
                    MobCategory.CREATURE
            ).sized(
                    0.5f,
                    0.5f
            ).build("sha")
    );

    RegistrySupplier<EntityType<StandMeteorEntity>> STAND_METEOR = ENTITY_TYPE_REGISTRY.register(JCraft.id("stand_meteor"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(StandMeteorEntity::new),
                    MobCategory.MISC
            ).sized(
                    4.0f,
                    4.0f
            ).build("sha")
    );

    RegistrySupplier<EntityType<WhiteSnakeEntity>> WHITE_SNAKE = ENTITY_TYPE_REGISTRY.register(JCraft.id("whitesnake"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(WhiteSnakeEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("whitesnake")
    );

    RegistrySupplier<EntityType<CMoonEntity>> C_MOON = ENTITY_TYPE_REGISTRY.register(JCraft.id("cmoon"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(CMoonEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("cmoon")
    );

    RegistrySupplier<EntityType<MadeInHeavenEntity>> MADE_IN_HEAVEN = ENTITY_TYPE_REGISTRY.register(JCraft.id("mih"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(MadeInHeavenEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 2.1f).build("mih")
    );

    RegistrySupplier<EntityType<TheWorldOverHeavenEntity>> THE_WORLD_OVER_HEAVEN = ENTITY_TYPE_REGISTRY.register(JCraft.id("twoh"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(TheWorldOverHeavenEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("twoh")
    );

    RegistrySupplier<EntityType<SilverChariotEntity>> SILVER_CHARIOT = ENTITY_TYPE_REGISTRY.register(JCraft.id("silverchariot"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(SilverChariotEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("silverchariot")
    );

    RegistrySupplier<EntityType<MagiciansRedEntity>> MAGICIANS_RED = ENTITY_TYPE_REGISTRY.register(JCraft.id("mr"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(MagiciansRedEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("mr")
    );

    RegistrySupplier<EntityType<TheFoolEntity>> THE_FOOL = ENTITY_TYPE_REGISTRY.register(JCraft.id("thefool"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(TheFoolEntity::new),
                    MobCategory.CREATURE
            ).sized(2f, 2f).fireImmune().build("thefool")
    );

    RegistrySupplier<EntityType<GoldExperienceEntity>> GOLD_EXPERIENCE = ENTITY_TYPE_REGISTRY.register(JCraft.id("goldexperience"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(GoldExperienceEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("goldexperience")
    );

    RegistrySupplier<EntityType<GETreeEntity>> GE_TREE = ENTITY_TYPE_REGISTRY.register(JCraft.id("getree"),
            () -> EntityType.Builder.of(
                    ((EntityType<GETreeEntity> type, Level world) -> new GETreeEntity(world)),
                    MobCategory.MISC
            ).sized(0.6f, 0.8f).build("goldexperience")
    );

    RegistrySupplier<EntityType<GESnakeEntity>> GE_SNAKE = ENTITY_TYPE_REGISTRY.register(JCraft.id("gesnake"),
            () -> EntityType.Builder.of(
                    (GESnakeEntity::new),
                    MobCategory.CREATURE
            ).sized(1f, 0.3f).build("gesnake")
    );

    RegistrySupplier<EntityType<GEFrogEntity>> GE_FROG = ENTITY_TYPE_REGISTRY.register(JCraft.id("gefrog"),
            () -> EntityType.Builder.of(
                    (GEFrogEntity::new),
                    MobCategory.CREATURE
            ).sized(0.3f, 0.3f).build("gefrog")
    );

    RegistrySupplier<EntityType<GEButterflyEntity>> GE_BUTTERFLY = ENTITY_TYPE_REGISTRY.register(JCraft.id("gebutterfly"),
            () -> EntityType.Builder.of(
                    (GEButterflyEntity::new),
                    MobCategory.CREATURE
            ).sized(0.3f, 0.3f).build("gebutterfly")
    );


    RegistrySupplier<EntityType<HGEntity>> HIEROPHANT_GREEN = ENTITY_TYPE_REGISTRY.register(JCraft.id("hierophant_green"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(HGEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("hierophant_green")
    );

    RegistrySupplier<EntityType<TheSunEntity>> THE_SUN = ENTITY_TYPE_REGISTRY.register(JCraft.id("the_sun"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(TheSunEntity::new),
                            MobCategory.CREATURE
                    ).sized(2f, 2f)
                    .noSave()
                    .build("the_sun")
    );

    RegistrySupplier<EntityType<PurpleHazeEntity>> PURPLE_HAZE = ENTITY_TYPE_REGISTRY.register(JCraft.id("purple_haze"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(PurpleHazeEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("purple_haze")
    );

    RegistrySupplier<EntityType<PurpleHazeDistortionEntity>> PURPLE_HAZE_DISTORTION = ENTITY_TYPE_REGISTRY.register(JCraft.id("purple_haze_distortion"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(PurpleHazeDistortionEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("purple_haze_distortion")
    );

    RegistrySupplier<EntityType<HorusEntity>> HORUS = ENTITY_TYPE_REGISTRY.register(JCraft.id("horus"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(HorusEntity::new),
                    MobCategory.CREATURE
            ).sized(0.8f, 2.2f).build("horus")
    );

    RegistrySupplier<EntityType<CrazyDiamondEntity>> CRAZY_DIAMOND = ENTITY_TYPE_REGISTRY.register(JCraft.id("crazy_diamond"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(CrazyDiamondEntity::new),
                    MobCategory.CREATURE
            ).sized(
                    0.6f,
                    1.8f
            ).build("crazy_diamond"));

    RegistrySupplier<EntityType<AerosmithEntity>> AEROSMITH = ENTITY_TYPE_REGISTRY.register(JCraft.id("aerosmith"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(AerosmithEntity::new),
                    MobCategory.CREATURE
            ).sized(
                    1.2f,
                    0.9f
            ).build("aerosmith"));

    RegistrySupplier<EntityType<GEREntity>> GER = ENTITY_TYPE_REGISTRY.register(JCraft.id("ger"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(GEREntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("ger")
    );

    RegistrySupplier<EntityType<GERScorpionEntity>> GER_SCORPION = ENTITY_TYPE_REGISTRY.register(JCraft.id("gerscorpion"),
            () -> EntityType.Builder.of(
                            GERScorpionEntity::new, MobCategory.CREATURE)
                    .sized(0.4f, 0.4f)
                    .build("gerscorpion")
    );

    RegistrySupplier<EntityType<PlayerCloneEntity>> PLAYER_CLONE = ENTITY_TYPE_REGISTRY.register(JCraft.id("playerclone"),
            () -> EntityType.Builder.of(
                            (EntityType<PlayerCloneEntity> entityType, Level world) -> new PlayerCloneEntity(world), MobCategory.CREATURE)
                    .sized(0.6f, 1.8f)
                    .build("playerclone")
    );

    RegistrySupplier<EntityType<RoadRollerEntity>> ROAD_ROLLER = ENTITY_TYPE_REGISTRY.register(JCraft.id("roadroller"),
            () -> EntityType.Builder.of(
                            (EntityType<RoadRollerEntity> entityType, Level world) -> new RoadRollerEntity(world), MobCategory.MISC)
                    .sized(3.5f, 2.0f)
                    .build("roadroller")
    );

    // Take note of the extra <KnifeProjectile> and tracked values
    RegistrySupplier<EntityType<KnifeProjectile>> KNIFE = ENTITY_TYPE_REGISTRY.register(JCraft.id("knife"),
            () -> EntityType.Builder.of(
                            (EntityType<KnifeProjectile> entityType, Level world) -> new KnifeProjectile(world), MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(6)
                    .updateInterval(20)
                    .build("knife")
    );

    RegistrySupplier<EntityType<ScalpelProjectile>> SCALPEL = ENTITY_TYPE_REGISTRY.register(JCraft.id("scalpel"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(ScalpelProjectile::new),
                            MobCategory.MISC
                    ).sized(0.5f, 0.5f)
                    .clientTrackingRange(6)
                    .updateInterval(20)
                    .build("scalpel")
    );

    RegistrySupplier<EntityType<RazorProjectile>> RAZOR = ENTITY_TYPE_REGISTRY.register(JCraft.id("razor"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(RazorProjectile::new),
                            MobCategory.MISC
                    ).sized(0.25f, 0.25f)
                    .clientTrackingRange(6)
                    .updateInterval(10)
                    .build("razor")
    );

    RegistrySupplier<EntityType<BisectProjectile>> BISECT = ENTITY_TYPE_REGISTRY.register(JCraft.id("bisect"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(BisectProjectile::new),
                            MobCategory.MISC
                    ).sized(1.0f, 1.0f)
                    .clientTrackingRange(6)
                    .updateInterval(20)
                    .build("bisect")
    );

    RegistrySupplier<EntityType<EmeraldProjectile>> EMERALD = ENTITY_TYPE_REGISTRY.register(JCraft.id("emerald"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(EmeraldProjectile::new),
                            MobCategory.MISC
                    ).sized(0.5f, 0.5f)
                    .clientTrackingRange(6)
                    .updateInterval(3)
                    .build("emerald")
    );

    RegistrySupplier<EntityType<IcicleProjectile>> ICICLE = ENTITY_TYPE_REGISTRY.register(JCraft.id("icicle"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(IcicleProjectile::new),
                            MobCategory.MISC
                    ).sized(0.5f, 0.5f)
                    .clientTrackingRange(6)
                    .updateInterval(3)
                    .build("icicle")
    );

    RegistrySupplier<EntityType<LargeIcicleProjectile>> LARGE_ICICLE = ENTITY_TYPE_REGISTRY.register(JCraft.id("large_icicle"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(LargeIcicleProjectile::new),
                            MobCategory.MISC
                    ).sized(1.0f, 1.0f)
                    .clientTrackingRange(6)
                    .updateInterval(20)
                    .build("large_icicle")
    );

    RegistrySupplier<EntityType<IceBranchProjectile>> ICE_BRANCH = ENTITY_TYPE_REGISTRY.register(JCraft.id("ice_branch"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(IceBranchProjectile::new),
                            MobCategory.MISC
                    ).sized((float) IceBranchProjectile.LENGTH, (float) IceBranchProjectile.LENGTH)
                    .clientTrackingRange(6)
                    .updateInterval(20)
                    .build("ice_branch")
    );

    RegistrySupplier<EntityType<BulletProjectile>> BULLET = ENTITY_TYPE_REGISTRY.register(JCraft.id("bullet"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(BulletProjectile::new),
                            MobCategory.MISC
                    ).sized(0.1f, 0.1f)
                    .clientTrackingRange(6)
                    .updateInterval(10)
                    .build("bullet")
    );

    RegistrySupplier<EntityType<RapierProjectile>> RAPIER = ENTITY_TYPE_REGISTRY.register(JCraft.id("rapier"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(RapierProjectile::new),
                            MobCategory.MISC
                    ).sized(0.5f, 0.5f)
                    .clientTrackingRange(6)
                    .updateInterval(3)
                    .build("rapier")
    );

    RegistrySupplier<EntityType<AnkhProjectile>> ANKH = ENTITY_TYPE_REGISTRY.register(JCraft.id("ankh"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(AnkhProjectile::new),
                            MobCategory.MISC
                    ).sized(0.75f, 0.75f)
                    .clientTrackingRange(6)
                    .updateInterval(20)
                    .build("ankh")
    );

    RegistrySupplier<EntityType<MeteorProjectile>> METEOR = ENTITY_TYPE_REGISTRY.register(JCraft.id("meteor"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(MeteorProjectile::new),
                            MobCategory.MISC
                    ).sized(1.0f, 1.0f)
                    .clientTrackingRange(6)
                    .updateInterval(20)
                    .build("meteor")
    );

    RegistrySupplier<EntityType<BubbleProjectile>> BUBBLE = ENTITY_TYPE_REGISTRY.register(JCraft.id("bubble"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(BubbleProjectile::new),
                            MobCategory.MISC
                    ).sized(0.5f, 0.5f)
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .build("bubble")
    );

    RegistrySupplier<EntityType<BloodProjectile>> BLOOD_PROJECTILE = ENTITY_TYPE_REGISTRY.register(JCraft.id("bloodprojectile"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(BloodProjectile::new),
                            MobCategory.MISC
                    ).sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("bloodprojectile")
    );

    RegistrySupplier<EntityType<LaserProjectile>> LASER_PROJECTILE = ENTITY_TYPE_REGISTRY.register(JCraft.id("laserprojectile"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(LaserProjectile::new),
                            MobCategory.MISC
                    ).sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("laserprojectile")
    );

    RegistrySupplier<EntityType<PHCapsuleProjectile>> PH_CAPSULE = ENTITY_TYPE_REGISTRY.register(JCraft.id("ph_capsule"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(PHCapsuleProjectile::new),
                            MobCategory.MISC
                    ).sized(0.75f, 0.75f)
                    .clientTrackingRange(6)
                    .updateInterval(20)
                    .build("ph_capsule")
    );

    RegistrySupplier<EntityType<LifeDetectorEntity>> LIFE_DETECTOR = ENTITY_TYPE_REGISTRY.register(JCraft.id("lifedetector"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(LifeDetectorEntity::new),
                            MobCategory.MISC
                    ).sized(1f, 1f)
                    .build("lifedetector")
    );

    RegistrySupplier<EntityType<HGNetEntity>> HG_NET = ENTITY_TYPE_REGISTRY.register(JCraft.id("hg_net"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(HGNetEntity::new),
                            MobCategory.MISC
                    ).sized(2f, 4f)
                    .build("hg_net")
    );

    RegistrySupplier<EntityType<RedBindEntity>> RED_BIND = ENTITY_TYPE_REGISTRY.register(JCraft.id("redbind"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(RedBindEntity::new),
                            MobCategory.MISC
                    ).sized(1f, 2f)
                    .build("redbind")
    );

    RegistrySupplier<EntityType<BlockProjectile>> BLOCK_PROJECTILE = ENTITY_TYPE_REGISTRY.register(JCraft.id("blockprojectile"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(BlockProjectile::new),
                            MobCategory.MISC
                    ).sized(0.5f, 0.5f)
                    .build("blockprojectile")
    );

    RegistrySupplier<EntityType<SandTornadoEntity>> SAND_TORNADO = ENTITY_TYPE_REGISTRY.register(JCraft.id("sandtornado"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(SandTornadoEntity::new),
                            MobCategory.MISC
                    ).sized(1f, 2f)
                    .build("sandtornado")
    );

    RegistrySupplier<EntityType<WSAcidProjectile>> WS_ACID_PROJECTILE = ENTITY_TYPE_REGISTRY.register(JCraft.id("wsacidprojectile"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(WSAcidProjectile::new),
                            MobCategory.MISC
                    ).sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("wsacidprojectile")
    );

    RegistrySupplier<EntityType<StandArrowEntity>> STAND_ARROW_PROJECTILE = ENTITY_TYPE_REGISTRY.register(JCraft.id("standarrowprojectile"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(StandArrowEntity::new),
                            MobCategory.MISC
                    ).sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("standarrowprojectile")
    );

    RegistrySupplier<EntityType<ItemTossProjectile>> ITEM_TOSS_PROJECTILE = ENTITY_TYPE_REGISTRY.register(JCraft.id("item_toss_projectile"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(ItemTossProjectile::new),
                            MobCategory.MISC
                    ).sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("item_toss_projectile")
    );

    RegistrySupplier<EntityType<SunBeamProjectile>> SUN_BEAM = ENTITY_TYPE_REGISTRY.register(JCraft.id("sunbeam"),
            () -> EntityType.Builder.of(
                            (EntityType<SunBeamProjectile> entityType, Level world) -> new SunBeamProjectile(world, null, null),
                            MobCategory.MISC
                    ).sized(1f, 2f)
                    .build("sunbeam")
    );

    RegistrySupplier<EntityType<PurpleHazeCloudEntity>> PURPLE_HAZE_CLOUD = ENTITY_TYPE_REGISTRY.register(JCraft.id("purple_haze_cloud"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(PurpleHazeCloudEntity::new),
                            MobCategory.MISC
                    ).sized(1f, 1f)
                    .build("purple_haze_cloud")
    );

    RegistrySupplier<EntityType<AerobombProjectile>> AEROBOMB = ENTITY_TYPE_REGISTRY.register(JCraft.id("aerobomb"),
            () -> EntityType.Builder.of(
                            WorldOnlyEntityFactory.from(AerobombProjectile::new),
                            MobCategory.MISC
                    ).sized(4f/16, 2f/16)
                    .build("aerobomb")
    );

    RegistrySupplier<EntityType<HamonWaveEntity>> HAMON_WAVE = ENTITY_TYPE_REGISTRY.register(JCraft.id("hamon_wave"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(HamonWaveEntity::new),
                    MobCategory.MISC
                    ).sized(2.0f, 0.2f)
                    .fireImmune()
                    .build("hamon_wave")
    );

    RegistrySupplier<EntityType<PetshopEntity>> PETSHOP = ENTITY_TYPE_REGISTRY.register(JCraft.id("petshop"),
            () -> EntityType.Builder.of(
                            (EntityType<PetshopEntity> entityType, Level world) -> new PetshopEntity(world),
                            MobCategory.CREATURE
                    ).sized(0.4f, 0.75f)
                    .build("petshop")
    );

    RegistrySupplier<EntityType<AyaTsujiEntity>> AYA_TSUJI = ENTITY_TYPE_REGISTRY.register(JCraft.id("aya_tsuji"),
            () -> EntityType.Builder.of(
                            (EntityType<AyaTsujiEntity> entityType, Level world) -> new AyaTsujiEntity(world),
                            MobCategory.CREATURE
                    ).sized(0.6f, 2f)
                    .build("aya_tsuji")
    );

    RegistrySupplier<EntityType<CinderellaEntity>> CINDERELLA = ENTITY_TYPE_REGISTRY.register(JCraft.id("cinderella"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(CinderellaEntity::new),
                    MobCategory.CREATURE
            ).sized(0.65f, 1.7f).build("cinderella")
    );

    RegistrySupplier<EntityType<DarbyOlderEntity>> DARBY_OLDER = ENTITY_TYPE_REGISTRY.register(JCraft.id("darby_older"),
            () -> EntityType.Builder.of(
                            (EntityType<DarbyOlderEntity> entityType, Level world) -> new DarbyOlderEntity(world),
                            MobCategory.CREATURE
                    ).sized(1f, 2f)
                    .build("darby_older")
    );

    RegistrySupplier<EntityType<OsirisEntity>> OSIRIS = ENTITY_TYPE_REGISTRY.register(JCraft.id("osiris"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(OsirisEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("osiris")
    );

    RegistrySupplier<EntityType<DarbyYoungerEntity>> DARBY_YOUNGER = ENTITY_TYPE_REGISTRY.register(JCraft.id("darby_younger"),
            () -> EntityType.Builder.of(
                            (EntityType<DarbyYoungerEntity> entityType, Level world) -> new DarbyYoungerEntity(world),
                            MobCategory.CREATURE
                    ).sized(1f, 2f)
                    .build("darby_younger")
    );

    RegistrySupplier<EntityType<AtumEntity>> ATUM = ENTITY_TYPE_REGISTRY.register(JCraft.id("atum"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(AtumEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("atum")
    );

    RegistrySupplier<EntityType<TonpettyEntity>> TONPETTY = ENTITY_TYPE_REGISTRY.register(JCraft.id("tonpetty"),
            () -> EntityType.Builder.of(
                            (EntityType<TonpettyEntity> entityType, Level world) -> new TonpettyEntity(world),
                            MobCategory.CREATURE
                    ).sized(0.6f, 2f)
                    .build("tonpetty")
    );

    RegistrySupplier<EntityType<ChariotRequiemEntity>> CHARIOT_REQUIEM = ENTITY_TYPE_REGISTRY.register(JCraft.id("chariot_requiem"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(ChariotRequiemEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("chariot_requiem")
    );

    RegistrySupplier<EntityType<DiverDownEntity>> DIVER_DOWN = ENTITY_TYPE_REGISTRY.register(JCraft.id("diver_down"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(DiverDownEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("diver_down")
    );

    RegistrySupplier<EntityType<DragonsDreamEntity>> DRAGONS_DREAM = ENTITY_TYPE_REGISTRY.register(JCraft.id("dragons_dream"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(DragonsDreamEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("dragons_dream")
    );

    RegistrySupplier<EntityType<FooFightersEntity>> FOO_FIGHTERS = ENTITY_TYPE_REGISTRY.register(JCraft.id("foo_fighters"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(FooFightersEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("foo_fighters")
    );

    RegistrySupplier<EntityType<GooGooDollsEntity>> GOO_GOO_DOLLS = ENTITY_TYPE_REGISTRY.register(JCraft.id("goo_goo_dolls"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(GooGooDollsEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("goo_goo_dolls")
    );

    RegistrySupplier<EntityType<MetallicaEntity>> METALLICA = ENTITY_TYPE_REGISTRY.register(JCraft.id("metallica"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(MetallicaEntity::new),
                    MobCategory.CREATURE
            ).sized(
                    0.6f,
                    1.8f
            ).build("metallica")
    );

    RegistrySupplier<EntityType<TheHandEntity>> THE_HAND = ENTITY_TYPE_REGISTRY.register(JCraft.id("the_hand"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(TheHandEntity::new),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("the_hand")
    );
    RegistrySupplier<EntityType<MandomEntity>> MANDOM = ENTITY_TYPE_REGISTRY.register(JCraft.id("mandom"),
            () -> EntityType.Builder.of(
                    WorldOnlyEntityFactory.from(MandomEntity::new),
                    MobCategory.CREATURE
            ).sized(0.3f, 0.9f).build("mandom")
    );
    RegistrySupplier<EntityType<TrainingDummyEntity>> TRAINING_DUMMY = ENTITY_TYPE_REGISTRY.register(JCraft.id("training_dummy"),
            () -> EntityType.Builder.of(
                    (EntityType<TrainingDummyEntity> entityType, Level world) -> new TrainingDummyEntity(entityType, world),
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("training_dummy")
    );


    @NotNull
    private static <T extends SpecUserMob> EntityType<T> createSpecUser(Function<Level, T> ctor, String id) {
        // this can be generalized for a lot of these registrations.
        return EntityType.Builder.of(
                WorldOnlyEntityFactory.from(ctor),
                MobCategory.CREATURE
        ).sized(0.6f, 1.8f).build(id);
    }

    RegistrySupplier<EntityType<BrawlerSpecUser>> BRAWLER_SPEC_USER = ENTITY_TYPE_REGISTRY.register(
            JCraft.id("brawler_spec_user"),
            () -> createSpecUser(BrawlerSpecUser::new, "brawler_spec_user")
    );

    RegistrySupplier<EntityType<HamonSpecUser>> HAMON_SPEC_USER = ENTITY_TYPE_REGISTRY.register(
            JCraft.id("hamon_spec_user"),
            () -> createSpecUser(HamonSpecUser::new, "hamon_spec_user")
    );

    RegistrySupplier<EntityType<VampireSpecUser>> VAMPIRE_SPEC_USER = ENTITY_TYPE_REGISTRY.register(
            JCraft.id("vampire_spec_user"),
            () -> createSpecUser(VampireSpecUser::new, "vampire_spec_user")
    );

    RegistrySupplier<EntityType<AnubisSpecUser>> ANUBIS_SPEC_USER = ENTITY_TYPE_REGISTRY.register(
            JCraft.id("anubis_spec_user"),
            () -> createSpecUser(AnubisSpecUser::new, "anubis_spec_user")
    );

    RegistrySupplier<EntityType<LivingEntity>> RANDOM_SPEC_USER = ENTITY_TYPE_REGISTRY.register(
            JCraft.id("random_spec_user"),
            () -> EntityType.Builder.<LivingEntity>of(
                    (type, world) -> switch (world.getRandom().nextInt(2)) {
                        case (0) -> new BrawlerSpecUser(world);
                        case (1) -> new VampireSpecUser(world);
                        case (2) -> new AnubisSpecUser(world);
                        default -> throw new IllegalStateException("Unexpected value");
                    },
                    MobCategory.CREATURE
            ).sized(0.6f, 1.8f).build("random_spec_user")
    );

    static void registerAttributes() {
        EntityAttributeRegistry.register(STAR_PLATINUM, StarPlatinumEntity::createMobAttributes);
        EntityAttributeRegistry.register(SPTW, SPTWEntity::createMobAttributes);
        EntityAttributeRegistry.register(KING_CRIMSON, KingCrimsonEntity::createMobAttributes);
        EntityAttributeRegistry.register(CREAM, CreamEntity::createMobAttributes);
        EntityAttributeRegistry.register(KILLER_QUEEN, KillerQueenEntity::createMobAttributes);
        EntityAttributeRegistry.register(KILLER_QUEEN_BITES_THE_DUST, KQBTDEntity::createMobAttributes);
        EntityAttributeRegistry.register(SHEER_HEART_ATTACK, () -> SheerHeartAttackEntity.createMobAttributes()
                .add(Attributes.ARMOR, 20)
                .add(Attributes.ARMOR_TOUGHNESS, 12)
                .add(Attributes.MOVEMENT_SPEED, 0.15));

        EntityAttributeRegistry.register(WHITE_SNAKE, WhiteSnakeEntity::createMobAttributes);
        EntityAttributeRegistry.register(C_MOON, CMoonEntity::createMobAttributes);

        EntityAttributeRegistry.register(MANDOM, MandomEntity::createMobAttributes);

        EntityAttributeRegistry.register(MADE_IN_HEAVEN, MadeInHeavenEntity::createMobAttributes);
        EntityAttributeRegistry.register(SHADOW_THE_WORLD, ShadowTheWorldEntity::createMobAttributes);
        EntityAttributeRegistry.register(THE_WORLD, TheWorldEntity::createMobAttributes);
        EntityAttributeRegistry.register(THE_WORLD_OVER_HEAVEN, TheWorldOverHeavenEntity::createMobAttributes);
        EntityAttributeRegistry.register(SILVER_CHARIOT, SilverChariotEntity::createMobAttributes);
        EntityAttributeRegistry.register(MAGICIANS_RED, MagiciansRedEntity::createMobAttributes);
        EntityAttributeRegistry.register(THE_FOOL, TheFoolEntity::createMobAttributes);
        EntityAttributeRegistry.register(HIEROPHANT_GREEN, HGEntity::createMobAttributes);
        EntityAttributeRegistry.register(THE_SUN, TheSunEntity::createMobAttributes);
        EntityAttributeRegistry.register(HORUS, HorusEntity::createMobAttributes);
        EntityAttributeRegistry.register(CRAZY_DIAMOND, CrazyDiamondEntity::createMobAttributes);
        EntityAttributeRegistry.register(AEROSMITH, AerosmithEntity::createMobAttributes);
        EntityAttributeRegistry.register(CINDERELLA, CinderellaEntity::createMobAttributes);
        EntityAttributeRegistry.register(OSIRIS, OsirisEntity::createMobAttributes);
        EntityAttributeRegistry.register(ATUM, AtumEntity::createMobAttributes);
        EntityAttributeRegistry.register(DIVER_DOWN, DiverDownEntity::createMobAttributes);
        EntityAttributeRegistry.register(CHARIOT_REQUIEM, ChariotRequiemEntity::createMobAttributes);
        EntityAttributeRegistry.register(DRAGONS_DREAM, DragonsDreamEntity::createMobAttributes);
        EntityAttributeRegistry.register(FOO_FIGHTERS, FooFightersEntity::createMobAttributes);
        EntityAttributeRegistry.register(GOO_GOO_DOLLS, GooGooDollsEntity::createMobAttributes);

        EntityAttributeRegistry.register(PURPLE_HAZE, () -> AbstractPurpleHazeEntity.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.55));
        EntityAttributeRegistry.register(PURPLE_HAZE_DISTORTION, AbstractPurpleHazeEntity::createMobAttributes);
        EntityAttributeRegistry.register(GOLD_EXPERIENCE, GoldExperienceEntity::createMobAttributes);
        EntityAttributeRegistry.register(GER, GEREntity::createMobAttributes);
        //EntityAttributeRegistry.register(GE_TREE, GETreeEntity::createLivingAttributes);
        EntityAttributeRegistry.register(GE_FROG, GEFrogEntity::createAttributes);
        EntityAttributeRegistry.register(GE_BUTTERFLY, GEButterflyEntity::createButterflyAttributes);
        EntityAttributeRegistry.register(GE_SNAKE, () -> GESnakeEntity.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.ATTACK_DAMAGE, 4)
                .add(Attributes.ATTACK_KNOCKBACK, 0));

        EntityAttributeRegistry.register(TRAINING_DUMMY, TrainingDummyEntity::createLivingAttributes);
        EntityAttributeRegistry.register(GER_SCORPION, () -> GERScorpionEntity.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.ATTACK_DAMAGE, 0)
                .add(Attributes.ATTACK_KNOCKBACK, 0));
        EntityAttributeRegistry.register(D4C, D4CEntity::createMobAttributes);
        EntityAttributeRegistry.register(PLAYER_CLONE, PlayerCloneEntity::createCloneAttributes);
        EntityAttributeRegistry.register(HG_NET, HGNetEntity::createNetAttributes);
        EntityAttributeRegistry.register(LIFE_DETECTOR, LifeDetectorEntity::createDetectorAttributes);
        EntityAttributeRegistry.register(RED_BIND, RedBindEntity::createLivingAttributes);
        EntityAttributeRegistry.register(BLOCK_PROJECTILE, BlockProjectile::createBlockAttributes);
        EntityAttributeRegistry.register(SAND_TORNADO, SandTornadoEntity::createTornadoAttributes);
        EntityAttributeRegistry.register(HAMON_WAVE, HamonWaveEntity::createLivingAttributes);

        EntityAttributeRegistry.register(PETSHOP, PetshopEntity::createPetshopAttributes);
        EntityAttributeRegistry.register(AYA_TSUJI, AyaTsujiEntity::createAyaTsujiAttributes);
        EntityAttributeRegistry.register(DARBY_OLDER, DarbyOlderEntity::createDarbyOlderAttributes);
        EntityAttributeRegistry.register(DARBY_YOUNGER, DarbyYoungerEntity::createDarbyYoungerAttributes);
        EntityAttributeRegistry.register(TONPETTY, TonpettyEntity::createTonpettiAttributes);

        EntityAttributeRegistry.register(METALLICA, MetallicaEntity::createMobAttributes);
        EntityAttributeRegistry.register(THE_HAND, TheHandEntity::createMobAttributes);

        EntityAttributeRegistry.register(STAND_METEOR, StandMeteorEntity::createMobAttributes);

        EntityAttributeRegistry.register(ROAD_ROLLER, () -> RoadRollerEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 40)
                .add(Attributes.ARMOR, 2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
        );

        EntityAttributeRegistry.register(BRAWLER_SPEC_USER, BrawlerSpecUser::createUserAttributes);
        EntityAttributeRegistry.register(HAMON_SPEC_USER, HamonSpecUser::createUserAttributes);
        EntityAttributeRegistry.register(VAMPIRE_SPEC_USER, VampireSpecUser::createUserAttributes);
        EntityAttributeRegistry.register(ANUBIS_SPEC_USER, AnubisSpecUser::createUserAttributes);
        EntityAttributeRegistry.register(RANDOM_SPEC_USER, Mob::createMobAttributes);
    }

    static void init() {
    }

    @RequiredArgsConstructor(staticName = "from")
    class WorldOnlyEntityFactory<T extends Entity> implements EntityType.EntityFactory<T> {
        private final Function<Level, T> ctor;

        @Override
        public @NonNull T create(@NonNull EntityType<T> type, @NotNull Level world) {
            return ctor.apply(world);
        }
    }
}
