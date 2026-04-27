package net.arna.jcraft.common.events;

import com.jcraft_eoe.jjbacosplay.CosplayItem;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import dev.architectury.platform.Platform;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.moves.BlockMarkerMove;
import net.arna.jcraft.api.component.living.CommonStandComponent;
import net.arna.jcraft.api.component.living.CommonVampireComponent;
import net.arna.jcraft.api.registry.*;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.stand.StandType;
import net.arna.jcraft.api.stand.StandTypeUtil;
import net.arna.jcraft.common.block.CoffinBlock;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.data.AttackerDataLoader;
import net.arna.jcraft.common.entity.StandMeteorEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.item.AuMockItem;
import net.arna.jcraft.common.item.RewindMockItem;
import net.arna.jcraft.common.marker.BlockMarkerMoves;
import net.arna.jcraft.common.network.s2c.AttackerDataPacket;
import net.arna.jcraft.common.saveddata.ExclusiveStandsData;
import net.arna.jcraft.common.spec.VampireSpec;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
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
import net.minecraft.world.level.block.entity.BlockEntity;
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
            List.of(Items.AIR, Items.GOLDEN_BOOTS,      Items.CHAINMAIL_BOOTS,      Items.IRON_BOOTS,      Items.DIAMOND_BOOTS,      Items.NETHERITE_BOOTS      ),
            List.of(Items.AIR, Items.GOLDEN_LEGGINGS,   Items.CHAINMAIL_LEGGINGS,   Items.IRON_LEGGINGS,   Items.DIAMOND_LEGGINGS,   Items.NETHERITE_LEGGINGS   ),
            List.of(Items.AIR, Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE ),
            List.of(Items.AIR, Items.GOLDEN_HELMET,     Items.CHAINMAIL_HELMET,     Items.IRON_HELMET,     Items.DIAMOND_HELMET,     Items.NETHERITE_HELMET     )
    );

    private static final List<ArmorMaterial> VANILLA_MATERIAL = Arrays.asList(ArmorMaterials.values());

    private static final List<List<CosplayItem<?>>> COSPLAY = List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

    public static void finishLoading(final MinecraftServer server) {
        JCraft.auWorld = server.getLevel(JDimensionRegistry.AU_DIMENSION_KEY);
        JCraft.setExclusiveStandsData(ExclusiveStandsData.fromDefaultFile(server));
        if (Platform.isModLoaded("jjbacosplay")) {
            for (final CosplayItem<?> cosplay : CosplayItem.all()) {
                switch (cosplay.getSlot()) {
                    case HELMET -> COSPLAY.get(3).add(cosplay);
                    case CHESTPLATE -> COSPLAY.get(2).add(cosplay);
                    case LEGGINGS -> COSPLAY.get(1).add(cosplay);
                    case BOOTS -> COSPLAY.get(0).add(cosplay);
                }
            }
        }
    }

    public static void saveExclusives(final MinecraftServer server) {
        if (JCraft.getExclusiveStandsData() != null) {
            JCraft.getExclusiveStandsData().saveToDefaultFile(server);
        }
    }

    public static AABB createBurstHitbox(final Vec3 pPos) {
        final Vec3 min = pPos.subtract(2.0, 2.0, 2.0);
        final Vec3 max = pPos.add(2.0, 2.0, 2.0);
        return new AABB(min, max);
    }

    public static void serverPostTick(MinecraftServer server) {
        if (JCraft.preloadLockTicks > 0) {
            JCraft.preloadLockTicks--;
        }

        //noinspection removal
        RevolverFire.tick(server);
        PeacemakerReload.tick(server);
        PastDimensions.tick(server);
        Timestops.tick(server);
        Revivables.tick(server);
        JEnemies.tick();
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

            AABB burstHitbox = createBurstHitbox(pPos);
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

        // Pushblock cooldown ticking
        pushblockCooldowns.replaceAll((key, value) -> value - 1);

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
                if (item.getOwner() != null || stack.getItem() instanceof AuMockItem) {
                    return EventResult.pass();
                }

                ItemStack mockStack = AuMockItem.createMockStack(stack); // Convert it to a mock item (incompatible and useless)
                if (stack.getItem() instanceof BlockItem) // ... and mark down all relevant data
                {
                    mockStack.getOrCreateTag().putIntArray("AttractPos", new int[]{item.getBlockX(), item.getBlockY(), item.getBlockZ()});
                }
                item.setItem(mockStack);
            } else { // ... outside the AU
                if (stack.getItem() instanceof AuMockItem) {
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

            if (!mob.getType().is(JTagRegistry.CAN_HAVE_STAND)) {
                return EventResult.pass();
            }

            // Create new stand user mobs
            if (standData.isTagged()) {
                return EventResult.pass();
            }
            if (standData.getType() != null) {
                return EventResult.pass();
            }

            RandomSource random = mob.getRandom();
            GameRules gameRules = world.getGameRules();

            standData.setTagged(true);
            // STAND
            if (!JServerConfig.SPAWNER_STANDS.getValue() && ((EntityAddon) mob).jcraft$isFromSpawner()) {
                return EventResult.pass();
            }

            if (100 - random.nextInt(0, 100) > gameRules.getInt(CHANCE_MOB_SPAWNS_WITH_STAND)) {
                return EventResult.pass();
            }
            final StandType type = StandTypeUtil.generateStandTypeForMob(gameRules);
            standData.setType(type);

            // ATTRIBUTES
            AttributeInstance followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
            if (followRange != null) {
                followRange.setBaseValue(Mth.clamp(
                        128d * (1d + entity.level().getDifficulty().getId()) / 4d,
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

            NonNullList<ItemStack> handItems = (NonNullList<ItemStack>) mob.getHandSlots();
            // Silver chariot users may spawn with Anubis (25% chance)
            if (type == JStandTypeRegistry.SILVER_CHARIOT.get() && random.nextInt(5) == 4) {
                handItems.set(0, new ItemStack(JItemRegistry.ANUBIS.get()));
            }

            if (random.nextInt(0, 100) <= 100 * JServerConfig.STAND_ARROW_SPAWN_RATE.getValue()) {
                handItems.set(1, new ItemStack(JItemRegistry.STAND_ARROW.get()));
                mob.setDropChance(EquipmentSlot.OFFHAND, 100f);
            }

            armorMob(mob);

            JEnemies.add(mob);
        }
        return EventResult.pass();
    }

    public static void armorMob(Mob mob) {
        final RandomSource random = mob.getRandom();
        final NonNullList<ItemStack> armorItems = (NonNullList<ItemStack>) mob.getArmorSlots();

        Enchantment enchantment;
        ItemStack itemStack = null;
        int baseArmorLevel = random.nextInt(1, 6);
        int enchantsSize = JCRAFT_ARMOR_ENCHANTS.size();
        for (int slot = 0; slot < 4; slot++) {
            int armorLevel = 0;
            final int diamondLevel = 4; // Check above

            if (Platform.isModLoaded("jjbacosplay")) {
                List<CosplayItem<?>> slottedCosplay = COSPLAY.get(slot);
                if (!slottedCosplay.isEmpty()) {
                    ArmorItem selected = JUtils.chooseRandom(random,
                            slottedCosplay.get(random.nextInt(slottedCosplay.size())).getAll()
                    ).get();
                    itemStack = new ItemStack(selected);
                    if (VANILLA_MATERIAL.contains(selected.getMaterial())) {
                        armorLevel = ((ArmorMaterials) selected.getMaterial()).ordinal();
                    } else {
                        armorLevel = diamondLevel;
                    }
                }
            }
            else {
                armorLevel = baseArmorLevel + random.nextInt(-1, 1);
                itemStack = new ItemStack(EQUIPMENT.get(slot).get(armorLevel));
            }
            if (itemStack != null) {
                enchantment = JCRAFT_ARMOR_ENCHANTS.get(random.nextInt(enchantsSize));
                itemStack.enchant(enchantment, enchantment.getMaxLevel());
                armorItems.set(slot, itemStack);

                if (armorLevel >= diamondLevel) {
                    mob.setDropChance(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, slot), 0f);
                }
            }
        }
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

    public static EventResult death(final LivingEntity living, final DamageSource source) {
        if (living.level() instanceof final ServerLevel serverWorld) {
            if (living instanceof final ServerPlayer serverPlayer) {
                final GameRules gameRules = serverWorld.getGameRules();

                if (!gameRules.getBoolean(JCraft.KEEP_STAND)) {
                    JComponentPlatformUtils.getStandComponent(living).setTypeAndSkin(JStandTypeRegistry.NONE.get(), 0, false);
                }

                if (!gameRules.getBoolean(JCraft.KEEP_SPEC)) {
                    JComponentPlatformUtils.getSpecData(serverPlayer)
                            .setType(JSpecTypeRegistry.NONE.get());
                }

                if (source.getEntity() instanceof LivingEntity killer) {
                    if (serverPlayer.getId() == killer.getId()) return EventResult.pass();

                    JComponentPlatformUtils.getCooldowns(killer).clear(CooldownType.COMBO_BREAKER);

                    boolean killVampirism = JServerConfig.KILL_VAMPIRISM.getValue();

                    if (killVampirism) {
                        if (killer instanceof ServerPlayer killerPlayer) {
                            killerPlayer.getFoodData().eat(20, 20f);
                            CommonVampireComponent vampireComponent = JComponentPlatformUtils.getVampirism(killerPlayer);
                            if (vampireComponent.isVampire()) {
                                vampireComponent.setBlood(VampireSpec.MAX_BLOOD);
                            }
                        }

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
            maybeLaunch(entity, source, serverWorld, entity.getEffect(JStatusRegistry.DAZED.get()), source.getEntity());
        }
        return EventResult.pass();
    }

    public static void maybeLaunch(LivingEntity entity, DamageSource source, ServerLevel serverWorld, MobEffectInstance stun, Entity attacker) {
        if (stun != null && stun.getAmplifier() != 2) {
            boolean toLaunch = false;
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

    public static EventResult beforeBlockSet(final @NonNull BlockPos blockPos, final @NonNull BlockState oldBlockState, final @NonNull BlockState newBlockState, final @NonNull Level level) {
        if (oldBlockState.is(BlockTags.LEAVES) && newBlockState.is(BlockTags.LEAVES)) {
            return EventResult.pass();
        }

        BlockMarkerMoves.mergeQueues();
        BlockMarkerMoves.forEach(move -> move.addBlock(blockPos, oldBlockState, level));

        return EventResult.pass();
    }

    public static EventResult processBlockLoot(final @NonNull List<ItemStack> loot, final @NonNull BlockState state, final @NonNull ServerLevel serverLevel, final @NonNull BlockPos pos, final @Nullable BlockEntity blockEntity) {
        if (JServerConfig.MANDOM_AFFECTS_BLOCKS.getValue()) {
            final Optional<BlockMarkerMove> move = BlockMarkerMoves.findFirst(m -> m.isRecording() && m.isInRange(pos, serverLevel));
            if (move.isPresent()) {
                final List<ItemStack> modifiedList = new LinkedList<>();
                for (ItemStack stack : loot) {
                    if (stack.getItem() instanceof RewindMockItem) {
                        modifiedList.add(stack);
                    } else {
                        modifiedList.add(RewindMockItem.createMockStack(stack, move.get()));
                    }
                }
                loot.clear();
                loot.addAll(modifiedList);
            }
        }
        return EventResult.pass();
    }

}
