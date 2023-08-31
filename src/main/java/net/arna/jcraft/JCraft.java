package net.arna.jcraft;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import lombok.Getter;
import lombok.Setter;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.config.GravityChangerConfig;
import net.arna.jcraft.common.gravity.util.GravityChannel;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.item.StandDiscItem;
import net.arna.jcraft.common.loot.JLootTableHelper;
import net.arna.jcraft.common.network.c2s.*;
import net.arna.jcraft.common.network.s2c.*;
import net.arna.jcraft.common.spec.JSpec;
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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.example.GeckoLibMod;

import java.util.*;

import static net.arna.jcraft.common.entity.stand.StandEntity.stun;

//initialize your FUCKING variables, arna
//todo: add working out
public class JCraft implements ModInitializer {

    // Unchanging mod values
    public static final String MOD_ID = "jcraft";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static void prefixedLog(boolean isClient, String s) {
        LOGGER.info(isClient ? "CLIENT: " : "SERVER: " + s);
    }
    public static final int SPEC_QUEUE_MOVESTUN_LIMIT = 11; // exclusive, 10 -> 0.5s window for queueing moves
    public static final int QUEUE_MOVESTUN_LIMIT = 7; // exclusive, 6 -> 0.3s window for queueing moves

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

    public static final Object2IntMap<LivingEntity> burstTimers = new Object2IntRBTreeMap<>();

    public static final List<DashData> dashes = new ArrayList<>();

    @Getter
    private static final Map<Entity, EntityInterest> entitiesOfInterest = new HashMap<>();

    // Standardized cooldowns
    public static final int dashCooldown = 40;

    public static final int LIGHT_COOLDOWN = 30;
    public static final double lightCooldown = 1.5;

    @Getter
    @Setter
    private static IClientEntityHandler clientEntityHandler = DummyClientEntityHandler.INSTANCE;

    public static void markItemOfInterest(@NotNull Entity entity, @NotNull EntityInterest interest) {
        entitiesOfInterest.put(entity, interest);
    }

    /**
     * Starts tracking a timestop on the server.
     * Synchronizes with clients (upon timestop creation, not repeatedly)
     * Puts nearby players' items on cooldown.
     *
     * @param position in world
     */
    //todo: make TS stop animated textures
    public static void beginTimestop(LivingEntity timestopper, Vec3d position, ServerWorld world, int duration) {
        // Registration
        RegistryKey<World> worldRegistryKey = world.getRegistryKey();
        JUtils.activeTimestops.add(new DimValues(timestopper, position, worldRegistryKey, duration));

        // Synchronization
        PacketByteBuf buf = TimeStopStatePacket.createStartPacket(timestopper.getId(), position, worldRegistryKey, duration);
        PlayerLookup.world(world).forEach( playerEntity -> TimeStopStatePacket.send(playerEntity, buf) ); // Sends to unaffected players because they may walk into range

        List<ServerPlayerEntity> toStop = world.getEntitiesByClass(ServerPlayerEntity.class,
                new Box(position.add(96.0, 96.0, 96.0), position.subtract(96.0, 96.0, 96.0)), EntityPredicates.VALID_LIVING_ENTITY);

        for (ServerPlayerEntity serverPlayer : toStop) {
            // Shader handling
            ShaderActivationPacket.send(serverPlayer, timestopper, 0, duration, ShaderActivationPacket.Type.ZA_WARUDO);
            if (serverPlayer == timestopper || serverPlayer.isCreative()) continue;

            // Puts all player items besides armor into cooldown for entire duration of timestop
            for (int i = 0; i < serverPlayer.getInventory().main.size(); i++)
                serverPlayer.getItemCooldownManager().set(serverPlayer.getInventory().main.get(i).getItem(), duration);
            serverPlayer.getItemCooldownManager().set(serverPlayer.getOffHandStack().getItem(), duration);
        }
    }

