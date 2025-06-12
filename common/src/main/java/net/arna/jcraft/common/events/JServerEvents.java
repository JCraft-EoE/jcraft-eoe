package net.arna.jcraft.common.events;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.*;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.block.CoffinBlock;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.component.living.CommonVampireComponent;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.data.AttackerDataLoader;
import net.arna.jcraft.common.entity.StandMeteorEntity;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.item.MockItem;
import net.arna.jcraft.common.network.s2c.AttackerDataPacket;
import net.arna.jcraft.common.saveddata.ExclusiveStandsData;
import net.arna.jcraft.common.tickable.*;
import net.arna.jcraft.common.util.*;
import net.arna.jcraft.mixin_logic.EntityAddon;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.arna.jcraft.JCraft.*;
import static net.arna.jcraft.common.util.EntityInterest.blockAttractionInterest;
import static net.arna.jcraft.common.util.EntityInterest.itemAttractionInterest;

public class JServerEvents {
    private static final List<Enchantment> JCRAFT_ARMOR_ENCHANTS = List.of(
            Enchantments.ALL_DAMAGE_PROTECTION, Enchantments.PROJECTILE_PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.UNBREAKING);

    private static final List<List<Item>> EQUIPMENT = List.of(
            List.of(Items.AIR, Items.GOLDEN_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS),
            List.of(Items.AIR, Items.GOLDEN_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS),
            List.of(Items.AIR, Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE),
            List.of(Items.AIR, Items.GOLDEN_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET)
    );

    public static void finishLoading(final MinecraftServer server) {
        auWorld = server.getLevel(JDimensionRegistry.AU_DIMENSION_KEY);
        tutorialWorld = server.getLevel(JDimensionRegistry.TUTORIAL_DIMENSION_KEY);
        setExclusiveStandsData(ExclusiveStandsData.fromDefaultFile(server));
    }

    public static void saveExclusives(final MinecraftServer server) {
        getExclusiveStandsData().saveToDefaultFile(server);
    }

    private static final int PREDICTION_RADIUS = 6 * 16;
    private static final int MAX_COMPENSATION_MS = 250; // Game is barely playable at this point
    private static final double MS_TO_TICKS = 1000.0 / 20.0; // 1000ms = 1s, 1s = 20t

