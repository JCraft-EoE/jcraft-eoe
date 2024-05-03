package net.arna.jcraft.common.events;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.JCraft.DashData;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.StandComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.arna.jcraft.common.item.MockItem;
import net.arna.jcraft.common.network.c2s.PredictionTriggerPacket;
import net.arna.jcraft.common.network.s2c.PredictionUpdatePacket;
import net.arna.jcraft.common.tickable.*;
import net.arna.jcraft.common.util.EntityInterest;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JDimensionRegistry;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static net.arna.jcraft.JCraft.ALLOW_MOB_EVOLVED_STANDS;
import static net.arna.jcraft.JCraft.CHANCE_MOB_SPAWNS_WITH_STAND;
import static net.arna.jcraft.common.entity.stand.StandEntity.stun;
import static net.arna.jcraft.common.util.EntityInterest.blockAttractionInterest;
import static net.arna.jcraft.common.util.EntityInterest.itemAttractionInterest;

public class JServerEvents {
    private static final List<Enchantment> jcraftArmorEnchants = List.of(
            Enchantments.PROTECTION, Enchantments.PROJECTILE_PROTECTION, Enchantments.BLAST_PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.UNBREAKING);

    private static final List<List<Item>> equipment = List.of(
            List.of(Items.AIR, Items.GOLDEN_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS),
            List.of(Items.AIR, Items.GOLDEN_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS),
            List.of(Items.AIR, Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE),
            List.of(Items.AIR, Items.GOLDEN_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET)
    );

    public static void finishLoading(MinecraftServer server) {
        JCraft.auWorld = server.getWorld(JDimensionRegistry.AU_DIMENSION_KEY);
    }

    private static final int PREDICTION_RADIUS = 6 * 16;
    private static final int MAX_COMPENSATION_MS = 250; // Game is barely playable at this point
    private static final double MS_TO_TICKS = 1000.0 / 20.0; // 1000ms = 1s, 1s = 20t
    public static void serverTick(MinecraftServer server) {
        if (JCraft.preloadLockTicks > 0)
            JCraft.preloadLockTicks--;

        RevolverFire.tick(server);
        PastDimensions.tick(server);
        Timestops.tick(server);
        Revivables.tick(server);
        JEnemies.tick(server);
        // Positional prediction logic for players that want a more current look at where their enemies are, at the cost of smoothness
        PredictionTriggerPacket.getSubscribers().forEach(
                subscriber -> {
                    int adjustedPing = subscriber.pingMilliseconds;
                    if (adjustedPing > MAX_COMPENSATION_MS)
                        adjustedPing = MAX_COMPENSATION_MS;
                    double pingTicks = adjustedPing * MS_TO_TICKS;

                    Set<Pair<Integer, Vec3d>> idPosPairs = PlayerLookup.around((ServerWorld) subscriber.getWorld(), subscriber.getPos(), PREDICTION_RADIUS)
                            .stream()
                            .filter(serverPlayer -> serverPlayer != subscriber)
                            .map(
                                    serverPlayer -> {
                                        // This will likely need extension
                                        Vec3d predictedDeltaPos = JUtils.deltaPos(serverPlayer).multiply(pingTicks);
                                        return new Pair<>(serverPlayer.getId(), serverPlayer.getPos().add(predictedDeltaPos));
                                    }
                            ).collect(Collectors.toSet());

                    PredictionUpdatePacket.send(subscriber, idPosPairs);
                }
        );

        // Player logic (cooldown handling and DamageTimer counting)
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            if (player == null || !player.isAlive()) continue;

            if (player.getAttacker() != null) JComponents.getMiscData(player).startDamageTimer();
        }

        // Burst handling
        Object2IntMap<LivingEntity> newBurstTimers = new Object2IntOpenHashMap<>();

        for (Object2IntMap.Entry<LivingEntity> burst : JCraft.burstTimers.object2IntEntrySet()) {
            LivingEntity player = burst.getKey();
            burst.setValue(burst.getIntValue() - 1);
            int newVal = burst.getIntValue();

            Set<Entity> filter = new HashSet<>();
            filter.add(player);
            if (player.hasPassengers()) filter.addAll(player.getPassengerList());

            if (newVal > 0) {
                newBurstTimers.put(player, newVal);
                continue;
            }

            player.removeStatusEffect(JStatusRegistry.DAZED);
            stun(player, 10, 1);

            Vec3d pPos = player.getEyePos();

            Box burstHitbox = AbstractSimpleAttack.createBox(pPos, 4);
            List<? extends Entity> toPush = player.getWorld().getEntitiesByClass(Entity.class, burstHitbox,
                    EntityPredicates.VALID_LIVING_ENTITY.and(e -> !filter.contains(e)));
            JUtils.displayHitbox(player.getWorld(), burstHitbox);

            for (Entity ent : toPush) {
                Vec3d awayVector = ent.getPos().subtract(pPos).normalize();
                boolean pushAway = true;

                // If the stand was hit, the attack will stop and the user will be hit remotely
                if (ent instanceof StandEntity<?, ?> stand) {
                    if (stand.hasUser()) {
                        stun(stand.getUser(), 10, 3);
                        stand.cancelMove();
                    }
                } else if (ent.getFirstPassenger() instanceof StandEntity<?, ?> stand) { // Stands should not have passengers
                    if (stand.blocking) pushAway = false;
                    else if (ent instanceof LivingEntity living) { // Stand users that aren't blocking get launched and their stand attacks are cancelled
                        //awayVector = awayVector.multiply(0.5);
                        stun(living, 10, 3);
                        stand.cancelMove();
                    }
                }

                if (!pushAway) continue;
                JUtils.setVelocity(ent, awayVector.x, awayVector.y / 5 + 0.4, awayVector.z);
            }
        }

