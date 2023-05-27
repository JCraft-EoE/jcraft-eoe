package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface JEntityTypeRegister {

    EntityType<StarPlatinumEntity> STAR_PLATINUM = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "starplatinum"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, StarPlatinumEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<KingCrimsonEntity> KING_CRIMSON = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "kingcrimson"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, KingCrimsonEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<TheWorldEntity> THE_WORLD = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "theworld"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TheWorldEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<D4CEntity> D4C = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "d4c"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, D4CEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<CreamEntity> CREAM = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "cream"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, CreamEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<KillerQueenEntity> KILLER_QUEEN = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "killerqueen"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, KillerQueenEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<KQBTDEntity> KILLER_QUEEN_BITES_THE_DUST = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "kqbtd"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, KQBTDEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );
    EntityType<SheerHeartAttackEntity> SHEER_HEART_ATTACK = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "sha"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, SheerHeartAttackEntity::new).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build()
    );

    EntityType<WhitesnakeEntity> WHITE_SNAKE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "whitesnake"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WhitesnakeEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<CMoonEntity> C_MOON = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "cmoon"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, CMoonEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<MadeInHeavenEntity> MADE_IN_HEAVEN = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "mih"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, MadeInHeavenEntity::new).dimensions(EntityDimensions.fixed(0.6f, 2.1f)).build()
    );

    EntityType<TheWorldOverHeavenEntity> THE_WORLD_OVER_HEAVEN = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "twoh"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TheWorldOverHeavenEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<SilverChariotEntity> SILVER_CHARIOT = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "silverchariot"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, SilverChariotEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<MagiciansRedEntity> MAGICIANS_RED = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "mr"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, MagiciansRedEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<TheFoolEntity> THE_FOOL = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "thefool"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TheFoolEntity::new).dimensions(EntityDimensions.fixed(2f, 2f)).build()
    );

    EntityType<GoldenExperienceEntity> GOLDEN_EXPERIENCE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "goldenexperience"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GoldenExperienceEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );
    EntityType<GETreeEntity> GE_TREE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "getree"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, GETreeEntity::new).dimensions(EntityDimensions.fixed(0.6f, 0.8f)).build()
    );
    EntityType<GESnakeEntity> GE_SNAKE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "gesnake"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GESnakeEntity::new).dimensions(EntityDimensions.fixed(1f, 0.3f)).build()
    );
    EntityType<GEREntity> GER = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "ger"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GEREntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );
    EntityType<GERScorpionEntity> GER_SCORPION = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "gerscorpion"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GERScorpionEntity::new).dimensions(EntityDimensions.fixed(0.4f, 0.4f)).build()
    );

    // D4C clone fuckery
    EntityType<PlayerCloneEntity> PLAYER_ENTITY_CLONE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "playerclone"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, PlayerCloneEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    EntityType<PlayerCloneEntity> PLAYER_ENTITY_CLONE_SLIM = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "playerclone_slim"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, PlayerCloneEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    // Take note of the extra <KnifeProjectile> and tracked values
    EntityType<KnifeProjectile> KNIFE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "knife"),
            FabricEntityTypeBuilder.<KnifeProjectile>create(SpawnGroup.MISC, KnifeProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(6).trackedUpdateRate(10).build()
    );

    EntityType<AnkhProjectile> ANKH = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "ankh"),
            FabricEntityTypeBuilder.<AnkhProjectile>create(SpawnGroup.MISC, AnkhProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.75f, 0.75f)).trackRangeChunks(6).trackedUpdateRate(20).build()
    );

    EntityType<BubbleProjectile> BUBBLE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "bubble"),
            FabricEntityTypeBuilder.<BubbleProjectile>create(SpawnGroup.MISC, BubbleProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(8).trackedUpdateRate(20).build()
    );

    EntityType<BloodProjectile> BLOOD_PROJECTILE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "bloodprojectile"),
            FabricEntityTypeBuilder.<BloodProjectile>create(SpawnGroup.MISC, BloodProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(4).trackedUpdateRate(10).build()
    );

    static void registerEntities() {
        FabricDefaultAttributeRegistry.register(STAR_PLATINUM, StarPlatinumEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(KING_CRIMSON, KingCrimsonEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(CREAM, CreamEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(KILLER_QUEEN, KillerQueenEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(KILLER_QUEEN_BITES_THE_DUST, KQBTDEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(SHEER_HEART_ATTACK, SheerHeartAttackEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_ARMOR, 10)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 10)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.15)
        );

        FabricDefaultAttributeRegistry.register(WHITE_SNAKE, WhitesnakeEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(C_MOON, CMoonEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(MADE_IN_HEAVEN, MadeInHeavenEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(THE_WORLD, TheWorldEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(THE_WORLD_OVER_HEAVEN, TheWorldOverHeavenEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(SILVER_CHARIOT, SilverChariotEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(MAGICIANS_RED, MagiciansRedEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(THE_FOOL, TheFoolEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(GOLDEN_EXPERIENCE, GoldenExperienceEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(GER, GEREntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(GE_SNAKE, SheerHeartAttackEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0)
        );

        FabricDefaultAttributeRegistry.register(GER_SCORPION, SheerHeartAttackEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0)
        );

        FabricDefaultAttributeRegistry.register(D4C, D4CEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(PLAYER_ENTITY_CLONE, D4CEntity.createMobAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3));
        FabricDefaultAttributeRegistry.register(PLAYER_ENTITY_CLONE_SLIM, D4CEntity.createMobAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3));
    }
}
