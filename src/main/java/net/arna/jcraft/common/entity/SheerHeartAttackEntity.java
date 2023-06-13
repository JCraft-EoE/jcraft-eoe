package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.entity.ai.goal.SHAAttackGoal;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
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

public class SheerHeartAttackEntity extends MobEntity implements IAnimatable, IAnimationTickable {
    AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    public SheerHeartAttackEntity(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    private LivingEntity owner;
    private static final TrackedData<String> OWNERNAME = DataTracker.registerData(SheerHeartAttackEntity.class, TrackedDataHandlerRegistry.STRING);

    public LivingEntity getOwner() {
        return this.owner;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        this.setOwnerName(owner.getName().getString());
    }

    public String getOwnerName() {
        return this.dataTracker.get(OWNERNAME);
    }

    public void setOwnerName(String state) {
        this.dataTracker.set(OWNERNAME, state);
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(OWNERNAME, "%unset_owner_name");
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
        nbt.putString("OwnerName", this.getOwnerName());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.setOwnerName(nbt.getString("OwnerName"));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.owner != null && amount < 256) { // 256 value is arbitrary, to stop /kill from also killing the owner
            this.owner.damage(source, amount / 4); // Reflect damage to owner (SHA is the right hand of KQ)
        }
        return super.damage(source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        boolean client = this.world.isClient();
        if (!client) {
            if (this.owner == null) {
                // Run every 2 seconds (player lists are rather expensive)
                if (this.age % 40 == 0) {
                    // If the owner name is set, but the owner isn't (when loaded via NBT data), find owner
                    String ownerName = this.getOwnerName();
                    if (!ownerName.equals("%unset_owner_name")) {
                        ServerWorld serverWorld = (ServerWorld) this.world;
                        for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.world(serverWorld)) {
                            if (serverPlayerEntity.getName().getString().equals(ownerName)) {
                                this.owner = serverPlayerEntity;
                            }
                        }
                    }
                }

                this.setTarget(this.getAttacking());
            } else {
                //45s is the cooldown period
                //18s is how long SHA can be out for
                if (this.age > 360 || !this.owner.isAlive()) {
                    this.kill();
                }

                Vec3d pos = this.getPos();
                LivingEntity target = this.getTarget();

                if (target == null) {
                    if (this.age % 10 == 0) { // Entity lists are still expensive
                        List<LivingEntity> toTrack = world.getEntitiesByClass(
                                LivingEntity.class,
                                new Box(pos.add(16, 16, 16), pos.add(-16, -16, -16)),
                                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR
                        );

                        toTrack.remove(this);
                        toTrack.remove(this.owner);

                        LivingEntity coldTarget = null;
                        LivingEntity hotTarget = null;

                        for (LivingEntity living : toTrack) {
                            if (!this.canTarget(living)) {
                                continue;
                            }
                            if (living.hasVehicle() && living.getVehicle() == this.owner) {
                                continue;
                            }

                            // Prioritize heat
                            if (living.isOnFire()) {
                                this.setTarget(living);
                                coldTarget = null;
                                hotTarget = null;
                                break;
                            }
                            // Discourage undead (cold)
                            if (coldTarget == null || hotTarget == null) {
                                if (living.isUndead()) {
                                    coldTarget = living;
                                } else {
                                    hotTarget = living;
                                }
                            }
                        }

                        if (hotTarget != null) {
                            this.setTarget(hotTarget);
                        } else if (coldTarget != null) {
                            this.setTarget(coldTarget);
                        }
                    }
                } else if (!this.canTarget(this.getTarget())) {
                    this.setTarget(null);
                }
            }
        }
    }

    public void Explode() {
        world.createExplosion(this, this.getX(), this.getY() + this.getHeight() / 2, this.getZ(), 2f, Explosion.DestructionType.NONE);
    }
}
