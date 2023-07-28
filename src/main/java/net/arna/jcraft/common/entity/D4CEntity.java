package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.Attack;
import net.arna.jcraft.common.attack.AttackType;
import net.arna.jcraft.common.attack.StunType;
import net.arna.jcraft.common.item.MockItem;
import net.arna.jcraft.common.util.DimValues;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.common.util.StandAnimationState;
import net.arna.jcraft.mixin.ChunkLightProviderAccessor;
import net.arna.jcraft.mixin.LightStorageAccessor;
import net.arna.jcraft.mixin.LightingProviderAccessor;
import net.arna.jcraft.registry.JDimensionRegister;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkToNibbleArrayMap;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import software.bernie.geckolib3.core.builder.AnimationBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class D4CEntity extends StandEntity<D4CEntity, D4CEntity.State> {
    public static final Attack crm1 = new Attack(11, JCraft.lightCooldown, 0.75f, 15, 11, 0, 0, 0f, AttackType.BOX)
            .setInfo("Item Place", "places an item from an alternate universe on the ground, attracts other such items");
    public static final Attack light = new Attack(0, JCraft.lightCooldown, 0.75f, 15, 9, 1.5, 5f, 0.75f, AttackType.BOX, 1.1f, -0.1f, 0, JSoundRegister.IMPACT_2)
            .crouchingVariation(crm1)
            .setInfo("Chop", "quick combo starter");
    public static final Attack barrage = new Attack(2, 17, 0.75f, 70, 0, 2, 0.8f, 0.25f, AttackType.BARRAGE, 2, 0, 3, JSoundRegister.IMPACT_2)
            .setInfo("Barrage", "fast reliable combo starter/extender, high stun");
    public static final Attack heavy = new Attack(1, 15, 1, 25, 14, 2, 8f, 1.5f, AttackType.BOX, 0.6f, -0.2f, 0, JSoundRegister.IMPACT_2)
            .setHitspark(2)
            .hyperArmor()
            .setLaunch()
            .setInfo("Charge", "user & stand charge forward, uninterruptable launcher");
    public static final Attack dimhop_others = new Attack(3, 60, 1, 60, 40, 1.5, 0f, 0.0f, AttackType.BOX)
            .setInfo("Dimensional Hop", "travels to a random dimension at exact coordinates, if user was hit in the last 30s, he is forced back, certified death button");

    public static final Attack givegun = new Attack(6, 25, 14, 10, 0, 0.75f, AttackType.BOX)
            .setInfo("Summon Gun", "gives the user a revolver");
    public static final Attack grab = new Attack(4, 25, 0.75f, 21, 12, 1.5, 0f, 0.0f, AttackType.BOX, 2, 0, 0, null)
            .setGrab()
            .crouchingVariation(givegun)
            .setInfo("Grab", "unblockable, combo finisher");
    public static final Attack grabhit = new Attack(5, 0, 0.75f, 34, 0, 2, 4f, 0f, AttackType.MULTIHIT, 0.5f, 0, List.of(11, 17, 26), JSoundRegister.IMPACT_1)
            .setStunType(StunType.UNBURSTABLE)
            .setInfo("Grab (Hit)", "");
    private static final Attack grabhitfinal = new Attack(10, 0, 0.75f, 34, 0, 2, 4f, 1.2f, AttackType.MULTIHIT, 0.45f, 0, List.of(11, 17, 26), JSoundRegister.IMPACT_1)
            .setHitspark(2)
            .setLaunch()
            .setInfo("Grab (Final Hit)", "");


    public static final Attack counter = new Attack(7, 30, 35, 5, 0, 0.75f, AttackType.COUNTER)
            .setInfo("Counter", "0.25s startup, 1.5s duration, high damage, knocks back when hit");
    public static final Attack clonespawn = new Attack(8, 40, 1, 50, 40, 0, 0f, 0.0f, AttackType.BOX)
            .setRanged(true)
            .setInfo("Dimensional Clone", "summons an unlimited number of servants");
    public static final Attack flag = new Attack(9, 20, 60, 10, 0, 0, AttackType.BOX)
            .setInfo("Dimensional Phase", "hides in a flag in an un-stunnable, floating state")
            .setMobility(MobilityType.HIGHJUMP);

    public static ServerWorld auWorld;

    public D4CEntity(World worldIn) {
        super(StandType.D4C, worldIn);
        super.initialize();
        idleRotation = -45f;

        description = "All Range, Multipurpose TRICKSTER";

        pros = List.of(
                "good combo tools",
                "counter",
                "extensive setups",
                "good pressure"
        );

        cons = List.of(
                "optimal setups and combos require preparation",
                "slower than average"
        );

        freespace =
                """
                        BNBs:
                            the lazy zoner
                            M1>Barrage>M1>Grab/Charge
                            
                            the western
                            M1>Summon Gun>Barrage>M1~stand.OFF>M2>M2>M2>M2>M2>M2~s.ON+M1>Charge""";

        moves = List.of(light, heavy, barrage, dimhop_others, clonespawn, grab, counter, flag);

        if (world.isClient) return;
        auWorld = Objects.requireNonNull(getServer()).getWorld(JDimensionRegister.AU_DIMENSION_KEY);
    }

    private static final List<ItemStack> placeableStacks = List.of(
            Items.STICK.getDefaultStack(),
            Items.COBBLESTONE.getDefaultStack(),
            Items.DEAD_BUSH.getDefaultStack(),
            Items.APPLE.getDefaultStack(),
            Items.OAK_SAPLING.getDefaultStack()
    );
    private boolean placingFirstStack = true;
    private ItemStack placing;
    @Override
    public void initLightAttack() {
        if (!canAttack()) return;
        if (getUserOrThrow().isSneaking() && handleAttack(crm1, JCraft.standLightCD, State.ITEM_PLACE)) {
            if (placingFirstStack) {
                placing = MockItem.createMockStack( placeableStacks.get(random.nextInt(placeableStacks.size())) );
            }
            equipStack(EquipmentSlot.OFFHAND, placing.copy());
            placingFirstStack = !placingFirstStack;
        } else if (handleAttack(light, JCraft.standLightCD, State.LIGHT))
            playSound(JSoundRegister.D4C_LIGHT, 1, 1);
    }

    @Override
    public void initHeavyAttack() {
        if (!canAttack() || !handleAttack(heavy, JCraft.standHeavyCD, State.HEAVY)) return;

        playSound(JSoundRegister.D4C_HEAVY, 1, 1);
        Entity ent = getUserOrThrow();

        if (!ent.isOnGround()) return;
        ent.setVelocity(ent.getVelocity().add(this.getRotationVector().multiply(0.75)).add(0.0, 0.15, 0.0));
        ent.velocityModified = true;
    }

    @Override
    public void initBarrage() {
        if (!canAttack()) return;
        if (handleAttack(barrage, JCraft.standBarrageCD, State.BARRAGE))
            playSound(JSoundRegister.D4C_BARRAGE, 1, 1);
    }

    @Override
    public void initSpecial1() {
        if (!canAttack()) return;
        if (handleAttack(clonespawn, JCraft.standS1CD, State.DIM_HOP))
            playSound(JSoundRegister.D4C_DIMHOP, 1, 1);
    }

    @Override
    public void initUlt() {
        // Ability to cancel dimension hop
        if (curAttack == dimhop_others) {
            setMoveStun(0);
            curAttack = null;
        }

        if (!canAttack()) return;

        LivingEntity user = getUser();
        if (user instanceof ServerPlayerEntity serverPlayer) { // Logic for cancelling dimhop early, and generating failsafe data
            if (user.getWorld().getRegistryKey().equals(JDimensionRegister.AU_DIMENSION_KEY)) {
                boolean isStored = false; // Should always be true
                for (DimValues dimV : JCraft.pastDimensions) {
                    if (dimV.user != user)
                        continue;
                    isStored = true;
                    dimV.timer = 1;
                }

                if (!isStored) { // If not stored, force your way back
                    BlockPos spawnPos = serverPlayer.getSpawnPointPosition(); // Prioritize spawn point
                    // Use current position if all else fails
                    if (spawnPos == null) spawnPos = serverPlayer.getBlockPos();
                    JCraft.pastDimensions.add(new DimValues(user,
                            new Vec3d(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ()),
                            serverPlayer.getSpawnPointDimension()));
                }
            }
        }

        if (handleAttack(dimhop_others, JCraft.standUltCD, State.DIM_HOP)) playSound(JSoundRegister.D4C_DIMHOP, 1, 1);
    }

    @Override
    public void initSpecial2() {
        if (!canAttack() || !hasUser()) return;
        if (getUserOrThrow().isSneaking() && handleAttack(givegun, JCraft.standS2CD, State.GIVE_GUN)) {
            playSound(JSoundRegister.D4C_THROW, 1, 1);
            equipStack(EquipmentSlot.MAINHAND, JObjectRegistry.FVREVOLVER.getDefaultStack());
        } else if (handleAttack(grab, JCraft.standS2CD, State.THROW)) {
            playSound(JSoundRegister.D4C_THROW, 1, 1);
            equipStack(EquipmentSlot.MAINHAND, JObjectRegistry.FVREVOLVER.getDefaultStack());
        }
    }

    @Override
    public void initSpecial3() {
        if (!canAttack()) return;
        handleAttack(counter, JCraft.standS3CD, State.COUNTER);
    }

    @Override
    protected Box calculateBoundingBox() {
        if (getState() == State.FLAG) {
            double x = getX();
            double y = getY();
            double z = getZ();
            return new Box(x + 0.5, y + 0.5, z + 0.5, x - 0.5, y, z - 0.5);
        }
        return super.calculateBoundingBox();
    }

    @Override
    public void initUtil() {
        if (!canAttack() || !hasUser()) return;
        if (handleAttack(flag, JCraft.utilCD, State.FLAG)) {
            getUserOrThrow().addStatusEffect(new StatusEffectInstance(JStatusRegister.KNOCKDOWN, flag.moveStun, 0, true, false));
            getUserOrThrow().addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, flag.moveStun, 0, true, false));
            playSound(JSoundRegister.D4C_UTILITY, 1, 1);
        }
    }

    /* -- OLD GUN THROW CODE
                Vec3d rotVec = this.getRotationVector();
                Vec3d eyePos = this.getEyePos();

                ItemEntity revolver1 = new ItemEntity(EntityType.ITEM, world);
                revolver1.setStack(new ItemStack(JObjectRegistry.FVREVOLVER, 1));
                revolver1.setPickupDelay(100);
                revolver1.setPosition(eyePos.add(rotVec.rotateY(90)));
                revolver1.setVelocity(rotVec.rotateY(95).multiply(1.5));

                ItemEntity revolver2 = new ItemEntity(EntityType.ITEM, world);
                revolver2.setStack(new ItemStack(JObjectRegistry.FVREVOLVER, 1));
                revolver2.setPickupDelay(100);
                revolver2.setPosition(eyePos.add(rotVec.rotateY(-90)));
                revolver2.setVelocity(rotVec.rotateY(-95).multiply(1.5));

                world.spawnEntity(revolver1);
                world.spawnEntity(revolver2);
    */

    @Override
    public void specialAttack(Attack attack, List<LivingEntity> entities) {
        LivingEntity user = getUser();
        switch (attack.id) {
            case (3) -> {
                ChunkPos origin = getChunkPos();
                ServerWorld world = (ServerWorld) getWorld();

                // Lighting providers are too complicated, man. Wth
                // We got 2 providers, every provider has 2 storages and every storage has 2 storages.
                LightingProvider ogLightingProvider = world.getLightingProvider();
                LightingProvider auLightingProvider = auWorld.getLightingProvider();
                LightStorageAccessor ogBlockLightStorage0 = (LightStorageAccessor) ((ChunkLightProviderAccessor)
                        ((LightingProviderAccessor) ogLightingProvider).getBlockLightProvider()).getLightStorage();
                LightStorageAccessor auBlockLightStorage0 = (LightStorageAccessor) ((ChunkLightProviderAccessor)
                        ((LightingProviderAccessor) auLightingProvider).getBlockLightProvider()).getLightStorage();
                LightStorageAccessor ogSkyLightStorage0 = (LightStorageAccessor) ((ChunkLightProviderAccessor)
                        ((LightingProviderAccessor) ogLightingProvider).getSkyLightProvider()).getLightStorage();
                LightStorageAccessor auSkyLightStorage0 = (LightStorageAccessor) ((ChunkLightProviderAccessor)
                        ((LightingProviderAccessor) auLightingProvider).getSkyLightProvider()).getLightStorage();

                ChunkToNibbleArrayMap<?> ogBlockLightStorage = ogBlockLightStorage0.getStorage();
                ChunkToNibbleArrayMap<?> ogUncachedBlockLightStorage = ogBlockLightStorage0.getUncachedStorage();
                ChunkToNibbleArrayMap<?> auBlockLightStorage = auBlockLightStorage0.getStorage();
                ChunkToNibbleArrayMap<?> auUncachedBlockLightStorage = auBlockLightStorage0.getUncachedStorage();
                ChunkToNibbleArrayMap<?> ogSkyLightStorage = ogSkyLightStorage0.getStorage();
                ChunkToNibbleArrayMap<?> ogUncachedSkyLightStorage = ogSkyLightStorage0.getUncachedStorage();
                ChunkToNibbleArrayMap<?> auSkyLightStorage = auSkyLightStorage0.getStorage();
                ChunkToNibbleArrayMap<?> auUncachedSkyLightStorage = auSkyLightStorage0.getUncachedStorage();

                for (int x = -3; x < 4; x++) {
                    for (int z = -3; z < 4; z++) {
                        int cX = origin.x + x;
                        int cZ = origin.z + z;
                        JCraft.preloadChunk(auWorld, cX, cZ);

                        WorldChunk ogChunk = world.getChunk(cX, cZ);
                        WorldChunk auChunk = auWorld.getChunk(cX, cZ);

                        ChunkSection[] sections = ogChunk.getSectionArray();
                        ChunkSection[] copies = IntStream.range(0, sections.length)
                                .mapToObj(i -> {
                                    ChunkSection copy = new ChunkSection(world.sectionIndexToCoord(i),
                                            world.getRegistryManager().get(Registry.BIOME_KEY));

                                    PacketByteBuf serialized = PacketByteBufs.create();
                                    sections[i].toPacket(serialized);
                                    copy.fromPacket(serialized);
                                    return copy;
                                })
                                .toArray(ChunkSection[]::new);

                        ChunkSection[] auSec = auChunk.getSectionArray();
                        System.arraycopy(copies, 0, auSec, 0, Math.min(copies.length, auSec.length));

                        // Copy light for every section.
                        for (int y = auWorld.getBottomY(); y < auWorld.getTopY(); y += 16) {
                            long cPos = ChunkSectionPos.toLong(new BlockPos(cX * 16, y, cZ * 16));
                            auBlockLightStorage.put(cPos, Optional.ofNullable(ogBlockLightStorage.get(cPos))
                                    .map(ChunkNibbleArray::copy).orElse(null));
                            auUncachedBlockLightStorage.put(cPos, Optional.ofNullable(ogUncachedBlockLightStorage.get(cPos))
                                    .map(ChunkNibbleArray::copy).orElse(null));
                            auSkyLightStorage.put(cPos, Optional.ofNullable(ogSkyLightStorage.get(cPos))
                                    .map(ChunkNibbleArray::copy).orElse(null));
                            auUncachedSkyLightStorage.put(cPos, Optional.ofNullable(ogUncachedSkyLightStorage.get(cPos))
                                    .map(ChunkNibbleArray::copy).orElse(null));
                        }
                    }
                }

                for (BlockPos pos : BlockPos.iterate(new BlockPos(origin.getStartX() - 3 * 16, world.getBottomY(), origin.getStartZ() - 3 * 16),
                        new BlockPos(origin.getEndX() + 3 * 16, world.getTopY(), origin.getEndZ() + 3 * 16))) {
                    auWorld.removeBlockEntity(pos); // Ensure the old one is gone.
                    auWorld.getBlockEntity(pos); // Creates the BE if it does not yet exist while there should be one.
                }

                List<Entity> toHop = new ArrayList<>(entities);
                toHop.add(user);
                int heightOffset = auWorld.getHeight() - world.getHeight();
                for (Entity entity : toHop)
                    JCraft.dimensionHop(entity, heightOffset / 2);
            }
            case (4) -> {
                if (!entities.isEmpty()) {
                    // Grab bypasses and disables block
                    for (LivingEntity ent : entities) {
                        stun(ent, 34, 0);
                        if (ent.getFirstPassenger() instanceof StandEntity<?, ?> stand)
                            stand.blocking = false;
                    }

                    setAttack(grabhit, State.THROW_HIT);
                } else getMainHandStack().decrement(1);
            }
            case (5) -> {
                if (getMoveStun() == 17)
                    curAttack = grabhitfinal;
                playSound(JSoundRegister.REVOLVER_FIRE, 1, 1);
            }
            case (6) -> {
                if (user instanceof PlayerEntity playerEntity) {
                    playerEntity.giveItemStack(JObjectRegistry.FVREVOLVER.getDefaultStack());
                    getMainHandStack().decrement(1);
                }
            }
            case (8) -> {
                ItemStack weapon = new ItemStack(Items.IRON_SWORD);
                weapon.setDamage(249);

                if (user instanceof ServerPlayerEntity playerEntity) {
                    PlayerCloneEntity playerCloneEntity = new PlayerCloneEntity(PlayerCloneEntity.getCloneType(playerEntity), this.world);
                    playerCloneEntity.copyPositionAndRotation(playerEntity);
                    playerCloneEntity.setMaster(playerEntity);

                    world.spawnEntity(playerCloneEntity);
                    playerCloneEntity.equipStack(EquipmentSlot.MAINHAND, weapon);
                } else if (user instanceof MobEntity mob) { //Code sourced from MobEntity.class convertTo()
                    EntityType<?> entityType = mob.getType();
                    MobEntity newMob = (MobEntity) entityType.create(world);

                    if (newMob == null) {
                        JCraft.LOGGER.error("Failed to create D4C clone mob of type " + entityType + " in world " + world);
                        return;
                    }

                    newMob.copyPositionAndRotation(mob);
                    newMob.setBaby(mob.isBaby());

                    if (mob.hasCustomName()) {
                        newMob.setCustomName(mob.getCustomName());
                        newMob.setCustomNameVisible(mob.isCustomNameVisible());
                    }

                    newMob.age = mob.age;
                    IEntityDataSaver newMobData = (IEntityDataSaver) newMob;
                    newMobData.getPersistentData().putInt("StandID", 0);

                    world.spawnEntity(newMob);
                    newMob.equipStack(EquipmentSlot.MAINHAND, weapon);
                }
            }
            case (9) -> {
                if (!hasUser()) return;

                int duration = flag.moveStun - flag.initTime;
                getUserOrThrow().addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, duration, 0, true, false));
                getUserOrThrow().addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, duration, 2, true, false));
            }
            case (10) -> getMainHandStack().decrement(1);
            case (11) -> {
                ItemStack offHandStack = getOffHandStack();
                ItemEntity item = new ItemEntity(getWorld(), getX(), getY() + 0.2, getZ(), placing.copy(), 0, 0, 0);
                item.setPickupDelay(200);
                world.spawnEntity(item);
                offHandStack.decrement(1);
            }
        }
    }

    @Override
    public void counter(Entity entity, DamageSource source) {
        super.counter(entity, source);

        if (entity == null || !hasUser()) return;
        LivingEntity user = getUserOrThrow();
        if (!source.isProjectile() && !source.isMagic()) {
            Vec3d trueKnockback = entity.getPos().subtract(user.getPos()).normalize().multiply(1.5);
            entity.addVelocity(trueKnockback.x, 0.5, trueKnockback.z);
            entity.velocityModified = true;

            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.damage(DamageSource.mob(user), 10);
                stun(livingEntity, 20, 3);

                StandEntity<?, ?> stand = ((IEntityDataSaver) livingEntity).getStand();
                if (stand != null)
                    stand.cancelAttack();
            }

            world.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1f, 1f);
            playSound(JSoundRegister.D4C_COUNTER, 1, 1);
        }
    }

    private static final Attack counterMiss = new Attack(14, 0, 10, 11);
    @Override
    public void whiffCounter() {
        setAttack(counterMiss, State.COUNTER_MISS);
        stun(getUser(), counterMiss.moveStun, 0);
    }

    @Override
    public void tick() {
        if (age == 1) {
            playSound(JSoundRegister.STAND_SUMMON, 1, 1);
            playSound(JSoundRegister.D4C_SUMMON, 1, 1);
        }

        super.tick();

        if (hasUser())
            setAlpha((float) MathHelper.clamp(255.0 * squaredDistanceTo(getUser()) / 2, 0.0, 255.0) / 255f);
    }

    // Animation code
    public enum State implements StandAnimationState<D4CEntity> {
        IDLE(builder -> builder.loop("animation.d4c.idle")),
        LIGHT(builder -> builder.playAndHold("animation.d4c.light")),
        BLOCK(builder -> builder.loop("animation.d4c.block")),
        HEAVY(builder -> builder.playAndHold("animation.d4c.heavy")),
        BARRAGE(builder -> builder.loop("animation.d4c.barrage")),
        DIM_HOP(builder -> builder.playAndHold("animation.d4c.dimhop")),
        THROW(builder -> builder.playAndHold("animation.d4c.throw")),
        THROW_HIT(builder -> builder.playAndHold("animation.d4c.throwhit")),
        COUNTER(builder -> builder.loop("animation.d4c.counter")),
        COUNTER_MISS(builder -> builder.playAndHold("animation.d4c.counter_miss")),
        GIVE_GUN(builder -> builder.playAndHold("animation.d4c.givegun")),
        FLAG(builder -> builder.playAndHold("animation.d4c.flag")),
        ITEM_PLACE(builder -> builder.playAndHold("animation.d4c.itemplace"));

        private final Consumer<AnimationBuilder> animator;

        State(Consumer<AnimationBuilder> animator) {
            this.animator = animator;
        }

        @Override
        public void playAnimation(D4CEntity stand, AnimationBuilder builder) {
            animator.accept(builder);
        }
    }

    @Override
    protected State[] getStateValues() {
        return State.values();
    }

    @Override
    protected String getSummonAnimation() {
        return "animation.d4c.summon";
    }

    @Override
    public State getBlockState() {
        return State.BLOCK;
    }
}