    public static void serverPostTick(MinecraftServer server) {
        if (JCraft.preloadLockTicks > 0) {
            JCraft.preloadLockTicks--;
        }

        RevolverFire.tick(server);
        PastDimensions.tick(server);
        Timestops.tick(server);
        Revivables.tick(server);
        JEnemies.tick(server);
        FrameDataRequests.tick();
        MagneticFields.tick();
        RazorCoughs.tick();

        // Player logic (cooldown handling and DamageTimer counting)
        for (ServerPlayer player : JUtils.all(server)) {
            if (player == null || !player.isAlive()) {
                continue;
            }

            if (player.getLastHurtByMob() != null) {
                JComponentPlatformUtils.getMiscData(player).startDamageTimer();
            }
        }

        // Burst handling
        Object2IntMap<LivingEntity> newBurstTimers = new Object2IntOpenHashMap<>();

        for (Object2IntMap.Entry<LivingEntity> burst : JCraft.burstTimers.object2IntEntrySet()) {
            LivingEntity player = burst.getKey();
            burst.setValue(burst.getIntValue() - 1);
            int newVal = burst.getIntValue();

            Set<Entity> filter = new HashSet<>();
            filter.add(player);
            if (player.isVehicle()) {
                filter.addAll(player.getPassengers());
            }

            if (newVal > 0) {
                newBurstTimers.put(player, newVal);
                continue;
            }

            player.removeEffect(JStatusRegistry.DAZED.get());
            stun(player, 10, 1);

            Vec3 pPos = player.getEyePosition();

            AABB burstHitbox = AbstractSimpleAttack.createBox(pPos, 4);
            List<? extends Entity> toPush = player.level().getEntitiesOfClass(Entity.class, burstHitbox,
                    EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(e -> !filter.contains(e)));
            JUtils.displayHitbox(player.level(), burstHitbox);

            for (Entity ent : toPush) {
                Vec3 awayVector = ent.position().subtract(pPos).normalize();
                boolean pushAway = true;

                // If the stand was hit, the attack will stop and the user will be hit remotely
                if (ent instanceof StandEntity<?, ?> stand) {
                    if (stand.hasUser()) {
                        stun(stand.getUser(), 10, 3, player);
                        stand.cancelMove();
                    }
                } else if (ent.getFirstPassenger() instanceof StandEntity<?, ?> stand) { // Stands should not have passengers
                    if (stand.blocking) {
                        pushAway = false;
                    } else if (ent instanceof LivingEntity living) { // Stand users that aren't blocking get launched and their stand attacks are cancelled
                        //awayVector = awayVector.multiply(0.5);
                        stun(living, 10, 3, player);
                        stand.cancelMove();
                    }
                }

                if (!pushAway) {
                    continue;
                }
                JUtils.setVelocity(ent, awayVector.x, awayVector.y / 5 + 0.4, awayVector.z);
            }
        }

        JCraft.burstTimers.clear();
        JCraft.burstTimers.putAll(newBurstTimers);

        // Dash handling
        for (Map.Entry<LivingEntity, DashData> entry : new HashSet<>(JCraft.dashes.entrySet())) {
            DashData dash = entry.getValue();
            dash.tickDash();

            if (dash.finished) {
                JCraft.dashes.remove(entry.getKey());
            }
        }

        // Handle items of interest
        Map<Entity, EntityInterest> entitiesOfInterest = JCraft.getEntitiesOfInterest();
        HashMap<Entity, EntityInterest> newItemsOfInterest = new HashMap<>();

        for (Map.Entry<Entity, EntityInterest> entityAndInterest : entitiesOfInterest.entrySet()) {
            Entity entity = entityAndInterest.getKey();
            if (entity == null || !entity.isAlive()) {
                continue;
            }
            EntityInterest interest = entityAndInterest.getValue();
            ServerLevel serverWorld = (ServerLevel) entity.level();
            boolean saveForNextIteration = true;

            switch (interest.getType()) {
                default -> saveForNextIteration = false;
                case BLOCK_ATTRACTION -> {
                    BlockPos attractionBlockPos = interest.getAttractionBlockPos();
                    if (entity.distanceToSqr(attractionBlockPos.getX(), attractionBlockPos.getY(), attractionBlockPos.getZ()) < 4) {
                        boolean griefing = serverWorld.getGameRules().getBoolean(JCraft.STAND_GRIEFING);
                        dimensionalExplosion(serverWorld, griefing, entity);
                        if (griefing) {
                            serverWorld.setBlockAndUpdate(attractionBlockPos, Blocks.AIR.defaultBlockState());
                        }
                    } else {
                        BlockPos delta = attractionBlockPos.subtract(entity.blockPosition());
                        Vec3 towardsVel = new Vec3(delta.getX(), delta.getY(), delta.getZ()).normalize();
                        entity.push(towardsVel.x, towardsVel.y, towardsVel.z);
                        entity.hurtMarked = true;
                    }
                }
                case ITEM_ATTRACTION -> {
                    if (!(entity instanceof ItemEntity item)) {
                        continue;
                    }
                    for (Map.Entry<Entity, EntityInterest> entityAndInterest2 : entitiesOfInterest.entrySet()) {
                        Entity entity2 = entityAndInterest2.getKey();
                        if (entity2 instanceof ItemEntity item2) {
                            if (
                                    entityAndInterest2.getValue().getType() == EntityInterest.ItemInterestType.ITEM_ATTRACTION &&
                                            item2 != entity &&
                                            item2.level() == serverWorld &&
                                            item2.getItem().getItem() == item.getItem().getItem() &&
                                            item2.distanceToSqr(entity) <= 256
                            ) {
                                Vec3 converge = item2.position().subtract(entity.position());
                                Vec3 towardsVector = converge.normalize().scale(0.25);
                                entity.push(towardsVector.x, towardsVector.y, towardsVector.z);
                                entity.hurtMarked = true;

                                if (item2.distanceTo(entity) <= 1.0) {
                                    dimensionalExplosion(serverWorld, serverWorld.getGameRules().getBoolean(JCraft.STAND_GRIEFING), entity, item2);
                                    saveForNextIteration = false;
                                }
                            }
                        }
                    }
                }
            }

            if (saveForNextIteration) {
                newItemsOfInterest.put(entity, interest);
            }
        }

        entitiesOfInterest.clear();
        entitiesOfInterest.putAll(newItemsOfInterest);

        if (AttackerDataLoader.isDirty()) {
            AttackerDataPacket.send(server.getPlayerList().getPlayers());
            AttackerDataLoader.setDirty(false);
        }
    }

