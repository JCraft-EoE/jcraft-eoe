package net.arna.jcraft.common.entity;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.MoveUsage;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent.HitAnimation;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.registry.JTagRegistry;
import net.arna.jcraft.common.network.s2c.DamageNumberPacket;
import net.arna.jcraft.common.util.ICustomDamageHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TrainingDummyEntity extends Mob implements GeoEntity, ICustomDamageHandler {
    public static final int HIT_ANIMATION_LENGTH = 20; // Length of hit animation in ticks

    // Data watchers
    private static final EntityDataAccessor<Boolean> HAS_KNOCKDOWN = SynchedEntityData.defineId(TrainingDummyEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> HIT_COUNTER = SynchedEntityData.defineId(TrainingDummyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> HIT_START_TIME = SynchedEntityData.defineId(TrainingDummyEntity.class, EntityDataSerializers.LONG);

    private boolean invisible;

    public TrainingDummyEntity(EntityType<? extends TrainingDummyEntity> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(0.0F);
        setPersistenceRequired();
    }

    public static AttributeSupplier.@NotNull Builder createLivingAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.ARMOR, 5.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 2.0);
    }

    public TrainingDummyEntity(Level level, double x, double y, double z) {
        this(JEntityTypeRegistry.TRAINING_DUMMY.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HAS_KNOCKDOWN, false);
        this.entityData.define(HIT_COUNTER, 0);
        this.entityData.define(HIT_START_TIME, 0L);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Invisible", this.isInvisible());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setInvisible(compound.getBoolean("Invisible"));
    }

    private boolean isOnKnockbackBlockingBlock() {
        // Check a small area around the entity's feet for cut red sandstone slabs
        BlockPos center = this.blockPosition();

        // Check current position and one block down
        for (int y = 0; y >= -1; y--) {
            BlockPos checkPos = center.offset(0, y, 0);
            if (this.level().getBlockState(checkPos).is(JTagRegistry.DUMMY_KNOCKBACK_BLOCKING)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isPushable() {
        return !isOnKnockbackBlockingBlock();
    }

    @Override
    protected void doPush(@NotNull Entity entity) {
        if (!isOnKnockbackBlockingBlock()) {
            super.doPush(entity);
        }
    }

    // Override Mob's lead behavior to allow leashing
    @Override
    public boolean canBeLeashed(Player player) {
        return true;
    }

    @Override
    protected @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        // Check BOTH hands to make sure they're completely empty
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        // ONLY pick up if BOTH hands are completely empty
        if (mainHand.isEmpty() && offHand.isEmpty()) {
            if (!player.level().isClientSide()) {
                ItemStack dummyItem = new ItemStack(JItemRegistry.TRAINING_DUMMY.get());
                player.getInventory().add(dummyItem);
                this.discard();
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean reflectsDamage() {
        return false;
    }

    @Override
    public boolean handleDamage(Vec3 kbVec, int stunTicks, int stunLevel, boolean overrideStun, float damage,
                                boolean lift, int blockstun, DamageSource source, Entity attacker,
                                HitAnimation hitAnimation,
                                MoveUsage moveUsage, boolean canBackstab, boolean unblockable) {
        if (level().isClientSide() || isRemoved()) {
            return false;
        }

        // Send damage number packet
        if (damage > 0) {
            sendDamageNumberPacket(this, damage);
        }

        if (attacker != null) {
            double deltaX = attacker.getX() - getX();
            double deltaZ = attacker.getZ() - getZ();
            float yaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F;
            setYRot(yaw);
            setYHeadRot(yaw);
            setYBodyRot(yaw);
        }

        // Trigger hit animation by incrementing counter and setting start time
        int currentCounter = entityData.get(HIT_COUNTER);
        entityData.set(HIT_COUNTER, currentCounter + 1);
        entityData.set(HIT_START_TIME, level().getGameTime());

        level().broadcastEntityEvent(this, EntityEvent.ARMORSTAND_WOBBLE);
        level().broadcastEntityEvent(this, (byte)2);

        // Apply knockback ONLY if the move specifies it and not on red sandstone slab
        if (!isOnKnockbackBlockingBlock() && kbVec != null && (kbVec.x != 0 || kbVec.y != 0 || kbVec.z != 0)) {
            push(kbVec.x, kbVec.y, kbVec.z);
            hasImpulse = true;
        }

        // Dazed effect util for combos
        if (stunTicks > 0) {
            JCraft.stun(this, stunTicks, stunLevel);
        }

        return false; // Prevent actual damage
    }

    @Override
    protected void actuallyHurt(@NotNull DamageSource damageSource, float damageAmount) {
        // Do nothing - prevents actual health reduction
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!level().isClientSide() && !isRemoved()) {
            if (!isInvulnerableTo(source) && !invisible) {
                // Send damage number packet
                if (amount > 0) {
                    sendDamageNumberPacket(this, amount);
                }

                // Face the attacker if there is one
                Entity directAttacker = source.getEntity();
                if (directAttacker != null) {
                    double deltaX = directAttacker.getX() - getX();
                    double deltaZ = directAttacker.getZ() - getZ();
                    float yaw = (float) (Math.atan2(deltaZ, deltaX) * 180.0D / Math.PI) - 90.0F;
                    setYRot(yaw);
                    setYHeadRot(yaw);
                    setYBodyRot(yaw);
                }

                // Trigger hit animation by incrementing counter and setting start time
                int currentCounter = entityData.get(HIT_COUNTER);
                entityData.set(HIT_COUNTER, currentCounter + 1);
                entityData.set(HIT_START_TIME, level().getGameTime());

                // Play sounds and damage effects
                level().broadcastEntityEvent(this, (byte)32);
                level().broadcastEntityEvent(this, (byte)2);

                return true;
            }
        }
        return false;
    }

    private static void sendDamageNumberPacket(TrainingDummyEntity entity, float damage) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 pos = entity.position();
        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(ServerPlayer.class,
                new AABB(pos.add(64, 64, 64), pos.subtract(64, 64, 64)));

        if (!nearbyPlayers.isEmpty()) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            new DamageNumberPacket(entity.getId(), damage).write(buf);

            NetworkManager.sendToPlayers(nearbyPlayers, JPacketRegistry.S2C_DAMAGE_NUMBER, buf);
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Force health to always be at maximum
        if (getHealth() < getMaxHealth()) {
            setHealth(getMaxHealth());
        }

        // Update knockdown sync on server side
        if (!level().isClientSide()) {
            boolean hasKnockdown = hasEffect(JStatusRegistry.KNOCKDOWN.get());
            boolean currentSyncValue = entityData.get(HAS_KNOCKDOWN);
            if (currentSyncValue != hasKnockdown) {
                entityData.set(HAS_KNOCKDOWN, hasKnockdown);
            }
        }

        // Pure physics-based leash constraint - no AI
        if (isLeashed()) {
            Entity holder = getLeashHolder();
            if (holder != null) {
                enforceLeashDistance(holder);
            }
        }
    }

    private void enforceLeashDistance(Entity holder) {
        double targetDistance = 2.0D; // 2 blocks
        double currentDistance = distanceTo(holder);

        if (currentDistance > targetDistance) {
            // Too far - apply spring force toward holder
            Vec3 holderPos = holder.position();
            Vec3 myPos = position();
            Vec3 direction = holderPos.subtract(myPos).normalize();

            // Spring force proportional to distance over limit
            double excess = currentDistance - targetDistance;
            double springForce = excess * 0.2D; // Adjust for spring strength

            Vec3 currentVel = getDeltaMovement();
            Vec3 correction = direction.scale(springForce);

            setDeltaMovement(currentVel.add(correction));
            hasImpulse = true;
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 32) {
            if (level().isClientSide()) {
                level().playLocalSound(getX(), getY(), getZ(),
                        SoundEvents.WOOL_STEP, getSoundSource(), 0.3F, 1.0F, false);
            }
        } else if (id == 2) {
            if (level().isClientSide()) {
                hurtTime = hurtDuration = 10;
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    // AI goals for leash following - training dummy should follow when leashed
    @Override
    protected void registerGoals() {
        // Add basic movement goal when leashed
        goalSelector.addGoal(0, new FloatGoal(this));
        // Simple goal to move toward leash holder
        goalSelector.addGoal(1, new Goal() {
            {
                setFlags(java.util.EnumSet.of(Flag.MOVE));
            }

            @Override
            public boolean canUse() {
                if (!isLeashed()) return false;
                Entity holder = getLeashHolder();
                if (holder == null) return false;

                if (holder instanceof LeashFenceKnotEntity) {
                    // For fence posts, move directly to the fence (0.25 block distance)
                    return distanceToSqr(holder) > 0.25D; // 0.25 blocks
                } else if (holder instanceof LivingEntity) {
                    // For players/entities, keep 1 block distance
                    return distanceToSqr(holder) > 1.0D;
                }
                return false;
            }

            @Override
            public void tick() {
                Entity holder = getLeashHolder();
                if (holder != null) {
                    getNavigation().moveTo(holder, 0.5D);
                }
            }
        });
    }

    // Override look control to prevent looking around
    @Override
    protected void customServerAiStep() {
        // Don't call super to prevent normal mob AI behavior like looking around
        // Only do essential leash following
        if (isLeashed()) {
            Entity holder = getLeashHolder();
            if (holder != null) {
                double requiredDistance;
                if (holder instanceof LeashFenceKnotEntity) {
                    // For fence posts, move directly to the fence
                    requiredDistance = 0.25D; // 0.25 blocks
                } else if (holder instanceof LivingEntity) {
                    // For players/entities, keep 1 block distance
                    requiredDistance = 1.0D;
                } else {
                    return;
                }

                if (this.distanceToSqr(holder) > requiredDistance) {
                    this.getNavigation().moveTo(holder, 0.5D);
                }
            }
        }
    }

    // Override methods that might interfere with mixin systems
    @Override
    public boolean canBeSeenAsEnemy() {
        return true;
    }

    @Override
    public boolean canBeSeenByAnyone() {
        return true; // Allow targeting
    }

    // Allow all status effects
    @Override
    public boolean canBeAffected(@NotNull net.minecraft.world.effect.MobEffectInstance effectInstance) {
        return super.canBeAffected(effectInstance);
    }

    // Make sure training dummy is always a valid target
    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        // Never invulnerable
        return false;
    }

    // Required abstract methods
    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return List.of();
    }

    @Override
    public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    // Basic entity properties
    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public @NotNull Component getName() {
        return Component.empty();
    }

    @Override
    public Component getCustomName() {
        return null;
    }

    @Override
    public void setCustomNameVisible(boolean alwaysRenderNameTag) {
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.WOOL_STEP;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ARMOR_STAND_BREAK;
    }

    @Override
    public boolean isAffectedByPotions() {
        return true;
    }

    @Override
    public @NotNull Pose getPose() {
        if (this.hasEffect(JStatusRegistry.KNOCKDOWN.get())) {
            return Pose.STANDING;
        }
        return super.getPose();
    }

    @Override
    public boolean attackable() {
        return true;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(JItemRegistry.TRAINING_DUMMY.get());
    }

    @Override
    protected void updateInvisibilityStatus() {
        this.setInvisible(this.invisible);
    }

    @Override
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
        super.setInvisible(invisible);
    }

    @Override
    public @NotNull PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    // Animation System
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    private int lastHitCounter = -1;

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    public static final RawAnimation
            DUMMY_IDLE = RawAnimation.begin().thenLoop("animation.sandbag.idle"),
            DUMMY_HIT = RawAnimation.begin().thenPlayAndHold("animation.sandbag.hit"), // Play once and HOLD on last frame
            DUMMY_KNOCKED_DOWN = RawAnimation.begin().thenPlay("animation.sandbag.knockdown").thenLoop("animation.sandbag.knockdownidle"),
            DUMMY_KNOCKDOWN_IDLE = RawAnimation.begin().thenPlay("animation.sandbag.knockdownidle");

    private PlayState predicate(AnimationState<TrainingDummyEntity> state) {
        // Check knockdown first
        boolean hasKnockdown;
        if (this.level().isClientSide()) {
            hasKnockdown = this.entityData.get(HAS_KNOCKDOWN);
        } else {
            hasKnockdown = this.hasEffect(JStatusRegistry.KNOCKDOWN.get());
        }

        if (hasKnockdown) {
            state.setAnimation(DUMMY_KNOCKED_DOWN);
            return PlayState.CONTINUE;
        }

        // Get hit data
        int currentHitCounter = this.entityData.get(HIT_COUNTER);
        long hitStartTime = this.entityData.get(HIT_START_TIME);
        long currentTime = this.level().getGameTime();

        // Check if it has a new hit
        if (currentHitCounter != lastHitCounter && currentHitCounter > 0) {
            // New hit detected - force reset and play hit animation
            lastHitCounter = currentHitCounter;
            state.getController().forceAnimationReset();
            state.setAnimation(DUMMY_HIT);
            return PlayState.CONTINUE;
        }

        // Check if it's still within hit animation time
        boolean stillInHitAnimation = (currentTime - hitStartTime) < HIT_ANIMATION_LENGTH && hitStartTime > 0;

        if (stillInHitAnimation) {
            // Keep playing hit animation
            state.setAnimation(DUMMY_HIT);
            return PlayState.CONTINUE;
        }

        // Not in hit animation - play idle
        state.setAnimation(DUMMY_IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}