        JCraft.burstTimers.clear();
        JCraft.burstTimers.putAll(newBurstTimers);

        // Dash handling
        for (Map.Entry<LivingEntity, DashData> entry : new HashSet<>(JCraft.dashes.entrySet())) {
            DashData dash = entry.getValue();
            dash.tickDash();

            if (dash.finished) JCraft.dashes.remove(entry.getKey());
        }

        // Handle items of interest
        Map<Entity, EntityInterest> entitiesOfInterest = JCraft.getEntitiesOfInterest();
        HashMap<Entity, EntityInterest> newItemsOfInterest = new HashMap<>();

        for (Map.Entry<Entity, EntityInterest> entityAndInterest : entitiesOfInterest.entrySet()) {
            Entity entity = entityAndInterest.getKey();
            if (entity == null || !entity.isAlive()) continue;
            EntityInterest interest = entityAndInterest.getValue();
            ServerWorld serverWorld = (ServerWorld) entity.getWorld();
            boolean saveForNextIteration = true;

            switch (interest.getType()) {
                default -> saveForNextIteration = false;
                case BLOCK_ATTRACTION -> {
                    BlockPos attractionBlockPos = interest.getAttractionBlockPos();
                    if (entity.squaredDistanceTo(attractionBlockPos.getX(), attractionBlockPos.getY(), attractionBlockPos.getZ()) < 4) {
                        boolean griefing = serverWorld.getGameRules().getBoolean(JCraft.STAND_GRIEFING);
                        dimensionalExplosion(serverWorld, griefing, entity);
                        if (griefing) serverWorld.setBlockState(attractionBlockPos, Blocks.AIR.getDefaultState());
                    } else {
                        BlockPos delta = attractionBlockPos.subtract(entity.getBlockPos());
                        Vec3d towardsVel = new Vec3d(delta.getX(), delta.getY(), delta.getZ()).normalize();
                        entity.addVelocity(towardsVel.x, towardsVel.y, towardsVel.z);
                        entity.velocityModified = true;
                    }
                }
                case ITEM_ATTRACTION -> {
                    if (!(entity instanceof ItemEntity item)) continue;
                    for (Map.Entry<Entity, EntityInterest> entityAndInterest2 : entitiesOfInterest.entrySet()) {
                        Entity entity2 = entityAndInterest2.getKey();
                        if (entity2 instanceof ItemEntity item2) {
                            if (
                                    entityAndInterest2.getValue().getType() == EntityInterest.ItemInterestType.ITEM_ATTRACTION &&
                                    item2 != entity &&
                                    item2.getWorld() == serverWorld &&
                                    item2.getStack().getItem() == item.getStack().getItem() &&
                                    item2.squaredDistanceTo(entity) <= 256
                            ) {
                                Vec3d converge = item2.getPos().subtract(entity.getPos());
                                Vec3d towardsVector = converge.normalize().multiply(0.25);
                                entity.addVelocity(towardsVector.x, towardsVector.y, towardsVector.z);
                                entity.velocityModified = true;

                                if (item2.distanceTo(entity) <= 1.0) {
                                    dimensionalExplosion(serverWorld, serverWorld.getGameRules().getBoolean(JCraft.STAND_GRIEFING), entity, item2);
                                    saveForNextIteration = false;
                                }
                            }
                        }
                    }
                }
            }

            if (saveForNextIteration) newItemsOfInterest.put(entity, interest);
        }