    private static void dimensionalExplosion(ServerLevel serverWorld, boolean griefing, Entity one) {
        dimensionalExplosion(serverWorld, griefing, one, null);
    }

    private static void dimensionalExplosion(ServerLevel serverWorld, boolean griefing, Entity one, @Nullable Entity other) {
        Vec3 midPos = one.position();
        if (other != null) {
            midPos = midPos.add(other.position()).scale(0.5);
            other.discard();
        }

        one.discard();

        Explosion explosion = serverWorld.explode(null, midPos.x, midPos.y, midPos.z, 1f,
                griefing ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);

        List<LivingEntity> toDamage = serverWorld.getEntitiesOfClass(LivingEntity.class,
                new AABB(midPos.add(1.5, 1.5, 1.5), midPos.subtract(1.5, 1.5, 1.5)),
                EntitySelector.ENTITY_STILL_ALIVE);

        for (LivingEntity ent : toDamage) {
            ent.hurt(explosion.getDamageSource(), 7);
            JCraft.stun(ent, 10, 3);
            ent.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), 35, 0));
        }
    }

    public static EventResult entityLoad(Entity entity, boolean worldGenSpawned) {
        ServerLevel world = (ServerLevel) entity.level();

        // If an item was spawned
        if (entity instanceof ItemEntity item) {
            ItemStack stack = item.getItem();

            if (stack.is(JItemRegistry.ANUBIS.get())) {
                item.setPickUpDelay(0);
                return EventResult.pass();
            }

            if (stack.is(JItemRegistry.FV_REVOLVER.get())) {
                JCraft.markItemOfInterest(item, EntityInterest.itemAttractionInterest(JItemRegistry.FV_REVOLVER.get()));
                return EventResult.pass();
            }

            // ... in the AU
            if (world.dimension().equals(JDimensionRegistry.AU_DIMENSION_KEY)) {
                if (item.getOwner() != null || MockItem.isMockItem(stack)) {
                    return EventResult.pass();
                }

                ItemStack mockStack = MockItem.createMockStack(stack); // Convert it to a mock item (incompatible and useless)
                if (stack.getItem() instanceof BlockItem) // ... and mark down all relevant data
                {
                    mockStack.getOrCreateTag().putIntArray("AttractPos", new int[]{item.getBlockX(), item.getBlockY(), item.getBlockZ()});
                }
                item.setItem(mockStack);
            } else { // ... outside the AU
                if (MockItem.isMockItem(stack)) {
                    // Mark it as an item of interest, and save relevant data
                    CompoundTag stackData = stack.getOrCreateTag();
                    if (stackData.contains("AttractPos")) { // if attracted to a specific position
                        String itemId = stackData.getString("MockItem");
                        int[] attractPos = stackData.getIntArray("AttractPos");
                        BlockPos attractBlockPos = new BlockPos(attractPos[0], attractPos[1], attractPos[2]);
                        if ( // ... if the world has the specified block item
                                BuiltInRegistries.ITEM.getKey(
                                        world.getBlockState(attractBlockPos).getBlock().asItem()
                                ).toString().equals(itemId)
                        ) {
                            JCraft.markItemOfInterest(item, blockAttractionInterest(attractBlockPos));
                        }
                    } else { // if not attracted to a specific position, it's a general item to attract
                        JCraft.markItemOfInterest(item, itemAttractionInterest(stack.getItem()));
                    }
                }
            }
        }

        if (entity instanceof Mob mob) {
            // Mark stand user mobs
            final CommonStandComponent standData = JComponentPlatformUtils.getStandComponent(mob);
            if (!StandTypeUtil.isNone(standData.getType())) {
                JEnemies.add(mob);
                return EventResult.pass();
            }

            // Create new stand user mobs
            if (standData.isTagged()) {
                return EventResult.pass();
            }
            if (standData.getType() != null) {
                return EventResult.pass();
            }

            if (!mob.getType().is(JTagRegistry.CAN_HAVE_STAND)) {
                return EventResult.pass();
            }
            Random random = new Random();
            GameRules gameRules = world.getGameRules();

            standData.setTagged(true);
            // STAND
            if (!JServerConfig.SPAWNER_STANDS.getValue() && ((EntityAddon) mob).jcraft$isFromSpawner()) {
                return EventResult.pass();
            }

            if (100 - random.nextInt(0, 100) > gameRules.getInt(CHANCE_MOB_SPAWNS_WITH_STAND)) {
                return EventResult.pass();
            }
            final StandType type = gameRules.getBoolean(ALLOW_MOB_EVOLVED_STANDS)
                    ? StandTypeUtil.getRandom(entity.level().random)
                    : StandTypeUtil.getRandomRegular(entity.level().random);
            standData.setType(type);

            // ATTRIBUTES
            AttributeInstance followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
            if (followRange != null) {
                followRange.setBaseValue(Mth.clamp(
                        128 * entity.level().getCurrentDifficultyAt(entity.blockPosition()).getEffectiveDifficulty() / 6.75,
                        10d, 128d));
            }
            AttributeInstance movementSpeed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movementSpeed != null && movementSpeed.getBaseValue() < 0.3) {
                movementSpeed.setBaseValue(0.3);
            }

            // EQUIPMENT
            if (mob.getMaxHealth() > 100.0) {
                return EventResult.pass();
            }

            NonNullList<ItemStack> handItems = (NonNullList<ItemStack>) mob.getHandSlots(), armorItems = (NonNullList<ItemStack>) mob.getArmorSlots();
            // Silver chariot users may spawn with Anubis (25% chance)
            if (type == JStandTypeRegistry.SILVER_CHARIOT.get() && random.nextInt(5) == 4) {
                handItems.set(0, new ItemStack(JItemRegistry.ANUBIS.get()));
            }

            if (random.nextInt(0, 100) >= 90) {
                handItems.set(1, new ItemStack(JItemRegistry.STAND_ARROW.get()));
                mob.setDropChance(EquipmentSlot.OFFHAND, 100f);
            }

            Enchantment enchantment;
            ItemStack itemStack;
            int baseArmorLevel = random.nextInt(1, 6);
            int enchantsSize = JCRAFT_ARMOR_ENCHANTS.size();
            for (int i = 0; i < 4; i++) {
                itemStack = new ItemStack(EQUIPMENT.get(i).get(baseArmorLevel + random.nextInt(-1, 1)));
                enchantment = JCRAFT_ARMOR_ENCHANTS.get(random.nextInt(enchantsSize));
                itemStack.enchant(enchantment, enchantment.getMaxLevel());
                armorItems.set(i, itemStack);
                if (itemStack.is(Items.NETHERITE_HELMET) || itemStack.is(Items.DIAMOND_HELMET)) {
                    mob.setDropChance(EquipmentSlot.HEAD, 0f);
                }
                else if (itemStack.is(Items.NETHERITE_CHESTPLATE) || itemStack.is(Items.DIAMOND_CHESTPLATE)) {
                    mob.setDropChance(EquipmentSlot.CHEST, 0f);
                }
                else if (itemStack.is(Items.NETHERITE_LEGGINGS) || itemStack.is(Items.DIAMOND_LEGGINGS)) {
                    mob.setDropChance(EquipmentSlot.LEGS, 0f);
                }
                else if (itemStack.is(Items.NETHERITE_BOOTS) || itemStack.is(Items.DIAMOND_BOOTS)) {
                    mob.setDropChance(EquipmentSlot.FEET, 0f);
                }
            }

            JEnemies.add(mob);
        }
        return EventResult.pass();
    }

    public static EventResult rightClickBlock(Player player, InteractionHand hand, BlockPos blockPos, Direction direction) {
        if (!JUtils.canAct(player)) {
            return EventResult.interruptFalse();
        }

        // Remote players do stuff with their stand, not themselves
        StandEntity<?, ?> stand = JUtils.getStand(player);
        if (stand != null && stand.isRemoteAndControllable()) {
            return EventResult.interruptFalse();
        }

        return EventResult.pass();
    }

    public static EventResult leftClickBlock(Player player, InteractionHand hand, BlockPos blockPos, Direction direction) {
        if (!JUtils.canAct(player)) {
            return EventResult.interruptFalse();
        }

        // Remote players do stuff with their stand, not themselves
        StandEntity<?, ?> stand = JUtils.getStand(player);
        if (stand != null && stand.isRemoteAndControllable()) {
            return EventResult.interruptFalse();
        }

        return EventResult.pass();
    }

    public static CompoundEventResult<ItemStack> rightClick(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!JUtils.canAct(player)) {
            return CompoundEventResult.interruptFalse(stack);
        }
        return CompoundEventResult.pass();
    }

    public static EventResult death(LivingEntity living, DamageSource source) {
        if (living.level() instanceof ServerLevel serverWorld) {
            if (living instanceof ServerPlayer serverPlayer) {
                GameRules gameRules = serverWorld.getGameRules();
                if (!gameRules.getBoolean(JCraft.KEEP_STAND)) {
                    JComponentPlatformUtils.getStandComponent(living).setTypeAndSkin(JStandTypeRegistry.NONE.get(), 0);
                }
                if (!gameRules.getBoolean(JCraft.KEEP_SPEC)) {
                    JComponentPlatformUtils.getSpecData(serverPlayer)
                            .setType(JSpecTypeRegistry.NONE.get());
                }

                if (source.getEntity() instanceof LivingEntity killer) {
                    JComponentPlatformUtils.getCooldowns(killer).clear(CooldownType.COMBO_BREAKER);

                    boolean killVampirism = JServerConfig.KILL_VAMPIRISM.getValue();
                    if (killer instanceof ServerPlayer killerPlayer) {
                        if (killVampirism) {
                            killerPlayer.getFoodData().eat(20, 20f);
                            CommonVampireComponent vampireComponent = JComponentPlatformUtils.getVampirism(killerPlayer);
                            if (vampireComponent.isVampire()) {
                                vampireComponent.setBlood(20.0f);
                            }
                        }
                    }
                    if (killVampirism) {
                        killer.setHealth(killer.getMaxHealth());
                    }
                }
            }

            Revivables.addRevivable(living.getType(), living.position(), serverWorld.dimension());
        }
        return EventResult.pass();
    }

    public static EventResult hurt(LivingEntity entity, DamageSource source, float damage) {
        // No snowball shenanigans
        if (damage < 0.01f) return EventResult.pass();
        if (entity.level() instanceof ServerLevel serverWorld) {
            boolean toLaunch = false;
            Entity attacker = source.getEntity();
            MobEffectInstance stun = entity.getEffect(JStatusRegistry.DAZED.get());

            if (stun != null && stun.getAmplifier() != 2) {
                boolean projectileAttack = false;
                // Only apply stun nerfs if hit with a weapon or a projectile
                if (attacker instanceof LivingEntity living) {
                    projectileAttack = source.is(DamageTypeTags.IS_PROJECTILE);
                    boolean hasWeapon = projectileAttack;
                    if (!hasWeapon) {
                        hasWeapon = !living.getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty();
                    }
                    toLaunch = hasWeapon;
                }

                if (source.is(DamageTypes.EXPLOSION)) {
                    toLaunch = true;
                }

                if (toLaunch) {
                    int duration = stun.getDuration() / 3;

                    entity.removeEffect(JStatusRegistry.DAZED.get());
                    JCraft.stun(entity, duration, 3, attacker);

                    Vec3i upVec = GravityChangerAPI.getGravityDirection(entity).getNormal();
                    Vec3 upVecD = new Vec3(-upVec.getX() / 3.0, -upVec.getY() / 3.0, -upVec.getZ() / 3.0);

                    Vec3 sourcePos = source.getSourcePosition();
                    if (sourcePos == null) { // RNG Launch upwards
                        sourcePos = new Vec3(
                                entity.getRandom().nextGaussian(),
                                entity.getRandom().nextGaussian(),
                                entity.getRandom().nextGaussian())
                                .add(entity.position())
                                .subtract(upVecD);
                    }

                    Vec3 knockback = entity.position().subtract(sourcePos).normalize().add(upVecD);
                    GravityChangerAPI.setWorldVelocity(entity, knockback);
                    entity.hurtMarked = true;

                    JCraft.createParticle(serverWorld,
                            entity.getX() - upVec.getX(),
                            entity.getY() - upVec.getY(),
                            entity.getZ() - upVec.getZ(),
                            projectileAttack ? JParticleType.STUN_PIERCE : JParticleType.STUN_SLASH);
                }
            }
        }
        return EventResult.pass();
    }

    public static InteractionResult allowSleep(Player player, BlockPos sleepingPos) {
        if (player.level() instanceof ServerLevel serverWorld) {
            if (serverWorld.getBlockState(sleepingPos).is(JBlockRegistry.COFFIN_BLOCK.get())) {
                return serverWorld.isDay() ? InteractionResult.SUCCESS : InteractionResult.FAIL;
            }
        }

        return InteractionResult.PASS;
    }

    public static InteractionResult allowBed(Entity entity, BlockPos sleepingPos, BlockState state, boolean b) {
        if (state.is(JBlockRegistry.COFFIN_BLOCK.get())) {
            if (entity instanceof ServerPlayer serverPlayer) {
                return serverPlayer.isSleepingLongEnough() ? InteractionResult.FAIL : InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public static Direction modifySleepingDirection(Entity entity, BlockPos sleepingPos, Direction sleepingDirection) {
        BlockState state = entity.level().getBlockState(sleepingPos);
        if (state.is(JBlockRegistry.COFFIN_BLOCK.get())) {
            return state.getValue(CoffinBlock.FACING);
        }
        return sleepingDirection;
    }

    public static void stopSleeping(Entity entity, BlockPos sleepingPos) {
        if (entity instanceof ServerPlayer serverPlayer && serverPlayer.isSleepingLongEnough() && serverPlayer.level() instanceof ServerLevel serverWorld) {
            BlockState state = serverWorld.getBlockState(sleepingPos);
            if (state.is(JBlockRegistry.COFFIN_BLOCK.get())) {
                if (serverWorld.sleepStatus.areEnoughSleeping(serverWorld.getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE))
                        && serverWorld.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                    serverWorld.setDayTime(serverWorld.getDayTime() / 24000 * 24000 + 13000); // round up to nearest day and set to night
                }
                serverWorld.setBlockAndUpdate(sleepingPos, state.setValue(CoffinBlock.OCCUPIED, false));
            }
        }
    }

    public static void serverLevelPostTick(ServerLevel serverLevel) {
        final DimensionType dimensionType = serverLevel.dimensionType();
        if (dimensionType.hasCeiling() || !serverLevel.getGameRules().getBoolean(FALLING_METEORS)) return;

        List<ServerPlayer> players = serverLevel.getPlayers(LivingEntity::isAlive);
        if (players.isEmpty()) return;

        // Chance is percentage per second, so normalize it and divide by 20 to get per tick.
        double chance = JServerConfig.METEOR_SPAWN_RATE.getValue() / 100d / 20d;
        if (serverLevel.random.nextDouble() >= chance) return;

        // Pick a random player to target
        ServerPlayer player = players.get(serverLevel.random.nextInt(players.size()));

        final Vec3 randomPos = new Vec3(
                player.getX() + serverLevel.random.nextDouble() * 128.0 - 64.0,
                Math.min(242, serverLevel.getMaxBuildHeight() - 1),
                player.getZ() + serverLevel.random.nextDouble() * 128.0 - 64.0
        );
        final BlockPos randomBlockPos = BlockPos.containing(randomPos);
        if (serverLevel.isLoaded(randomBlockPos) && serverLevel.getBiome(randomBlockPos).is(JTagRegistry.METEORS_CAN_FALL)) {
            final StandMeteorEntity meteor = new StandMeteorEntity(serverLevel);
            meteor.setPos(randomPos);
            serverLevel.addFreshEntity(meteor);
        }
    }
}
