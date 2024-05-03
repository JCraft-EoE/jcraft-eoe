package net.arna.jcraft.registry;

import lombok.RequiredArgsConstructor;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.*;
import net.arna.jcraft.common.entity.projectile.*;
import net.arna.jcraft.common.entity.stand.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.World;

import java.util.function.Function;

public interface JEntityTypeRegistry {

    EntityType<StarPlatinumEntity> STAR_PLATINUM = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("starplatinum"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(StarPlatinumEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<SPTWEntity> SPTW = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("sptw"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(SPTWEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<KingCrimsonEntity> KING_CRIMSON = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("kingcrimson"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(KingCrimsonEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<TheWorldEntity> THE_WORLD = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("theworld"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(TheWorldEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<D4CEntity> D4C = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("d4c"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(D4CEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<CreamEntity> CREAM = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("cream"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(CreamEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<KillerQueenEntity> KILLER_QUEEN = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("killerqueen"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(KillerQueenEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<KQBTDEntity> KILLER_QUEEN_BITES_THE_DUST = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("kqbtd"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(KQBTDEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<SheerHeartAttackEntity> SHEER_HEART_ATTACK = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("sha"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, SheerHeartAttackEntity::new).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build()
    );

    EntityType<WhiteSnakeEntity> WHITE_SNAKE = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("whitesnake"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(WhiteSnakeEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<CMoonEntity> C_MOON = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("cmoon"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(CMoonEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<MadeInHeavenEntity> MADE_IN_HEAVEN = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("mih"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(MadeInHeavenEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 2.1f)).build()
    );

    EntityType<TheWorldOverHeavenEntity> THE_WORLD_OVER_HEAVEN = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("twoh"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(TheWorldOverHeavenEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<SilverChariotEntity> SILVER_CHARIOT = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("silverchariot"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(SilverChariotEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<MagiciansRedEntity> MAGICIANS_RED = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("mr"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(MagiciansRedEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<TheFoolEntity> THE_FOOL = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("thefool"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(TheFoolEntity::new))
                    .dimensions(EntityDimensions.fixed(2f, 2f))
                    .fireImmune()
                    .build()
    );

    EntityType<GoldExperienceEntity> GOLD_EXPERIENCE = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("goldexperience"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(GoldExperienceEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );
    EntityType<GETreeEntity> GE_TREE = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("getree"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, ((EntityType<GETreeEntity> type, World world) -> new GETreeEntity(type, world))).dimensions(EntityDimensions.fixed(0.6f, 0.8f)).build()
    );
    EntityType<GESnakeEntity> GE_SNAKE = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("gesnake"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GESnakeEntity::new).dimensions(EntityDimensions.fixed(1f, 0.3f)).build()
    );
    EntityType<GEFrogEntity> GE_FROG = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("gefrog"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GEFrogEntity::new).dimensions(EntityDimensions.fixed(0.3f, 0.3f)).build()
    );
    EntityType<GEButterflyEntity> GE_BUTTERFLY = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("gebutterfly"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GEButterflyEntity::new).dimensions(EntityDimensions.fixed(0.3f, 0.3f)).build()
    );

    EntityType<HGEntity> HIEROPHANT_GREEN = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("hierophant_green"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(HGEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<TheSunEntity> THE_SUN = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("the_sun"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(TheSunEntity::new)).dimensions(EntityDimensions.fixed(2f, 2f)).build()
    );

    EntityType<PurpleHazeEntity> PURPLE_HAZE = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("purple_haze"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(PurpleHazeEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<PurpleHazeDistortionEntity> PURPLE_HAZE_DISTORTION = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("purple_haze_distortion"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(PurpleHazeDistortionEntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<GEREntity> GER = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("ger"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WorldOnlyEntityFactory.from(GEREntity::new)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );
    EntityType<GERScorpionEntity> GER_SCORPION = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("gerscorpion"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GERScorpionEntity::new).dimensions(EntityDimensions.fixed(0.4f, 0.4f)).build()
    );

    // Player clone
    EntityType<PlayerCloneEntity> PLAYER_CLONE = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("playerclone"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, (EntityType<PlayerCloneEntity> entityType, World world) -> new PlayerCloneEntity(world)).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    // Take note of the extra <KnifeProjectile> and tracked values
    EntityType<KnifeProjectile> KNIFE = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("knife"),
            FabricEntityTypeBuilder.<KnifeProjectile>create(SpawnGroup.MISC, KnifeProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(6).trackedUpdateRate(10).build()
    );

    EntityType<EmeraldProjectile> EMERALD = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("emerald"),
            FabricEntityTypeBuilder.<EmeraldProjectile>create(SpawnGroup.MISC, EmeraldProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(6).trackedUpdateRate(15).build()
    );

    EntityType<BulletProjectile> BULLET = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("bullet"),
            FabricEntityTypeBuilder.<BulletProjectile>create(SpawnGroup.MISC, BulletProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.1f, 0.1f)).trackRangeChunks(6).trackedUpdateRate(10).build()
    );

    EntityType<RapierProjectile> RAPIER = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("rapier"),
            FabricEntityTypeBuilder.<RapierProjectile>create(SpawnGroup.MISC, RapierProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(6).trackedUpdateRate(15).build()
    );

    EntityType<AnkhProjectile> ANKH = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("ankh"),
            FabricEntityTypeBuilder.<AnkhProjectile>create(SpawnGroup.MISC, AnkhProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.75f, 0.75f)).trackRangeChunks(6).trackedUpdateRate(20).build()
    );

    EntityType<MeteorProjectile> METEOR = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("meteor"),
            FabricEntityTypeBuilder.<MeteorProjectile>create(SpawnGroup.MISC, MeteorProjectile::new)
                    .dimensions(EntityDimensions.fixed(1.0f, 1.0f)).trackRangeChunks(6).trackedUpdateRate(20).build()
    );

    EntityType<BubbleProjectile> BUBBLE = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("bubble"),
            FabricEntityTypeBuilder.<BubbleProjectile>create(SpawnGroup.MISC, BubbleProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(8).trackedUpdateRate(20).build()
    );

    EntityType<BloodProjectile> BLOOD_PROJECTILE = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("bloodprojectile"),
            FabricEntityTypeBuilder.<BloodProjectile>create(SpawnGroup.MISC, BloodProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(4).trackedUpdateRate(10).build()
    );

    EntityType<LaserProjectile> LASER_PROJECTILE = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("laserprojectile"),
            FabricEntityTypeBuilder.<LaserProjectile>create(SpawnGroup.MISC, LaserProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(4).trackedUpdateRate(10).build()
    );

    EntityType<PHCapsuleProjectile> PH_CAPSULE = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("ph_capsule"),
            FabricEntityTypeBuilder.<PHCapsuleProjectile>create(SpawnGroup.MISC, (type, world) -> new PHCapsuleProjectile(world))
                    .dimensions(EntityDimensions.fixed(0.75f, 0.75f)).trackRangeChunks(6).trackedUpdateRate(20).build()
    );

    EntityType<LifeDetectorEntity> LIFE_DETECTOR = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("lifedetector"),
                FabricEntityTypeBuilder.create(SpawnGroup.MISC, LifeDetectorEntity::new)
                        .dimensions(EntityDimensions.fixed(1f, 1f)).build()
    );

    EntityType<HGNetEntity> HG_NET = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("hg_net"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, HGNetEntity::new)
                    .dimensions(EntityDimensions.fixed(2f, 4f)).build()
    );

    EntityType<RedBindEntity> RED_BIND = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("redbind"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, RedBindEntity::new)
                    .dimensions(EntityDimensions.fixed(1f, 2f)).build()
    );

    EntityType<BlockProjectile> BLOCK_PROJECTILE = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("blockprojectile"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, BlockProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build()
    );

    EntityType<SandTornadoEntity> SAND_TORNADO = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("sandtornado"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, SandTornadoEntity::new)
                    .dimensions(EntityDimensions.fixed(1f, 2f)).build()
    );

    EntityType<WSAcidProjectile> WS_ACID_PROJECTILE = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("wsacidprojectile"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, WorldOnlyEntityFactory.from(WSAcidProjectile::new))
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(4).trackedUpdateRate(10).build()
    );

    EntityType<SunBeamProjectile> SUN_BEAM = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("sunbeam"),
            FabricEntityTypeBuilder.<SunBeamProjectile>create(SpawnGroup.MISC, (type, world) -> new SunBeamProjectile(world))
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(4).trackedUpdateRate(10).build()
    );

    EntityType<PurpleHazeCloudEntity> PURPLE_HAZE_COUD = Registry.register(
            Registries.ENTITY_TYPE,
            JCraft.id("purple_haze_cloud"),
            FabricEntityTypeBuilder.<PurpleHazeCloudEntity>create(SpawnGroup.MISC, (type, world) -> new PurpleHazeCloudEntity(world))
                    .dimensions(EntityDimensions.changing(1.0f, 1.0f)).build()
    );

    static void registerEntities() {
        FabricDefaultAttributeRegistry.register(STAR_PLATINUM, StarPlatinumEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(SPTW, SPTWEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(KING_CRIMSON, KingCrimsonEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(CREAM, CreamEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(KILLER_QUEEN, KillerQueenEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(KILLER_QUEEN_BITES_THE_DUST, KQBTDEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(SHEER_HEART_ATTACK, SheerHeartAttackEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_ARMOR, 20)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 12)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.15)
        );

        FabricDefaultAttributeRegistry.register(WHITE_SNAKE, WhiteSnakeEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(C_MOON, CMoonEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(MADE_IN_HEAVEN, MadeInHeavenEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(THE_WORLD, TheWorldEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(THE_WORLD_OVER_HEAVEN, TheWorldOverHeavenEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(SILVER_CHARIOT, SilverChariotEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(MAGICIANS_RED, MagiciansRedEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(THE_FOOL, TheFoolEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(HIEROPHANT_GREEN, HGEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(THE_SUN, TheSunEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(PURPLE_HAZE, AbstractPurpleHazeEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.55)
        );
        FabricDefaultAttributeRegistry.register(PURPLE_HAZE_DISTORTION, AbstractPurpleHazeEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(GOLD_EXPERIENCE, GoldExperienceEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(GER, GEREntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(GE_TREE, GETreeEntity.createLivingAttributes());
        FabricDefaultAttributeRegistry.register(GE_FROG, GEFrogEntity.createFrogAttributes());
        FabricDefaultAttributeRegistry.register(GE_BUTTERFLY, GEButterflyEntity.createButterflyAttributes());
        FabricDefaultAttributeRegistry.register(GE_SNAKE, GESnakeEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0)
        );

        FabricDefaultAttributeRegistry.register(GER_SCORPION, GERScorpionEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0)
        );

        FabricDefaultAttributeRegistry.register(D4C, D4CEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(PLAYER_CLONE, PlayerCloneEntity.createCloneAttributes());

        FabricDefaultAttributeRegistry.register(HG_NET, HGNetEntity.createNetAttributes());

        FabricDefaultAttributeRegistry.register(LIFE_DETECTOR, LifeDetectorEntity.createDetectorAttributes());
        FabricDefaultAttributeRegistry.register(RED_BIND, LifeDetectorEntity.createDetectorAttributes()); // This will also do fine.
        FabricDefaultAttributeRegistry.register(BLOCK_PROJECTILE, BlockProjectile.createBlockAttributes());
        FabricDefaultAttributeRegistry.register(SAND_TORNADO, SandTornadoEntity.createTornadoAttributes());
    }

    @RequiredArgsConstructor(staticName = "from")
    class WorldOnlyEntityFactory<T extends Entity> implements EntityType.EntityFactory<T> {
        private final Function<World, T> ctor;

        @Override
        public T create(EntityType<T> type, World world) {
            return ctor.apply(world);
        }
    }
}
