package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.entity.ai.goal.SHAAttackGoal;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JExplosionModifier;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SheerHeartAttackEntity extends Mob implements IOwnable {
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(SheerHeartAttackEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private LivingEntity master;

    public SheerHeartAttackEntity(Level world) {
        super(JEntityTypeRegistry.SHEER_HEART_ATTACK.get(), world);
    }

    @Override
    public LivingEntity getMaster() {
        return this.master;
    }

    @Override
    public void setMaster(LivingEntity owner) {
        this.master = owner;
        setOwnerId(owner.getUUID());
    }

    public @Nullable UUID getOwnerId() {
        return this.entityData.get(OWNER_ID).orElse(null);
    }

    private void setOwnerId(UUID id) {
        this.entityData.set(OWNER_ID, Optional.of(id));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OWNER_ID, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SHAAttackGoal(this, 1.5));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, LivingEntity.class, 32.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(6, new LeapAtTargetGoal(this, 0.2f));
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.EXPLOSION)) {
            return;
        }

        super.actuallyHurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        UUID ownerId = getOwnerId();

        if (ownerId != null)
            nbt.putUUID("Owner", ownerId);
        else nbt.remove("Owner");
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        setOwnerId(nbt.getUUID("Owner"));
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        // 256 value is arbitrary, to stop /kill from also killing the owner
        if (master != null && amount < 256) {
            master.hurt(source, amount / 4); // Reflect damage to owner (SHA is the right hand of KQ)
        }
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            JCraft.getClientEntityHandler().sheerHeartAttackEntityTick(this);
            return;
        }

        if (master == null) {
            // Run every 2 seconds (player lists are rather expensive)
            if (tickCount % 40 == 0) {
                // If the owner name is set, but the owner isn't (when loaded via NBT data), find owner
                final UUID ownerId = getOwnerId();
                if (ownerId != null) {
                    ServerLevel serverWorld = (ServerLevel) level();
                    master = serverWorld.getServer().getPlayerList().getPlayer(ownerId);
                }
            }

            final LivingEntity attacking = getLastHurtMob();
            if (attacking != null && canAttack(attacking)) {
                setTarget(attacking);
            }
        } else {
            if (tickCount % 19 == 0 && onGround() && getDeltaMovement().lengthSqr() > 0.005) {
                playSound(JSoundRegistry.SHA_TREAD.get(), 0.5f, 1.0f);
            }

            //50s is the cooldown period
            //15s is how long SHA can be out for
            if (tickCount > 300 || !master.isAlive()) {
                kill();
            }

            final Vec3 pos = position();
            final LivingEntity target = getTarget();

            if (target == null) {
                if (this.tickCount % 10 == 0) { // Entity lists are still expensive
                    final List<LivingEntity> toTrack = level().getEntitiesOfClass(
                            LivingEntity.class,
                            new AABB(pos.add(16, 16, 16), pos.add(-16, -16, -16)),
                            EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != this && e != master)
                    );

                    LivingEntity coldTarget = null;
                    LivingEntity hotTarget = null;

                    for (final LivingEntity living : toTrack) {
                        if (!canAttack(living)) {
                            continue;
                        }
                        if (living.isPassenger() && living.getVehicle() == master) {
                            continue;
                        }

                        // Prioritize heat
                        if (living.isOnFire()) {
                            setTarget(living);
                            coldTarget = null;
                            hotTarget = null;
                            break;
                        }

                        // Discourage undead (cold)
                        if (coldTarget == null || hotTarget == null) {
                            if (living.isInvertedHealAndHarm()) {
                                coldTarget = living;
                            } else {
                                hotTarget = living;
                            }
                        }
                    }

                    if (hotTarget != null) {
                        setTarget(hotTarget);
                    } else if (coldTarget != null) {
                        setTarget(coldTarget);
                    }
                }
            } else if (!canAttack(target)) {
                setTarget(null);
            }
        }
    }

    public void explode() {
        JUtils.explode(level(), this, getX(), getY(), getZ(), 1.8f,
                JExplosionModifier.builder().particle(JParticleTypeRegistry.BOOM_1.get())
                        .blockInteraction(AbstractMove.mayBreak(level(), master, blockPosition(), null)
                                ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP)
                        .particleVelocity(Vec3.ZERO)
                        .build());
    }

    // Animations
    /*private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }*/

//    public static final AzCommand SHA_WALK = AzCommand.create("base_controller", "animation.sha.walk", AzPlayBehaviors.LOOP);
//    public static final AzCommand SHA_IDLE = AzCommand.create("base_controller", "animation.sha.idle", AzPlayBehaviors.LOOP);

    /*private PlayState predicate(AnimationState<SheerHeartAttackEntity> state) {
        state.setAnimation(
                state.isMoving() ? SHA_WALK : SHA_IDLE
        );
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }*/
}
