package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;

public class LifeDetectorEntity extends JAttackEntity {
    private static final EntityDataAccessor<Boolean> EXPLODED;
    private LivingEntity target;

    static {
        EXPLODED = SynchedEntityData.defineId(LifeDetectorEntity.class, EntityDataSerializers.BOOLEAN);
    }

    public boolean hasExploded() {
        return this.entityData.get(EXPLODED);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(EXPLODED, false);
    }

    public LifeDetectorEntity(Level world) {
        super(JEntityTypeRegistry.LIFE_DETECTOR.get(), world);
    }

    @Override
    public boolean canAttack(@Nullable LivingEntity target) {
        if (target == null || master == null) {
            return false;
        }
        if (target == this || target == master) {
            return false;
        }
        if (target.isPassengerOfSameVehicle(master)) {
            return false;
        }
        if (target instanceof IOwnable ownable && ownable.getMaster() == master) {
            return false;
        }
        return target.canBeSeenAsEnemy() && target.isAlive() && JUtils.canDamage(level().damageSources().mobAttack(master), target);
    }

    private void explode() {
        setDeltaMovement(0, 0, 0);
        hurtMarked = true;

        final Vec3 pos = position();
        final Set<LivingEntity> hurt = JUtils.generateHitbox(level(), pos, 2.25, e -> true);
        for (LivingEntity living : hurt) {
            if (!canAttack(living)) {
                continue;
            }
            final LivingEntity target = JUtils.getUserIfStand(living);
            final Vec3 kbVec = target.position().subtract(pos).normalize();
            damageLogic(level(), target, kbVec, 10, 1, false, 5f, true, 9,
                    level().damageSources().mobAttack(master), master, CommonHitPropertyComponent.HitAnimation.MID, false);
        }

        entityData.set(EXPLODED, true);

        playSound(SoundEvents.FIRECHARGE_USE, 1f, 1f);

        EXPLODE.sendForEntity(this);

        kill();
    }

    @Override
    public void tick() {
        super.tick();
        if (master == null) {
            kill();
        }
        if (hasExploded()) {
            return;
        }

        if (level().isClientSide) {
            level().addParticle(
                    ParticleTypes.FLAME,
                    this.getX() + random.nextFloat() - 0.5f,
                    this.getY() + random.nextFloat() - 0.5f,
                    this.getZ() + random.nextFloat() - 0.5f,
                    0.0, 0.0, 0.0);
        } else {
            if (target == null) {
                if (this.tickCount % 2 == 0) {
                    LivingEntity finalTarget = null;
                    final List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class,
                            getBoundingBox().inflate(32f), EntitySelector.ENTITY_STILL_ALIVE);

                    for (LivingEntity t :
                            targets) {
                        if (!canAttack(t)) {
                            continue;
                        }
                        if (finalTarget == null) {
                            finalTarget = t;
                            continue;
                        }
                        // Prioritise nearest
                        if (t.position().distanceToSqr(position()) < finalTarget.position().distanceToSqr(position())) {
                            finalTarget = t;
                        }
                    }

                    target = finalTarget;
                }
            } else if (target.isAlive()) {
                final Vec3 eyePos = target.getEyePosition();
                lookAt(EntityAnchorArgument.Anchor.EYES, eyePos);
                if (this.distanceToSqr(eyePos) < 2.5) {
                    explode(); //If closer than 1.58m
                }
            } else {
                target = null;
            }

            if (!hasExploded() && (this.tickCount >= 300 || getHealth() <= 0f)) {
                explode();
            }

            // Lerp velocity to simulate inertia
            setDeltaMovement(
                    getDeltaMovement()
                            .add(getLookAngle().scale(0.5))
                            .scale(0.25)
            );
            this.hurtMarked = true;
        }
    }

    public static final AzCommand EXPLODE = AzCommand.create(JCraft.BASE_CONTROLLER, "animation.detector.explode");

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NonNull DamageSource source) {
        return SoundEvents.LAVA_EXTINGUISH;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.LAVA_EXTINGUISH;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean startRiding(@NonNull Entity entity, boolean force) {
        return false;
    }

    public static AttributeSupplier.Builder createDetectorAttributes() {
        return createLivingAttributes() // This must be used instead of DefaultAttributeContainer.builder() due to compatibility with step-height-entity-attribute
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.KNOCKBACK_RESISTANCE)
                .add(Attributes.MOVEMENT_SPEED)
                .add(Attributes.ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS);
    }

    @Override
    protected @NonNull AABB makeBoundingBox() { // Centered around 0,0,0 instead of 0,0.5,0
        final double x = getX(), y = getY(), z = getZ();
        final double s = hasExploded() ? 0.1 : 0.5;
        return new AABB(x + s, y + s, z + s, x - s, y - s, z - s);
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        writeMasterNbt(tag);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        readMasterNbt(tag);
    }
}