    public static void stopTimestop(Entity timestopper) {
        DimValues timestop = JUtils.getTimestop(timestopper);
        World world = timestopper.getWorld();

        if (timestop == null || !(world instanceof ServerWorld serverWorld)) return;

        // Synchronization
        PacketByteBuf buf = TimeStopStatePacket.createStopPacket(timestopper.getId());
        PlayerLookup.world(serverWorld).forEach( playerEntity -> TimeStopStatePacket.send(playerEntity, buf) );

        Vec3d position = timestop.pos;

        List<ServerPlayerEntity> toUnfreeze = serverWorld.getEntitiesByClass(ServerPlayerEntity.class,
                new Box(position.add(96.0, 96.0, 96.0), position.subtract(96.0, 96.0, 96.0)), EntityPredicates.VALID_LIVING_ENTITY);

        for (ServerPlayerEntity serverPlayer : toUnfreeze) {
            // Shader handling
            ShaderDeactivationPacket.send(serverPlayer, ShaderActivationPacket.Type.ZA_WARUDO);

            // Removes cooldowns
            for (int i = 0; i < serverPlayer.getInventory().main.size(); i++)
                serverPlayer.getItemCooldownManager().remove(serverPlayer.getInventory().main.get(i).getItem());
            serverPlayer.getItemCooldownManager().remove(serverPlayer.getOffHandStack().getItem());
        }

        JUtils.activeTimestops.remove(timestop);
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
        stacks.add(JObjectRegistry.FV_REVOLVER.getDefaultStack());
        stacks.add(JObjectRegistry.BULLET.getDefaultStack());

        stacks.add(new ItemStack(JObjectRegistry.SINNERSSOUL));
        stacks.add(new ItemStack(JObjectRegistry.SOUL_BLOCK.asItem()));
        stacks.add(new ItemStack(JObjectRegistry.METEORITE_BLOCK.asItem()));
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

        stacks.add(new ItemStack(JObjectRegistry.KQ_COIN));
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
            if (entity.hasStatusEffect(JStatusRegistry.DAZED)) { // Being stunned stops dashes
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
        CooldownsComponent cooldowns = JComponents.COOLDOWNS.get(entity);
        //todo: make a JCraftUtils method for checking if the player should be effectively disabled? like when stunned or knocked down as shown here:
        if (cooldowns.getCooldown(CooldownType.DASH) > 0 || !entity.isOnGround() || entity.hasStatusEffect(JStatusRegistry.DAZED) || entity.hasStatusEffect(JStatusRegistry.KNOCKDOWN))
            return;
        cooldowns.setCooldown(CooldownType.DASH, dashCooldown);

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
            JSpec spec = JUtils.getSpec(player);

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

    public static void preloadChunk(ServerWorld auWorld, int chunkX, int chunkZ) {
        // Already loaded, no need to do so again.
        if (auWorld.getForcedChunks().contains(new ChunkPos(chunkX, chunkZ).toLong())) return;

        preloadedChunks.add(new ChunkPos(chunkX, chunkZ));
        auWorld.setChunkForced(chunkX, chunkZ, true);
    }

    public static StandEntity<?, ?> summon(World world, LivingEntity user) {
        if (user.hasStatusEffect(JStatusRegistry.STANDLESS)) return null;

        StandComponent standData = JComponents.STAND.get(user);
        StandType type = standData.getType();
        StandEntity<?, ?> stand = type == null ? null : type.createNew(world);

        if (stand == null) return null;

        int skin = standData.getSkin();
        stand.setSkin(skin);
        stand.setPosition(user.getPos().subtract(user.getRotationVector()));
        stand.startRiding(user);
        stand.setUser(user);

        if (user instanceof ServerPlayerEntity player && StandBlockPacket.isBlocking(player))
            stand.blocking = true;

        world.spawnEntity(stand);

        standData.setStand(stand);
        return stand;
    }

    public static Identifier id(String name) {
        return new Identifier(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        GravityChannel.init();

        // Particle registration (serverside)
        JParticleTypeRegistry.initParticleTypes();

        // Geckolib
        GeckoLibMod.DISABLE_IN_DEV = true;

        // Registration
        JObjectRegistry.init();
        JBlockEntityTypeRegistry.init();
        JCommandRegistry.registerCommands();
        JEventsRegistry.registerEvents();
        JStatusRegistry.registerStatuses();
        JSoundRegistry.registerSounds();
        JEntityTypeRegistry.registerEntities();
        JDimensionRegistry.registerDimensions();
        JArgumentTypeRegistry.registerArgumentTypes();
        JEnchantmentRegistry.init();
        JLootTableHelper.init();

        ServerPlayNetworking.registerGlobalReceiver(JPacketRegistry.C2S_PLAYER_INPUT, PlayerInputPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(OnConnectedPacket.ID, OnConnectedPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(ConfigUpdatePacket.ID, ConfigUpdatePacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(JPacketRegistry.C2S_STAND_BLOCK, StandBlockPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(JPacketRegistry.C2S_COOLDOWN_CANCEL, CooldownCancelPacket::handle);
    }


    public static void createParticle(ServerWorld world, double x, double y, double z, JParticleType type) {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeShort(8);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeEnumConstant(type);

        PlayerLookup.around(world, new Vec3d(x, y, z), 128).forEach(
                serverPlayer -> ServerChannelFeedbackPacket.send(serverPlayer, buf)
        );
    }

    /**
     * Breaks out of a combo using a slightly delayed attack centered at the player.
     * This attack is blockable, launches and stuns on hit.
     */
    public static void comboBreak(ServerWorld world, LivingEntity player, StatusEffectInstance stun) {
        if (player.isSpectator()) return;
        CooldownsComponent cooldowns = JComponents.getCooldowns(player);

        if (stun.getDuration() > 1 && stun.getAmplifier() == 1 && cooldowns.getCooldown(CooldownType.COMBO_BREAKER) <= 0) {
            cooldowns.startCooldown(CooldownType.COMBO_BREAKER);

            stun(player, 5, 2); // Player is slowed down considerably pre-burst

            world.playSoundFromEntity(null, player, JSoundRegistry.COMBO_BREAK, SoundCategory.PLAYERS, 1, 1);

            Vec3d pPos = player.getEyePos();
            burstTimers.put(player, 4);
            createParticle(world, pPos.x, pPos.y, pPos.z, JParticleType.COMBO_BREAK);
        }
    }

    public static @Nullable <T extends Entity> T teleportToWorld(T e, ServerWorld w, double x, double y, double z) {
        if (!e.isRemoved()) {
            e.detach();
            T entity = (T) e.getType().create(w);
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

    public static void dimensionHop(LivingEntity entity, int heightOffset) {
        ServerWorld original = (ServerWorld) entity.getWorld();
        MinecraftServer server = original.getServer();
        ServerWorld au = server.getWorld(JDimensionRegistry.AU_DIMENSION_KEY);
        if (au == null) {
            JCraft.LOGGER.fatal("Alternate universe world does not exist!");
            return;
        }
        if (original == au) return;

        Vec3d pos = entity.getPos();
        LivingEntity finalEnt = entity;

        if (entity instanceof ServerPlayerEntity player) {
            player.teleport(au, pos.x, pos.y - heightOffset, pos.z, entity.getYaw(), entity.getPitch());
            player.networkHandler.sendPacket(
                    new PlaySoundS2CPacket(JSoundRegistry.D4C_ALT_UNIVERSE_AMBIENCE, SoundCategory.MUSIC, pos.x, pos.y - heightOffset, pos.z, 1.0F, 1.0F, 0)
            );
        } else finalEnt = teleportToWorld(entity, au, entity.getX(), entity.getY() - heightOffset, entity.getZ());

        if (finalEnt == null) {
            JCraft.LOGGER.error("Failed to teleport " + entity + " to alternate universe!");
            return;
        }

        finalEnt.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 9, true, false, true));
        pastDimensions.add(new DimValues(finalEnt, pos, original.getRegistryKey()));
    }
}
