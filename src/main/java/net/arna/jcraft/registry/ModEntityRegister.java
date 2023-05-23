package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.entity.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModEntityRegister {
    public static final EntityType<StarPlatinumEntity> STARPLATINUM = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "starplatinum"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, StarPlatinumEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    public static final EntityType<KingCrimsonEntity> KINGCRIMSON = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "kingcrimson"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, KingCrimsonEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    public static final EntityType<TheWorldEntity> THEWORLD = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "theworld"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TheWorldEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    public static final EntityType<D4CEntity> D4C = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "d4c"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, D4CEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    public static final EntityType<CreamEntity> CREAM = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "cream"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, CreamEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    public static final EntityType<KillerQueenEntity> KILLERQUEEN = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "killerqueen"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, KillerQueenEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );
    public static final EntityType<KQBTDEntity> KQBTD = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "kqbtd"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, KQBTDEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );
    public static final EntityType<SheerHeartAttackEntity> SHA = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "sha"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, SheerHeartAttackEntity::new).dimensions(EntityDimensions.fixed(0.5f, 0.5f)).build()
    );

    public static final EntityType<WhitesnakeEntity> WHITESNAKE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "whitesnake"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WhitesnakeEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    public static final EntityType<CMoonEntity> CMOON = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "cmoon"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, CMoonEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    public static final EntityType<MadeInHeavenEntity> MIH = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "mih"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, MadeInHeavenEntity::new).dimensions(EntityDimensions.fixed(0.6f, 2.1f)).build()
    );

    public static final EntityType<TheWorldOverHeavenEntity> TWOH = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "twoh"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TheWorldOverHeavenEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    public static final EntityType<SilverChariotEntity> SILVERCHARIOT = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "silverchariot"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, SilverChariotEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    public static final EntityType<MagiciansRedEntity> MAGICIANSRED = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "mr"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, MagiciansRedEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    public static final EntityType<TheFoolEntity> THEFOOL = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "thefool"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TheFoolEntity::new).dimensions(EntityDimensions.fixed(2f, 2f)).build()
    );

    public static final EntityType<GoldenExperienceEntity> GOLDENEXPERIENCE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "goldenexperience"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GoldenExperienceEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );
    public static final EntityType<GETreeEntity> GETREE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "getree"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, GETreeEntity::new).dimensions(EntityDimensions.fixed(0.6f,0.8f)).build()
    );
    public static final EntityType<GESnakeEntity> GESNAKE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "gesnake"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GESnakeEntity::new).dimensions(EntityDimensions.fixed(1f, 0.3f)).build()
    );
    public static final EntityType<GEREntity> GER = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "ger"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GEREntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );
    public static final EntityType<GERScorpionEntity> GERSCORPION = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "gerscorpion"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GERScorpionEntity::new).dimensions(EntityDimensions.fixed(0.4f, 0.4f)).build()
    );

    // D4C clone fuckery
    public static final EntityType<PlayerCloneEntity> PLAYERCLONE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "playerclone"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, PlayerCloneEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );
    public static final EntityType<PlayerCloneEntity> PLAYERCLONE_SLIM = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "playerclone_slim"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, PlayerCloneEntity::new).dimensions(EntityDimensions.fixed(0.6f, 1.8f)).build()
    );

    // Take note of the extra <KnifeProjectile> and tracked values
    public static EntityType<KnifeProjectile> KNIFE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "knife"),
            FabricEntityTypeBuilder.<KnifeProjectile>create(SpawnGroup.MISC, KnifeProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(6).trackedUpdateRate(10).build()
    );

    public static EntityType<AnkhProjectile> ANKH = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "ankh"),
            FabricEntityTypeBuilder.<AnkhProjectile>create(SpawnGroup.MISC, AnkhProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.75f, 0.75f)).trackRangeChunks(6).trackedUpdateRate(20).build()
    );

    public static EntityType<BubbleProjectile> BUBBLE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "bubble"),
            FabricEntityTypeBuilder.<BubbleProjectile>create(SpawnGroup.MISC, BubbleProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(8).trackedUpdateRate(20).build()
    );

    public static EntityType<BloodProjectile> BLOODPROJECTILE = Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier(JCraft.MOD_ID, "bloodprojectile"),
            FabricEntityTypeBuilder.<BloodProjectile>create(SpawnGroup.MISC, BloodProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.5f, 0.5f)).trackRangeChunks(4).trackedUpdateRate(10).build()
    );

    public static void registerEntities() {
        FabricDefaultAttributeRegistry.register(STARPLATINUM, StarPlatinumEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(KINGCRIMSON, KingCrimsonEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(CREAM, CreamEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(KILLERQUEEN, KillerQueenEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(KQBTD, KQBTDEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(SHA, SheerHeartAttackEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_ARMOR, 10)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 10)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.15)
        );

        FabricDefaultAttributeRegistry.register(WHITESNAKE, WhitesnakeEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(CMOON, CMoonEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(MIH, MadeInHeavenEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(THEWORLD, TheWorldEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(TWOH, TheWorldOverHeavenEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(SILVERCHARIOT, SilverChariotEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(MAGICIANSRED, MagiciansRedEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(THEFOOL, TheFoolEntity.createMobAttributes());

        FabricDefaultAttributeRegistry.register(GOLDENEXPERIENCE, GoldenExperienceEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(GER, GEREntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(GESNAKE, SheerHeartAttackEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0)
        );

        FabricDefaultAttributeRegistry.register(GERSCORPION, SheerHeartAttackEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0)
                .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0)
        );

        FabricDefaultAttributeRegistry.register(D4C, D4CEntity.createMobAttributes());
        FabricDefaultAttributeRegistry.register(PLAYERCLONE, D4CEntity.createMobAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3));
        FabricDefaultAttributeRegistry.register(PLAYERCLONE_SLIM, D4CEntity.createMobAttributes().add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3));
    }
}