        entitiesOfInterest.clear();
        entitiesOfInterest.putAll(newItemsOfInterest);
    }

    private static void dimensionalExplosion(ServerWorld serverWorld, boolean griefing, Entity one) {
        dimensionalExplosion(serverWorld, griefing, one, null);
    }

    private static void dimensionalExplosion(ServerWorld serverWorld, boolean griefing, Entity one, @Nullable Entity other) {
        Vec3d midPos = one.getPos();
        if (other != null) {
            midPos = midPos.add(other.getPos()).multiply(0.5);
            other.discard();
        }

        one.discard();

        Explosion explosion = serverWorld.createExplosion(null, midPos.x, midPos.y, midPos.z, 1f,
                griefing ? World.ExplosionSourceType.BLOCK : World.ExplosionSourceType.NONE);

        List<LivingEntity> toDamage = serverWorld.getEntitiesByClass(LivingEntity.class,
                new Box(midPos.add(1.5, 1.5, 1.5), midPos.subtract(1.5, 1.5, 1.5)),
                EntityPredicates.VALID_ENTITY);

        for (LivingEntity ent : toDamage) {
            ent.damage(explosion.getDamageSource(), 7);
            StandEntity.stun(ent, 10, 3);
            ent.addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 35, 0));
        }
    }

    public static void entityLoad(Entity entity, ServerWorld world) {
        // If an item was spawned
        if (entity instanceof ItemEntity item) {
            ItemStack stack = item.getStack();

            if (stack.isOf(JObjectRegistry.ANUBIS)) {
                item.setPickupDelay(0);
                return;
            }

            if (stack.isOf(JObjectRegistry.FV_REVOLVER)) {
                JCraft.markItemOfInterest(item, EntityInterest.itemAttractionInterest(JObjectRegistry.FV_REVOLVER));
                return;
            }

            // ... in the AU
            if (world.getRegistryKey().equals(JDimensionRegistry.AU_DIMENSION_KEY)) {
                if (item.getOwner() != null || MockItem.isMockItem(stack)) return;

                ItemStack mockStack = MockItem.createMockStack(stack); // Convert it to a mock item (incompatible and useless)
                if (stack.getItem() instanceof BlockItem) // ... and mark down all relevant data
                    mockStack.getOrCreateNbt().putIntArray("AttractPos", new int[]{item.getBlockX(), item.getBlockY(), item.getBlockZ()});
                item.setStack(mockStack);
            } else { // ... outside the AU
                if (MockItem.isMockItem(stack)) {
                    // Mark it as an item of interest, and save relevant data
                    NbtCompound stackData = stack.getOrCreateNbt();
                    if (stackData.contains("AttractPos")) { // if attracted to a specific position
                        String itemId = stackData.getString("MockItem");
                        int[] attractPos = stackData.getIntArray("AttractPos");
                        BlockPos attractBlockPos = new BlockPos(attractPos[0], attractPos[1], attractPos[2]);
                        if ( // ... if the world has the specified block item
                                Registries.ITEM.getId(
                                        world.getBlockState(attractBlockPos).getBlock().asItem()
                                ).toString().equals(itemId)
                        )
                            JCraft.markItemOfInterest(item, blockAttractionInterest(attractBlockPos));
                    } else { // if not attracted to a specific position, it's a general item to attract
                        JCraft.markItemOfInterest(item, itemAttractionInterest(stack.getItem()));
                    }
                }
            }
        }

        if (entity instanceof MobEntity mob) {
            // Mark stand user mobs
            StandComponent standData = JComponents.getStandData(mob);
            if (standData.getType() != null && standData.getType() != StandType.NONE) {
                JEnemies.add(mob);
                return;
            }

            // Create new stand user mobs
            if (mob.age > 0) return;
            if (standData.getType() != null) return;
            EntityGroup group = mob.getGroup();

            if (group != EntityGroup.UNDEAD && group != EntityGroup.ILLAGER && !(mob instanceof EndermanEntity)) return;
            Random random = new Random();
            GameRules gameRules = world.getGameRules();

            // STAND
            if (100 - random.nextInt(0, 100) > gameRules.getInt(CHANCE_MOB_SPAWNS_WITH_STAND)) return;
            List<StandType> types = gameRules.getBoolean(ALLOW_MOB_EVOLVED_STANDS) ? StandType.getAllStandTypes() : StandType.getRegularStandTypes();
            StandType type = types.get(random.nextInt(types.size()));
            standData.setType(type);

            // ATTRIBUTES
            EntityAttributeInstance followRange = mob.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
            if (followRange != null) followRange.setBaseValue(128.0);
            EntityAttributeInstance movementSpeed = mob.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (movementSpeed != null && movementSpeed.getBaseValue() < 0.3) movementSpeed.setBaseValue(0.3);

            // EQUIPMENT
            if (mob.getMaxHealth() > 100.0) return;

            DefaultedList<ItemStack> handItems = (DefaultedList<ItemStack>) mob.getHandItems(), armorItems = (DefaultedList<ItemStack>) mob.getArmorItems();
            // Silver chariot users may spawn with Anubis (25% chance)
            if (type == StandType.SILVER_CHARIOT && random.nextInt(5) == 4)
                handItems.set(0, new ItemStack(JObjectRegistry.ANUBIS));

            if (random.nextInt(0, 100) >= 90) {
                handItems.set(1, new ItemStack(JObjectRegistry.STANDARROW));
                mob.setEquipmentDropChance(EquipmentSlot.OFFHAND, 100f);
            }

            Enchantment enchantment;
            ItemStack itemStack;
            int baseArmorLevel = random.nextInt(1, 6);
            int enchantsSize = jcraftArmorEnchants.size();
            for (int i = 0; i < 4; i++) {
                itemStack = new ItemStack(equipment.get(i).get(baseArmorLevel + random.nextInt(-1, 1)));
                enchantment = jcraftArmorEnchants.get(random.nextInt(enchantsSize));
                itemStack.addEnchantment(enchantment, enchantment.getMaxLevel());
                armorItems.set(i, itemStack);
            }

            JEnemies.add(mob);
        }
    }
}
