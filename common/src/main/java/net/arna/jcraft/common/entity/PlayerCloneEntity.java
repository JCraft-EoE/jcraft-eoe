package net.arna.jcraft.common.entity;

import com.mojang.authlib.GameProfile;
import lombok.NonNull;
import net.arna.jcraft.common.entity.ai.goal.CloneAttackGoal;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class PlayerCloneEntity extends Monster implements RangedAttackMob, IOwnable {
    private static final EntityDataAccessor<Optional<UUID>> MASTER;
    private static final EntityDataAccessor<String> MASTER_NAME;
    private static final EntityDataAccessor<Boolean> SAND, RENDER_FOR_MASTER;
    private static final EntityDataAccessor<Byte> PART_MASK;
    private final RangedBowAttackGoal<PlayerCloneEntity> bowAttackGoal = new RangedBowAttackGoal<>(this, 1.0, 30, 15.0F);
    private final CloneAttackGoal cloneAttackGoal = new CloneAttackGoal(this, 1) {
        public void stop() {
            super.stop();
            PlayerCloneEntity.this.setAggressive(false);
        }

        public void start() {
            super.start();
            PlayerCloneEntity.this.setAggressive(true);
        }
    };
    private boolean allowItemExchange = true;

    private GameProfile gameProfile;

    private LivingEntity persistTarget = null;
    private LivingEntity master;
    private int cooldown, maxCooldown;
    private final PathNavigation navigation;
    private int disabledSlots;

    static {
        MASTER = SynchedEntityData.defineId(PlayerCloneEntity.class, EntityDataSerializers.OPTIONAL_UUID);
        MASTER_NAME = SynchedEntityData.defineId(PlayerCloneEntity.class, EntityDataSerializers.STRING);
        SAND = SynchedEntityData.defineId(PlayerCloneEntity.class, EntityDataSerializers.BOOLEAN);
        RENDER_FOR_MASTER = SynchedEntityData.defineId(PlayerCloneEntity.class, EntityDataSerializers.BOOLEAN);
        PART_MASK = SynchedEntityData.defineId(PlayerCloneEntity.class, EntityDataSerializers.BYTE);
    }

    public PlayerCloneEntity(Level world) {
        super(JEntityTypeRegistry.PLAYER_CLONE.get(), world);
        Arrays.fill(armorDropChances, 2.0F);
        Arrays.fill(handDropChances, 2.0F);

        updateAttackType();

        navigation = getNavigation();
        cooldown = 0;
        maxCooldown = 10;
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(MASTER, Optional.empty());
        entityData.define(MASTER_NAME, "");
        entityData.define(SAND, false);
        entityData.define(RENDER_FOR_MASTER, true);
        entityData.define(PART_MASK, (byte) 0);
    }

    public void disableDrops() {
        Arrays.fill(armorDropChances, 0.0F);
        Arrays.fill(handDropChances, 0.0F);
    }

    public void disableItemExchange() {
        allowItemExchange = false;
    }

    public void disableExperience() {
        xpReward = 0;
    }

    @Override
    public LivingEntity getMaster() {
        return master;
    }

    public GameProfile getGameProfile() {
        if ((gameProfile == null || gameProfile.getId() == null || gameProfile.getName() == null ||
                !gameProfile.getId().equals(getMasterId()) || !gameProfile.getName().equals(getMasterName())) &&
                getMasterId() != null) {
            gameProfile = new GameProfile(getMasterId(), getMasterName());
        }

        return gameProfile;
    }

    @Override
    public void setMaster(LivingEntity m) {
        this.master = m;
        Component mName = m.getName();
        setCustomName(mName);
        entityData.set(MASTER, Optional.of(m.getUUID()));
        entityData.set(MASTER_NAME, m.getScoreboardName());

        if (!(m instanceof ServerPlayer player)) {
            return;
        }
        byte partMask = 0;
        for (PlayerModelPart part : PlayerModelPart.values()) {
            if (player.isModelPartShown(part)) {
                partMask |= (byte) part.getMask();
            }
        }

        entityData.set(PART_MASK, partMask);
        setLeftHanded(player.getMainArm() == HumanoidArm.LEFT);
    }

    public UUID getMasterId() {
        return entityData.get(MASTER).orElse(null);
    }

    public @NonNull String getMasterName() {
        return entityData.get(MASTER_NAME);
    }

    public boolean shouldRenderForMaster() {
        return entityData.get(RENDER_FOR_MASTER);
    }

    public void setShouldRenderForMaster(boolean shouldRenderForMaster) {
        entityData.set(RENDER_FOR_MASTER, shouldRenderForMaster);
    }

    public boolean isSand() {
        return entityData.get(SAND);
    }

    public void markSand() {
        entityData.set(SAND, true);
    }

    public byte getPartMask() {
        return entityData.get(PART_MASK);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, LivingEntity.class, 32.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean canPickUpLoot() {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (getMasterId() != null) {
            nbt.putUUID("Master", getMasterId());
            nbt.putString("MasterName", getMasterName());
            nbt.putByte("PartMask", getPartMask());

            disabledSlots = nbt.getInt("DisabledSlots");
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        if (nbt.hasUUID("Master")) { // If one is here, then the rest should be too (unless the player manually modified NBT)
            entityData.set(MASTER, Optional.of(nbt.getUUID("Master")));
            entityData.set(MASTER_NAME, nbt.getString("MasterName"));
            entityData.set(PART_MASK, nbt.getByte("PartMask"));

            disabledSlots = nbt.getInt("DisabledSlots");
        }
        updateAttackType();
    }

    // Equipment handling
    private EquipmentSlot getSlotFromPosition(Vec3 hitPos) {
        EquipmentSlot equipmentSlot = EquipmentSlot.MAINHAND;
        double d = hitPos.y;
        EquipmentSlot equipmentSlot2 = EquipmentSlot.FEET;
        if (d >= 0.1 && d < 0.55 && this.hasItemInSlot(equipmentSlot2)) {
            equipmentSlot = EquipmentSlot.FEET;
        } else if (d >= 0.9 && d < 1.6 && this.hasItemInSlot(EquipmentSlot.CHEST)) {
            equipmentSlot = EquipmentSlot.CHEST;
        } else if (d >= 0.4 && d < 1.2 && this.hasItemInSlot(EquipmentSlot.LEGS)) {
            equipmentSlot = EquipmentSlot.LEGS;
        } else if (d >= 1.6 && this.hasItemInSlot(EquipmentSlot.HEAD)) {
            equipmentSlot = EquipmentSlot.HEAD;
        } else if (!this.hasItemInSlot(EquipmentSlot.MAINHAND) && this.hasItemInSlot(EquipmentSlot.OFFHAND)) {
            equipmentSlot = EquipmentSlot.OFFHAND;
        }

        return equipmentSlot;
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitPos, InteractionHand hand) {
        if (player != master || !player.isShiftKeyDown() || !allowItemExchange) {
            return InteractionResult.FAIL;
        }

        ItemStack itemStack = player.getItemInHand(hand);
        if (!itemStack.is(Items.NAME_TAG)) {
            if (player.isSpectator()) {
                return InteractionResult.SUCCESS;
            } else if (player.level().isClientSide) {
                return InteractionResult.CONSUME;
            } else {
                EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
                if (itemStack.isEmpty()) {
                    EquipmentSlot equipmentSlot2 = getSlotFromPosition(hitPos);
                    EquipmentSlot equipmentSlot3 = this.isSlotDisabled(equipmentSlot2) ? equipmentSlot : equipmentSlot2;
                    if (this.hasItemInSlot(equipmentSlot3) && this.equip(player, equipmentSlot2, itemStack, hand)) {
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    if (this.isSlotDisabled(equipmentSlot)) {
                        return InteractionResult.FAIL;
                    }
                    if (this.equip(player, equipmentSlot, itemStack, hand)) {
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.PASS;
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    private boolean isSlotDisabled(EquipmentSlot slot) {
        return (this.disabledSlots & 1 << slot.getIndex()) != 0;
    }

    private boolean equip(Player player, EquipmentSlot slot, ItemStack stack, InteractionHand hand) {
        ItemStack itemStack = this.getItemBySlot(slot);
        if (!itemStack.isEmpty() && (this.disabledSlots & 1 << slot.getIndex() + 8) != 0) {
            return false;
        } else if (itemStack.isEmpty() && (this.disabledSlots & 1 << slot.getIndex() + 16) != 0) {
            return false;
        } else {
            ItemStack itemStack2;
            if (player.getAbilities().instabuild && itemStack.isEmpty() && !stack.isEmpty()) {
                itemStack2 = stack.copy();
                itemStack2.setCount(1);
                this.setItemSlot(slot, itemStack2);
                return true;
            } else if (!stack.isEmpty() && stack.getCount() > 1) {
                if (!itemStack.isEmpty()) {
                    return false;
                } else {
                    itemStack2 = stack.copy();
                    itemStack2.setCount(1);
                    this.setItemSlot(slot, itemStack2);
                    stack.shrink(1);
                    return true;
                }
            } else {
                this.setItemSlot(slot, stack);
                player.setItemInHand(hand, itemStack);
                return true;
            }
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        updateAttackType();
        if (slot == EquipmentSlot.MAINHAND) {
            double maxCooldown = 10.0;
            Collection<AttributeModifier> attackSpeedModifiers = getMainHandItem().getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_SPEED);
            for (AttributeModifier attackSpeedModifier : attackSpeedModifiers) {
                maxCooldown *= -attackSpeedModifier.getAmount();
            }
            if (maxCooldown < 0) {
                maxCooldown = 0;
            }

            this.maxCooldown = (int) maxCooldown;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            if (isSand() && tickCount % 4 == 0) {
                level().addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                        getX() + getRandom().triangle(0, 0.5),
                        getRandomY(),
                        getZ() + getRandom().triangle(0, 0.5)
                        , 0, 0, 0
                );
            }

            //JCraft.getClientEntityHandler().playerCloneEntityClientTick(this);
        } else if (master == null) {
            // Run every 2 seconds (player lists are rather expensive)
            if (tickCount % 40 == 0) {
                // If the master id is set, but the master isn't (when loaded via NBT data), find master
                UUID master = this.getMasterId();
                if (master != null) {
                    for (ServerPlayer serverPlayerEntity : ((ServerLevel) level()).players()) {
                        if (serverPlayerEntity.getUUID().equals(master)) {
                            this.master = serverPlayerEntity;
                        }
                    }
                }
            }

            LivingEntity attacker = getLastHurtByMob();
            if (attacker != null) {
                setTarget(attacker);
            }
        } else { // Serverside, & Master isn't null
            cooldown--;

            if (persistTarget == null) {
                // Prioritize what the master is attacking, then what is attacking him
                LivingEntity attacking = master.getLastHurtMob();
                if (attacking != null && attacking.isAlive()) {
                    persistTarget = attacking;
                }

                LivingEntity attacker = master.getLastHurtByMob();
                if (attacker != null && attacker.isAlive()) {
                    persistTarget = attacker;
                }

                if (distanceToSqr(master) > 100) {
                    navigation.moveTo(master, 1);
                }
            } else if (persistTarget.isAlive() && canAttack(persistTarget)) {
                this.setTarget(this.persistTarget);
            } else { // This is called once, usually when the opponent dies
                persistTarget = null;
                this.setTarget(null);
                if (!navigation.isDone()) {
                    navigation.stop();
                }
            }
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target != master && target != this &&
                (!(target instanceof PlayerCloneEntity clone) || !clone.getMasterId().equals(getMasterId())) &&
                super.canAttack(target);
    }

    public void updateAttackType() {
        if (level() == null || level().isClientSide) {
            return;
        }
        goalSelector.removeGoal(this.cloneAttackGoal);
        goalSelector.removeGoal(this.bowAttackGoal);
        ItemStack itemStack = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
        if (itemStack.is(Items.BOW)) {
            goalSelector.addGoal(2, this.bowAttackGoal);
        } else {
            goalSelector.addGoal(2, this.cloneAttackGoal);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }

    // Ranged attack handling
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        ItemStack itemStack = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW)));
        AbstractArrow persistentProjectileEntity = this.createArrowProjectile(itemStack, pullProgress);
        double d = target.getX() - this.getX();
        double e = target.getY(0.33) - persistentProjectileEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f);
        persistentProjectileEntity.shoot(d, e + g * 0.2, f, 1.6F, 2f);
        this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(persistentProjectileEntity);
    }

    protected AbstractArrow createArrowProjectile(ItemStack arrow, float damageModifier) {
        return ProjectileUtil.getMobArrow(this, arrow, damageModifier);
    }

    public static AttributeSupplier.Builder createCloneAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void startCooldown() {
        this.cooldown = maxCooldown;
    }
}
