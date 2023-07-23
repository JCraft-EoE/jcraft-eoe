package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.entity.ai.goal.SHAAttackGoal;
import net.arna.jcraft.common.util.IOwnable;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SheerHeartAttackEntity extends MobEntity implements IAnimatable, IAnimationTickable, IOwnable {
    private static final TrackedData<Optional<UUID>> OWNER_ID = DataTracker.registerData(SheerHeartAttackEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private final AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);
    private LivingEntity master;

    public SheerHeartAttackEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public LivingEntity getMaster() {
        return this.master;
    }

    @Override
    public void setMaster(LivingEntity owner) {
        this.master = owner;
        setOwnerId(owner.getUuid());
    }

    private UUID getOwnerId() {
        return this.dataTracker.get(OWNER_ID).orElse(null);
    }

    private void setOwnerId(UUID id) {
        this.dataTracker.set(OWNER_ID, Optional.of(id));
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(OWNER_ID, Optional.empty());
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new SHAAttackGoal(this, 1.5));
        this.goalSelector.add(3, new LookAtEntityGoal(this, LivingEntity.class, 32.0F));
        this.goalSelector.add(4, new LookAroundGoal(this));
        this.targetSelector.add(6, new PounceAtTargetGoal(this, 0.2f));
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

    @Override
    public int tickTimer() {
        return age;
    }

    @SuppressWarnings("SameReturnValue")
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(
                event.isMoving() ? new AnimationBuilder().loop("animation.sha.walk") : new AnimationBuilder().loop("animation.sha.idle")
        );
        return PlayState.CONTINUE;
    }

    @Override
    protected void applyDamage(DamageSource source, float amount) {
        if (source.isExplosive()) return;
        super.applyDamage(source, amount);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putUuid("Owner", getOwnerId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        setOwnerId(nbt.getUuid("Owner"));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        // 256 value is arbitrary, to stop /kill from also killing the owner
        if (master != null && amount < 256)
            master.damage(source, amount / 4); // Reflect damage to owner (SHA is the right hand of KQ)
        return super.damage(source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient()) return;

        if (master == null) {
            // Run every 2 seconds (player lists are rather expensive)
            if (age % 40 == 0) {
                // If the owner name is set, but the owner isn't (when loaded via NBT data), find owner
                UUID ownerId = getOwnerId();
                if (ownerId != null) {
                    ServerWorld serverWorld = (ServerWorld) world;
                    for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.world(serverWorld)) {
                        if (serverPlayerEntity.getUuid().equals(ownerId))
                            master = serverPlayerEntity;
                    }
                }
            }

            setTarget(getAttacking());
        } else {
            //45s is the cooldown period
            //18s is how long SHA can be out for
            if (age > 360 || !master.isAlive()) kill();

            Vec3d pos = getPos();
            LivingEntity target = getTarget();

            if (target == null) {
                if (this.age % 10 == 0) { // Entity lists are still expensive
                    List<LivingEntity> toTrack = world.getEntitiesByClass(
                            LivingEntity.class,
                            new Box(pos.add(16, 16, 16), pos.add(-16, -16, -16)),
                            EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != this && e != master)
                    );

                    LivingEntity coldTarget = null;
                    LivingEntity hotTarget = null;

                    for (LivingEntity living : toTrack) {
                        if (!canTarget(living)) continue;
                        if (living.hasVehicle() && living.getVehicle() == master) continue;

                        // Prioritize heat
                        if (living.isOnFire()) {
                            setTarget(living);
                            coldTarget = null;
                            hotTarget = null;
                            break;
                        }

                        // Discourage undead (cold)
                        if (coldTarget == null || hotTarget == null) {
                            if (living.isUndead()) coldTarget = living;
                            else hotTarget = living;
                        }
                    }

                    if (hotTarget != null) setTarget(hotTarget);
                    else if (coldTarget != null) setTarget(coldTarget);
                }
            } else if (!canTarget(getTarget())) setTarget(null);
        }
    }

    public void Explode() {
        world.createExplosion(this, this.getX(), this.getY() + this.getHeight() / 2, this.getZ(), 2f, Explosion.DestructionType.NONE);
    }
}
