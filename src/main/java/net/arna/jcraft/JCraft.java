package net.arna.jcraft;

import eu.midnightdust.lib.config.MidnightConfig;
import lombok.Getter;
import lombok.Setter;
import net.arna.jcraft.common.JConfig;
import net.arna.jcraft.common.entity.StandEntity;
import net.arna.jcraft.common.entity.StandType;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.config.GravityChangerConfig;
import net.arna.jcraft.common.gravity.util.GravityChannel;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.item.StandDiscItem;
import net.arna.jcraft.common.loot.JLootTableHelper;
import net.arna.jcraft.common.network.c2s.InputSyncPacket;
import net.arna.jcraft.common.network.c2s.OnConnectedPacket;
import net.arna.jcraft.common.network.c2s.StandControlPacket;
import net.arna.jcraft.common.network.s2c.PlayerAnimPacket;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.network.s2c.ShaderActivationPacket;
import net.arna.jcraft.common.spec.JCraftSpec;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.example.GeckoLibMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.arna.jcraft.common.entity.StandEntity.stun;

//initialize your FUCKING variables, arna
//todo: add working out
public class JCraft implements ModInitializer {

    // Unchanging mod values
    public static final String MOD_ID = "jcraft";
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
    public static final String utilCD = "M3CD";
    public static final String comboBreakerCD = "CBCD";
    public static final String cooldownCancelCD = "CCCD";
    public static final String dashCD = "dCD";

    public static final List<String> cooldowns = List.of(
            standLightCD, standHeavyCD, standBarrageCD, standUltCD, standS1CD, standS2CD, standS3CD,
            utilCD, comboBreakerCD, cooldownCancelCD, dashCD,
            heavyCD, barrageCD, ultCD, s1CD, s2CD, s3CD);

    public static final ItemGroup JCRAFT_GROUP = FabricItemGroupBuilder.create(new Identifier(MOD_ID, "main"))
            .icon(() -> new ItemStack(JObjectRegistry.STANDARROW))
            .appendItems(JCraft::appendJcraftGroupStacks)
            .build();

    public static final GravityChangerConfig gravityConfig = new GravityChangerConfig(); // TODO incorporate this into our own config

