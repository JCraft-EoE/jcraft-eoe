package net.arna.jcraft.common.entity.projectile;


import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;

/**
 * Used in C-Moon's {@link net.arna.jcraft.common.attack.moves.cmoon.LaunchAttack}
 */
public class BlockProjectile extends JAttackEntity {
    private static final int MAX_TIME_TO_LAUNCH = 15;

    private int timeToLaunch = MAX_TIME_TO_LAUNCH;
    private int timeLaunched = 0;
    private boolean toRefresh = false;
    private boolean launched = false;
    private boolean hit = false;

    private static final EntityDataAccessor<Byte> EFFECT;
    private static final EntityDataAccessor<ItemStack> BLOCKSTACK;

    static {
        EFFECT = SynchedEntityData.defineId(BlockProjectile.class, EntityDataSerializers.BYTE);
        BLOCKSTACK = SynchedEntityData.defineId(BlockProjectile.class, EntityDataSerializers.ITEM_STACK);
    }

    public BlockProjectile(final Level world) {
        super(JEntityTypeRegistry.BLOCK_PROJECTILE.get(), world);
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(EFFECT, (byte)0);
        entityData.define(BLOCKSTACK, Items.STONE.getDefaultInstance());
    }

    public void setBlockStack(final ItemStack stack) {
        entityData.set(BLOCKSTACK, stack);
        item = stack;
    }

    /**
     * 0 - NONE
     * 1 - BREAK
     * 2 - HALT
     */
    public void setEffect(final byte effect) {
        entityData.set(EFFECT, effect);
    }

    public void markRefresh() {
        toRefresh = true;
    }

    private void breakBlock() {
        setPos(position().add(getDeltaMovement()));
        setDeltaMovement(0, 0, 0);
        setEffect((byte)1);
        setDiscardFriction(false);
        kill();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            IDLE.sendForEntity(this);

            final Vec3 vel = getDeltaMovement();
            final int effect = entityData.get(EFFECT);

            if (effect != 0) {
                for (int i = 0; i < 32; i++) {
                    ParticleOptions particle = (effect == 1) ?
                            new BlockParticleOption(ParticleTypes.BLOCK, Block.byItem(entityData.get(BLOCKSTACK).getItem()).defaultBlockState()) :
                            ParticleTypes.REVERSE_PORTAL;
                    level().addParticle(
                            particle,
                            getX() + vel.x + random.nextDouble() - 0.5,
                            getY() + vel.y + random.nextDouble() - 0.5,
                            getZ() + vel.z + random.nextDouble() - 0.5,
                            vel.x + random.nextDouble() * 2 - 1,
                            vel.y + random.nextDouble() * 2 - 1,
                            vel.z + random.nextDouble() * 2 - 1
                    );
                }
            }
            level().addParticle(ParticleTypes.REVERSE_PORTAL,
                    getX() + random.nextDouble() - 0.5,
                    getY() + random.nextDouble() - 0.5,
                    getZ() + random.nextDouble() - 0.5,
                    vel.x / 2,
                    vel.y / 2,
                    vel.z / 2
            );
        } else {
            if (master == null || deathTime > 1) {
                discard();
                return;
            }

            if (entityData.get(EFFECT) != 0) {
                setEffect((byte)0);
            }

            if (hit || onGround() || tickCount > 200) { // Placing this here delays it by 1 tick, allowing the client to see the proper end position
                breakBlock();
            }

            timeToLaunch--;
            if (timeToLaunch == 0) {
                if (toRefresh) {
                    timeToLaunch = MAX_TIME_TO_LAUNCH;
                    toRefresh = false;
                    setDeltaMovement(0, 0, 0);
                    setEffect((byte)2);
                    playSound(JSoundRegistry.CMOON_BLOCKHALT.get(), 1, 1);
                } else if (!launched) {
                    final Vec3 eP = master.getEyePosition();
                    final Vec3 rangeMod = master.getLookAngle().scale(32);
                    final EntityHitResult eHit = ProjectileUtil.getEntityHitResult(master, eP, eP.add(rangeMod),
                            master.getBoundingBox().inflate(32),
                            EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(entity -> entity != this),
                            1024 // Squared
                    );

                    final Vec3 targetPos = Objects.requireNonNullElseGet(eHit, () -> level().clip(
                            new ClipContext(eP, eP.add(rangeMod), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, master)
                    )).getLocation();

                    setDeltaMovement(targetPos.subtract(position()).normalize()); //.multiply(1)

                    playSound(JSoundRegistry.CMOON_BLOCKLAUNCH.get(), 1, 1);
                    launched = true;
                    setDiscardFriction(true);
                }
            }

            if (launched && timeLaunched < 20 && !hit) {
                timeLaunched++;
                final Set<LivingEntity> toHurt = JUtils.generateHitbox(level(), position(), 1, Set.of(master));
                final DamageSource damageSource = level().damageSources().mobAttack(master);
                for (final LivingEntity living : toHurt) {
                    LivingEntity target = JUtils.getUserIfStand(living);
                    if (target == master || target == this || !JUtils.canDamage(damageSource, target)) {
                        continue;
                    }
                    hit = true;
                    damageLogic(level(), target, getDeltaMovement(), 15, 1, true,
                            6, false, 11, damageSource, master, CommonHitPropertyComponent.HitAnimation.MID, false);
                }
            }

            if (timeLaunched == 20) {
                setNoGravity(false);
            }
        }
    }

    public static AttributeSupplier.Builder createBlockAttributes() {
        return createLivingAttributes() // This must be used instead of DefaultAttributeContainer.builder() due to compatibility with step-height-entity-attribute
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.MOVEMENT_SPEED)
                .add(Attributes.ARMOR, 10)
                .add(Attributes.ARMOR_TOUGHNESS);
    }

    @Override
    public void push(final @NonNull Entity entity) { }

    @Override
    public boolean hurt(final DamageSource source, final float amount) {
        if (source.getDirectEntity() != null) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(final @NonNull DamageSource source) {
        return SoundEvents.STONE_STEP;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STONE_BREAK;
    }

    @Override
    protected @NonNull AABB makeBoundingBox() { // Centered around 0,0,0 instead of 0,0.5,0
        double x = getX();
        double y = getY();
        double z = getZ();
        double s = 0.5;
        return new AABB(x + s, y + s, z + s, x - s, y - s, z - s);
    }

    @Override
    public void addAdditionalSaveData(final @NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        writeMasterNbt(tag);
    }

    @Override
    public void readAdditionalSaveData(final @NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        readMasterNbt(tag);
    }

    private @NonNull ItemStack item = ItemStack.EMPTY;
    @Override
    public void setItemSlot(final @NonNull EquipmentSlot slot, final @NonNull ItemStack stack) {
        item = stack;
    }
    @Override
    public @NonNull ItemStack getItemBySlot(final @NonNull EquipmentSlot slot) {
        return item;
    }

    private static final AzCommand IDLE = AzCommand.create(JCraft.BASE_CONTROLLER, "animation.block.idle", AzPlayBehaviors.LOOP);

    // Animations
    /*
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.block.idle");
    private PlayState predicate(final AnimationState<GeoAnimatable> state) {
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }*/
}
