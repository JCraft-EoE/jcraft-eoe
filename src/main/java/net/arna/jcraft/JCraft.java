package net.arna.jcraft;

import eu.midnightdust.lib.config.MidnightConfig;
import net.arna.jcraft.client.network.s2c.ServerChannelFeedback;
import net.arna.jcraft.common.JCommonConfig;
import net.arna.jcraft.common.entity.*;
import net.arna.jcraft.common.network.c2s.StandControlPacket;
import net.arna.jcraft.common.util.AttackQueue;
import net.arna.jcraft.common.util.DimValues;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.example.GeckoLibMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.arna.jcraft.common.entity.StandEntity.Stun;

//initialize your FUCKING variables, arna
//todo: add static IDs for stuff like stands and attacks to make checks for individual ones less expensive
//todo: add working out
public class JCraft implements ModInitializer {

    // Unchanging mod values
    public static final String MOD_ID = "jcraft";
    public static final int STAND_COUNT = 11;
    public static final int EVOLUTION_COUNT = 5;
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);


    public static final int SPEC_QUEUE_MOVESTUN_LIMIT = 11; // exclusive, 10 -> 0.5s window for queueing moves
    public static final int QUEUE_MOVESTUN_LIMIT = 7; // exclusive, 6 -> 0.3s window for queueing moves

    // Stand Cooldowns
    public static final String standLightCD = "SLightCD";
    public static final String standHeavyCD = "SHeavyCD";
    public static final String standBarrageCD = "SBarrageCD";
    public static final String standS1CD = "SS1CD";
    public static final String standS2CD = "SS2CD";
    public static final String standS3CD = "SS3CD";
    public static final String standUltCD = "SUltCD";

    // Spec Cooldowns
    public static final String heavyCD = "HeavyCD";
    public static final String barrageCD = "BarrageCD";
    public static final String s1CD = "S1CD";
    public static final String s2CD = "S2CD";
    public static final String s3CD = "S3CD";
    public static final String ultCD = "UltCD";

    // Universal Cooldowns
    public static final String standMMBCD = "M3CD";
    public static final String standCBCD = "CBCD";
    public static final String standCCCD = "CCCD";

    public static List<String> cooldowns = List.of(
            standLightCD, standHeavyCD, standBarrageCD, standUltCD, standS1CD, standS2CD, standS3CD,
            standMMBCD, standCBCD, standCCCD,
            heavyCD, barrageCD, ultCD, s1CD, s2CD, s3CD);

    // Gamerules
    public static GameRules.Key<GameRules.BooleanRule> SHOW_HITBOXES = GameRuleRegistry.register("showHitboxes", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
    public static GameRules.Key<GameRules.BooleanRule> KINGCRIMSON_TELEPORT_EFFECT = GameRuleRegistry.register("kingCrimsonTeleportEffect", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
    public static GameRules.Key<GameRules.BooleanRule> COMBO_COUNTER = GameRuleRegistry.register("comboCounter", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    public static GameRules.Key<GameRules.IntRule> CHANCE_MOB_SPAWNS_WITH_STAND = GameRuleRegistry.register("chanceMobSpawnsWithStand", GameRules.Category.MOBS, GameRuleFactory.createIntRule(5, 0, 100));
    public static GameRules.Key<GameRules.BooleanRule> ALLOW_MOB_EVOLVED_STANDS = GameRuleRegistry.register("allowMobEvolvedStands", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(false));
    public static GameRules.Key<GameRules.BooleanRule> CREAM_BLACK_HOLE_ERASES_BLOCKS = GameRuleRegistry.register("creamBlackHoleErasesBlocks", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    public static GameRules.Key<GameRules.IntRule> DEFAULT_SPEC = GameRuleRegistry.register("defaultSpec", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(0, 0, 1));
    //public static GameRules.Key<GameRules.IntRule> DAMAGE_MULT = GameRuleRegistry.register("jcraftDamageMult", GameRules.Category.MISC, GameRuleFactory.createIntRule(0, 0, 100));

    // Dimensional travel bullshit
    public static ArrayList<DimValues> pastDimensions = new ArrayList<>();
    private static final List<ChunkPos> preloadedChunks = new ArrayList<>();

    public static void ClearPreloadedChunks(ServerWorld auWorld) {
        if (preloadedChunks.isEmpty()) {
            return;
        }
        for (ChunkPos p : preloadedChunks)
            auWorld.setChunkForced(p.x, p.z, false);
        preloadedChunks.clear();
    }

    public static void PreloadChunk(ServerWorld auWorld, int chunkX, int chunkY) {
        preloadedChunks.add(new ChunkPos(chunkX, chunkY));
        auWorld.setChunkForced(chunkX, chunkY, true);
    }

    // Item group
    public static final ItemGroup JCRAFT_GROUP = FabricItemGroupBuilder.create(new Identifier(MOD_ID, "main"))
            .icon(() -> new ItemStack(JObjectRegistry.STANDARROW))
            .appendItems((itemStacks -> {
                itemStacks.add(new ItemStack(JObjectRegistry.STANDDISC));

                itemStacks.add(new ItemStack(JObjectRegistry.STANDARROW));
                itemStacks.add(new ItemStack(JObjectRegistry.LIVINGARROW));
                itemStacks.add(new ItemStack(JObjectRegistry.REQUIEMARROW));
                itemStacks.add(new ItemStack(JObjectRegistry.REQUIEMRUBY));

                itemStacks.add(new ItemStack(JObjectRegistry.ANUBIS));
                itemStacks.add(new ItemStack(JObjectRegistry.ANUBISSHEATHED));
                itemStacks.add(new ItemStack(JObjectRegistry.KNIFE));
                itemStacks.add(new ItemStack(JObjectRegistry.KNIFEBUNDLE));
                itemStacks.add(JObjectRegistry.FVREVOLVER.getDefaultStack());

                itemStacks.add(new ItemStack(JObjectRegistry.SINNERSSOUL));
                itemStacks.add(new ItemStack(JObjectRegistry.SOUL_BLOCK.asItem()));
                itemStacks.add(new ItemStack(JObjectRegistry.GREENBABY));
                itemStacks.add(new ItemStack(JObjectRegistry.DIOSDIARY));

                itemStacks.add(new ItemStack(JObjectRegistry.BOXINGGLOVES));

                itemStacks.add(new ItemStack(JObjectRegistry.DIOHEADBAND));
                itemStacks.add(new ItemStack(JObjectRegistry.DIOJACKET));
                itemStacks.add(new ItemStack(JObjectRegistry.DIOPANTS));
                itemStacks.add(new ItemStack(JObjectRegistry.DIOBOOTS));

                itemStacks.add(new ItemStack(JObjectRegistry.JOTAROCAP));
                itemStacks.add(new ItemStack(JObjectRegistry.JOTAROJACKET));
                itemStacks.add(new ItemStack(JObjectRegistry.JOTAROPANTS));
                itemStacks.add(new ItemStack(JObjectRegistry.JOTAROBOOTS));

                itemStacks.add(new ItemStack(JObjectRegistry.KQCOIN));
            }))
            .build();

    // Stand names
    public static final Map<Integer, MutableText> standNames = Map.ofEntries(
            Map.entry(1, Text.translatable("entity.jcraft.starplatinum")),
            Map.entry(2, Text.translatable("entity.jcraft.theworld")),
            Map.entry(3, Text.translatable("entity.jcraft.kingcrimson")),
            Map.entry(4, Text.translatable("entity.jcraft.d4c")),
            Map.entry(5, Text.translatable("entity.jcraft.cream")),
            Map.entry(6, Text.translatable("entity.jcraft.killerqueen")),
            Map.entry(7, Text.translatable("entity.jcraft.whitesnake")),
            Map.entry(8, Text.translatable("entity.jcraft.silverchariot")),
            Map.entry(9, Text.translatable("entity.jcraft.mr")),
            Map.entry(10, Text.translatable("entity.jcraft.thefool")),
            Map.entry(11, Text.translatable("entity.jcraft.goldenexperience")),

            Map.entry(-1, Text.translatable("entity.jcraft.cmoon")),
            Map.entry(-2, Text.translatable("entity.jcraft.mih")),
            Map.entry(-3, Text.translatable("entity.jcraft.twoh")),
            Map.entry(-4, Text.translatable("entity.jcraft.kqbtd")),
            Map.entry(-5, Text.translatable("entity.jcraft.ger"))
    );

    // Buttons to IDs and vice versa
    public static final Map<Integer, AttackQueue> idToButton = Map.ofEntries(
            Map.entry(0, AttackQueue.LIGHT),
            Map.entry(1, AttackQueue.HEAVY),
            Map.entry(2, AttackQueue.BARRAGE),
            Map.entry(3, AttackQueue.SPECIAL1),
            Map.entry(4, AttackQueue.ULTIMATE),
            Map.entry(5, AttackQueue.SPECIAL2),
            Map.entry(6, AttackQueue.SPECIAL3),
            Map.entry(7, AttackQueue.MIDDLEMOUSE)
    );

    public static final Map<AttackQueue, Integer> buttonToId = Map.ofEntries(
            Map.entry(AttackQueue.LIGHT, 0),
            Map.entry(AttackQueue.HEAVY, 1),
            Map.entry(AttackQueue.BARRAGE, 2),
            Map.entry(AttackQueue.SPECIAL1, 3),
            Map.entry(AttackQueue.ULTIMATE, 4),
            Map.entry(AttackQueue.SPECIAL2, 5),
            Map.entry(AttackQueue.SPECIAL3, 6),
            Map.entry(AttackQueue.MIDDLEMOUSE, 7)
    );

    public static StandEntity Summon(ServerWorld world, LivingEntity player) {
        if (player.hasStatusEffect(JStatusRegister.Standless)) {
            return null;
        }

        StandEntity stand = null;

        //CMoonEntity(ModEntityRegister.MIH, world) works and i don't like that :(
        switch (((IEntityDataSaver) player).getPersistentData().getInt("StandID")) {
            case 1 -> stand = new StarPlatinumEntity(JEntityTypeRegister.STAR_PLATINUM, world);
            case 2 -> stand = new TheWorldEntity(JEntityTypeRegister.THE_WORLD, world);
            case 3 -> stand = new KingCrimsonEntity(JEntityTypeRegister.KING_CRIMSON, world);
            case 4 -> stand = new D4CEntity(JEntityTypeRegister.D4C, world);
            case 5 -> stand = new CreamEntity(JEntityTypeRegister.CREAM, world);
            case 6 -> stand = new KillerQueenEntity(JEntityTypeRegister.KILLER_QUEEN, world);
            case 7 -> stand = new WhitesnakeEntity(JEntityTypeRegister.WHITE_SNAKE, world);
            case 8 -> stand = new SilverChariotEntity(JEntityTypeRegister.SILVER_CHARIOT, world);
            case 9 -> stand = new MagiciansRedEntity(JEntityTypeRegister.MAGICIANS_RED, world);
            case 10 -> stand = new TheFoolEntity(JEntityTypeRegister.THE_FOOL, world);
            case 11 -> stand = new GoldenExperienceEntity(JEntityTypeRegister.GOLDEN_EXPERIENCE, world);
            // All evolutions have a negative ID
            case -1 -> stand = new CMoonEntity(JEntityTypeRegister.C_MOON, world);
            case -2 -> stand = new MadeInHeavenEntity(JEntityTypeRegister.MADE_IN_HAVEN, world);
            case -3 -> stand = new TheWorldOverHeavenEntity(JEntityTypeRegister.THE_WORLD_OVER_HEAVEN, world);
            case -4 -> stand = new KQBTDEntity(JEntityTypeRegister.KILLER_QUEEN_BITES_THE_DUST, world);
            case -5 -> stand = new GEREntity(JEntityTypeRegister.GER, world);
        }

        if (stand != null) {
            stand.setPosition(player.getPos().subtract(player.getRotationVector()));
            stand.startRiding(player);
            stand.setUser(player);
            world.spawnEntity(stand);
            return stand;
        }

        return null;
    }

    public static Identifier id(String name) {
        return new Identifier(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        MidnightConfig.init(MOD_ID, JCommonConfig.class);
        // Particle registration (serverside)
        JParticleTypeRegistry.initParticleTypes();

        // Geckolib
        GeckoLibMod.DISABLE_IN_DEV = true;

        // Registration
        JObjectRegistry.init();
        JCommandRegister.registerCommands();
        JEventsRegister.registerEvents();
        JStatusRegister.registerStatuses();
        JSoundRegister.registerSounds();
        JEntityTypeRegister.registerEntities();
        JDimensionRegister.registerDimensions();
        ServerPlayNetworking.registerGlobalReceiver(StandControlPacket.ID, StandControlPacket::handle);
    }


    public static void CreateParticle(ServerWorld world, double x, double y, double z, int id) {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeShort(8);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeInt(id); // Combo breaker particle ID

        for (ServerPlayerEntity serverPlayer : world.getPlayers()) {
            ServerChannelFeedback.send(serverPlayer, buf);
        }
    }

    public static List<String> unresettableCooldowns = List.of(standBarrageCD, standUltCD, barrageCD, ultCD, standCBCD, standCCCD);

    public static void CooldownCancel(ServerWorld world, LivingEntity player) {
        NbtCompound data = ((IEntityDataSaver) player).getPersistentData();

        if (data.getInt(standCCCD) <= 0) {
            for (String cooldownType : cooldowns) {
                if (unresettableCooldowns.contains(cooldownType)) {
                    continue;
                }
                data.putInt(cooldownType, 0);
            }

            data.putInt(standCCCD, 900); // 45s

            Vec3d pPos = player.getEyePos();
            world.playSoundFromEntity(null, player, JSoundRegister.COOLDOWN_CANCEL, SoundCategory.PLAYERS, 1, 1);
            CreateParticle(world, pPos.x, pPos.y, pPos.z, 1);
        }
    }

    public static HashMap<LivingEntity, Integer> burstTimers = new HashMap<>();

    public static void ComboBreak(ServerWorld world, LivingEntity player, StatusEffectInstance stun) {
        NbtCompound data = ((IEntityDataSaver) player).getPersistentData();
        //if (!user.getPersistentData().contains(JCraft.standCBCD)) { user.getPersistentData().putInt(JCraft.standCBCD, 0); } // Handled elsewhere
        if (stun.getDuration() > 1 && stun.getAmplifier() == 1 && data.getInt(standCBCD) <= 0) {
            data.putInt(standCBCD, 1200); // 60s

            Stun(player, 5, 2); // Player is slowed down considerably pre-burst

            world.playSoundFromEntity(null, player, JSoundRegister.COMBO_BREAK, SoundCategory.PLAYERS, 1, 1);

            Vec3d pPos = player.getEyePos();
            burstTimers.put(player, 4);
            CreateParticle(world, pPos.x, pPos.y, pPos.z, 0);
        }
    }

    public static Entity teleportToWorld(Entity e, ServerWorld w, double x, double y, double z) {
        if (!e.isRemoved()) {
            e.detach();
            Entity entity = e.getType().create(w);
            if (entity != null) {
                entity.copyFrom(e);
                entity.refreshPositionAndAngles(x, y, z, e.getYaw(), e.getPitch());
                entity.setVelocity(e.getVelocity());
                w.onDimensionChanged(entity);
                e.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
                w.resetIdleTimeout();
                return entity;
            }
        }
        return null;
    }

    public static void DimensionHop(Entity entity, int heightOffset) {
        ServerWorld original = (ServerWorld) entity.getWorld();
        MinecraftServer server = original.getServer();
        ServerWorld au = server.getWorld(JDimensionRegister.AU_DIMENSION_KEY);
        if (original == au) {
            return;
        }

        Vec3d pos = entity.getPos();
        Entity finalEnt = entity;

        if (entity instanceof ServerPlayerEntity player) {
            player.teleport(au, pos.x, pos.y - heightOffset, pos.z, entity.getYaw(), entity.getPitch());
            //todo: fix ability to get stuck in hell
        } else {
            finalEnt = teleportToWorld(entity, au, entity.getX(), entity.getY() - heightOffset, entity.getZ());
        }

        pastDimensions.add(new DimValues(finalEnt, pos, original.getRegistryKey()));
        au.playSound(null, pos.x, pos.y - heightOffset, pos.z, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }
}