    // Gamerules
    //public static final GameRules.Key<GameRules.BooleanRule> KINGCRIMSON_TELEPORT_EFFECT = GameRuleRegistry.register("kingCrimsonTeleportEffect", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanRule> COMBO_COUNTER = GameRuleRegistry.register("comboCounter", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.IntRule> CHANCE_MOB_SPAWNS_WITH_STAND = GameRuleRegistry.register("chanceMobSpawnsWithStand", GameRules.Category.MOBS, GameRuleFactory.createIntRule(5, 0, 100));
    public static final GameRules.Key<GameRules.BooleanRule> ALLOW_MOB_EVOLVED_STANDS = GameRuleRegistry.register("allowMobEvolvedStands", GameRules.Category.MOBS, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanRule> STAND_GRIEFING = GameRuleRegistry.register("standGriefing", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.IntRule> DEFAULT_SPEC = GameRuleRegistry.register("defaultSpec", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(0, 0, 2));
    //public static GameRules.Key<GameRules.IntRule> DAMAGE_MULT = GameRuleRegistry.register("jcraftDamageMult", GameRules.Category.MISC, GameRuleFactory.createIntRule(0, 0, 100));
    // Dimensional travel bullshit
    public static final List<DimValues> pastDimensions = new ArrayList<>();
    private static final List<ChunkPos> preloadedChunks = new ArrayList<>();
    private static final List<ItemEntity> itemsOfInterest = new ArrayList<>();
    public static final Map<LivingEntity, Integer> burstTimers = new HashMap<>();
    public static final List<DashData> dashes = new ArrayList<>();
    public static final int dashCooldown = 40;

    @Getter
    @Setter
    private static IClientEntityHandler clientEntityHandler = DummyClientEntityHandler.INSTANCE;

    /**
     * Starts tracking a timestop on the server.
     * Synchronizes with clients (upon timestop creation, not repeatedly)
     * Puts nearby players' items on cooldown.
     *
     * @param position in world
     */
    //todo: make TS stop animated textures
    public static void stopTime(Entity timestopper, Vec3d position, ServerWorld world, int duration) {
        // Registration
        RegistryKey<World> worldRegistryKey = world.getRegistryKey();
        JUtils.activeTimestops.add(new DimValues(timestopper, position, worldRegistryKey, duration));

        // Synchronization
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(15);
        buf.writeInt(timestopper.getId());
        buf.writeDouble(position.x);
        buf.writeDouble(position.y);
        buf.writeDouble(position.z);
        buf.writeRegistryKey(worldRegistryKey);
        buf.writeInt(duration);

        PlayerLookup.world(world).forEach(
                playerEntity -> ServerChannelFeedbackPacket.send(playerEntity, buf)
        );

        // Inventory cooldowns
        List<ServerPlayerEntity> toCooldown = world.getEntitiesByClass(ServerPlayerEntity.class,
                new Box(position.add(96.0, 96.0, 96.0), position.subtract(96.0, 96.0, 96.0)), EntityPredicates.VALID_LIVING_ENTITY);

        for (ServerPlayerEntity serverPlayerEntity : toCooldown) {
            // Shader handling
            ShaderActivationPacket.send(serverPlayerEntity, timestopper, 0, duration, ShaderActivationPacket.Type.ZA_WARUDO);
            if (serverPlayerEntity == timestopper || serverPlayerEntity.isCreative()) continue;

            // Puts all player items besides armor into cooldown for entire duration of timestop
            for (int i = 0; i < serverPlayerEntity.getInventory().main.size(); i++)
                serverPlayerEntity.getItemCooldownManager().set(serverPlayerEntity.getInventory().main.get(i).getItem(), duration);
            serverPlayerEntity.getItemCooldownManager().set(serverPlayerEntity.getOffHandStack().getItem(), duration);
        }
    }

    private static void appendJcraftGroupStacks(List<ItemStack> stacks) {
        stacks.add(new ItemStack(JObjectRegistry.STANDARROW));
        stacks.add(new ItemStack(JObjectRegistry.LIVINGARROW));
        stacks.add(new ItemStack(JObjectRegistry.REQUIEMARROW));
        stacks.add(new ItemStack(JObjectRegistry.REQUIEMRUBY));

        stacks.add(new ItemStack(JObjectRegistry.ANUBIS));
        stacks.add(new ItemStack(JObjectRegistry.ANUBISSHEATHED));
        stacks.add(new ItemStack(JObjectRegistry.KNIFE));
        stacks.add(new ItemStack(JObjectRegistry.KNIFEBUNDLE));
        stacks.add(JObjectRegistry.FVREVOLVER.getDefaultStack());
        stacks.add(JObjectRegistry.BULLET.getDefaultStack());

        stacks.add(new ItemStack(JObjectRegistry.SINNERSSOUL));
        stacks.add(new ItemStack(JObjectRegistry.SOUL_BLOCK.asItem()));
        stacks.add(new ItemStack(JObjectRegistry.GREENBABY));
        stacks.add(new ItemStack(JObjectRegistry.DIOSDIARY));

        stacks.add(new ItemStack(JObjectRegistry.BOXINGGLOVES));

        stacks.add(new ItemStack(JObjectRegistry.DIOHEADBAND));
        stacks.add(new ItemStack(JObjectRegistry.DIOJACKET));
        stacks.add(new ItemStack(JObjectRegistry.DIOPANTS));
        stacks.add(new ItemStack(JObjectRegistry.DIOBOOTS));

        stacks.add(new ItemStack(JObjectRegistry.JOTAROCAP));
        stacks.add(new ItemStack(JObjectRegistry.JOTAROJACKET));
        stacks.add(new ItemStack(JObjectRegistry.JOTAROPANTS));
        stacks.add(new ItemStack(JObjectRegistry.JOTAROBOOTS));

        stacks.add(new ItemStack(JObjectRegistry.KQCOIN));
        stacks.add(new ItemStack(JObjectRegistry.FOOLISH_SAND_BLOCK.asItem()));

        stacks.add(new ItemStack(JObjectRegistry.CINDERELLA_MASK));

        StandDiscItem.appendStacks(JCRAFT_GROUP, stacks);
    }

    // Dashes

    /**
     * Holds the data of an individual entities dash ({@link DashData#entity}, {@link DashData#dashVector}, {@link DashData#finished}, {@link DashData#duration})
     */
    public static class DashData {
        public final Vec3d dashVector;
        public final LivingEntity entity;
        public boolean finished = false;
        private int duration = 10;

        public DashData(Vec3d dashVector, LivingEntity entity) {
            this.dashVector = dashVector;
            this.entity = entity;
        }

        public void tickDash() {
            duration--;
            if (entity.hasStatusEffect(JStatusRegister.DAZED)) { // Being stunned stops dashes
                finished = true;
                return;
            }
            if (duration <= 5) { // 5 ticks of movement, then recovery
                if (duration <= 0) finished = true;
                return;
            }
            entity.setVelocity(entity.getVelocity().add(dashVector).multiply(0.5));
            entity.velocityModified = true;
        }
    }

    public static boolean isDashing(LivingEntity player) {
        for (DashData dash : dashes)
            if (player == dash.entity) return true;
        return false;
    }

    public static DashData getDash(LivingEntity player) {
        for (DashData dash : dashes)
            if (player == dash.entity) return dash;
        return null;
    }

    public static void tryDash(int forward, int side, LivingEntity entity) {
        NbtCompound data = ((IEntityDataSaver) entity).getPersistentData();
        //todo: make a JCraftUtils method for checking if the player should be effectively disabled? like when stunned or knocked down as shown here:
        if (data.getInt(dashCD) > 0 || !entity.isOnGround() || entity.hasStatusEffect(JStatusRegister.DAZED) || entity.hasStatusEffect(JStatusRegister.KNOCKDOWN))
            return;
        data.putInt(dashCD, dashCooldown);

        double dashSpeed = 0.75;
        Vec3d rotVec = entity.getRotationVector().rotateY(1.57079632679f * side); // L/R
        if (side != 0) {
            dashSpeed *= 0.75; // Sideways speed nerf
            if (forward == 1)
                rotVec = rotVec.rotateY(-0.785398163397f * side); // Forward diagonals
        }
        if (forward == -1) {
            rotVec = rotVec.rotateY(side == 0 ? 3.14159265359f : 0.785398163397f * side); // Back diagonals
            dashSpeed *= 0.75; // Backwards speed nerf
        }

        Vec3d dashDir = RotationUtil.vecPlayerToWorld( rotVec, GravityChangerAPI.getGravityDirection(entity) ); //todo: fix diagonal dashes while in custom gravity
        dashes.add(new DashData(dashDir.normalize().multiply(dashSpeed), entity));

        // Syncs dash anim (unless already attacking with a spec) with every player in the vicinity
        if (entity instanceof ServerPlayerEntity player) {
            JCraftSpec spec = JUtils.getSpec(player);

            if (spec == null || spec.moveStun < 1)
                PlayerLookup.around((ServerWorld) entity.getWorld(), entity.getPos(), 96).forEach( //todo: find a less arbitrary number for radius here
                        serverPlayer -> PlayerAnimPacket.send(player, serverPlayer, "dash"));
        }
    }

    public static void clearPreloadedChunks(ServerWorld auWorld) {
        if (preloadedChunks.isEmpty()) {
            return;
        }
        for (ChunkPos p : preloadedChunks)
            auWorld.setChunkForced(p.x, p.z, false);
        preloadedChunks.clear();
    }

    public static void preloadChunk(ServerWorld auWorld, int chunkX, int chunkY) {
        preloadedChunks.add(new ChunkPos(chunkX, chunkY));
        auWorld.setChunkForced(chunkX, chunkY, true);
    }

    public static StandEntity summon(World world, LivingEntity player) {
        if (player.hasStatusEffect(JStatusRegister.STANDLESS)) return null;

        NbtCompound data = ((IEntityDataSaver) player).getPersistentData();
        StandType type = StandType.fromId(data.getInt("StandID"));
        StandEntity stand = type == null ? null : type.createNew(world);

        if (stand == null) return null;

        int skin = data.contains("StandSkin", NbtElement.INT_TYPE) ? data.getInt("StandSkin") : 0;
        stand.setSkin(skin);
        stand.setPosition(player.getPos().subtract(player.getRotationVector()));
        stand.startRiding(player);
        stand.setUser(player);
        world.spawnEntity(stand);
        return stand;
    }

    public static Identifier id(String name) {
        return new Identifier(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        MidnightConfig.init(MOD_ID, JConfig.class);
        GravityChannel.init();

        // Particle registration (serverside)
        JParticleTypeRegistry.initParticleTypes();

        // Geckolib
        GeckoLibMod.DISABLE_IN_DEV = true;

        // Registration
        JObjectRegistry.init();
        JBlockEntityTypeRegistry.init();
        JCommandRegister.registerCommands();
        JEventsRegister.registerEvents();
        JStatusRegister.registerStatuses();
        JSoundRegister.registerSounds();
        JEntityTypeRegister.registerEntities();
        JDimensionRegister.registerDimensions();
        JArgumentTypeRegistry.registerArgumentTypes();
        JEnchantmentRegistry.init();
        JLootTableHelper.init();

        ServerPlayNetworking.registerGlobalReceiver(StandControlPacket.ID, StandControlPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(InputSyncPacket.ID, InputSyncPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(OnConnectedPacket.ID, OnConnectedPacket::handle);
    }


    public static void createParticle(ServerWorld world, double x, double y, double z, int id) {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeShort(8);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeInt(id); // Combo breaker particle ID

        PlayerLookup.around(world, new Vec3d(x, y, z), 128).forEach(
                serverPlayer -> ServerChannelFeedbackPacket.send(serverPlayer, buf)
        );
    }

    public static final List<String> unresettableCooldowns = List.of(standBarrageCD, standUltCD, barrageCD, ultCD, comboBreakerCD, cooldownCancelCD, dashCD);

    /**
     * Resets specific cooldowns, or all cooldowns if player is in creative.
     */
    public static void cooldownCancel(ServerWorld world, LivingEntity player) {
        if (player.isSpectator()) return;

        NbtCompound data = ((IEntityDataSaver) player).getPersistentData();

        if (data.getInt(cooldownCancelCD) <= 0) {
            for (String cooldownType : cooldowns) {
                if (unresettableCooldowns.contains(cooldownType)) {
                    continue;
                }
                data.putInt(cooldownType, 0);
            }

            data.putInt(cooldownCancelCD, 900); // 45s

            Vec3d pPos = player.getEyePos();
            world.playSoundFromEntity(null, player, JSoundRegister.COOLDOWN_CANCEL, SoundCategory.PLAYERS, 1, 1);
            createParticle(world, pPos.x, pPos.y, pPos.z, 1);
        }
    }

    /**
     * Breaks out of a combo using a slightly delayed attack centered at the player.
     * This attack is blockable, launches and stuns on hit.
     */
    public static void comboBreak(ServerWorld world, LivingEntity player, StatusEffectInstance stun) {
        if (player.isSpectator()) return;
        NbtCompound data = ((IEntityDataSaver) player).getPersistentData();

        if (stun.getDuration() > 1 && stun.getAmplifier() == 1 && data.getInt(comboBreakerCD) <= 0) {
            data.putInt(comboBreakerCD, 1200); // 60s

            stun(player, 5, 2); // Player is slowed down considerably pre-burst

            world.playSoundFromEntity(null, player, JSoundRegister.COMBO_BREAK, SoundCategory.PLAYERS, 1, 1);

            Vec3d pPos = player.getEyePos();
            burstTimers.put(player, 4);
            createParticle(world, pPos.x, pPos.y, pPos.z, 0);
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

    public static void dimensionHop(Entity entity, int heightOffset) {
        ServerWorld original = (ServerWorld) entity.getWorld();
        MinecraftServer server = original.getServer();
        ServerWorld au = server.getWorld(JDimensionRegister.AU_DIMENSION_KEY);
        if (au == null) {
            JCraft.LOGGER.fatal("Alternate universe world does not exist!");
            return;
        }
        if (original == au)
            return;

        Vec3d pos = entity.getPos();
        Entity finalEnt = entity;

        if (entity instanceof ServerPlayerEntity player) {
            player.teleport(au, pos.x, pos.y - heightOffset, pos.z, entity.getYaw(), entity.getPitch());
            player.networkHandler.sendPacket(
                    new PlaySoundS2CPacket(JSoundRegister.D4C_ALT_UNIVERSE_AMBIENCE, SoundCategory.MUSIC, pos.x, pos.y - heightOffset, pos.z, 1.0F, 1.0F, 0)
            );
        } else
            finalEnt = teleportToWorld(entity, au, entity.getX(), entity.getY() - heightOffset, entity.getZ());

        pastDimensions.add(new DimValues(finalEnt, pos, original.getRegistryKey()));
    }
}
