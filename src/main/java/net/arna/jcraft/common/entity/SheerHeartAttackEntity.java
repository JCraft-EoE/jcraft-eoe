package net.arna.jcraft.common.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.ai.goal.SHAAttackGoal;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JExplosionModifier;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JParticleTypeRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
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
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SheerHeartAttackEntity extends MobEntity implements GeoEntity, IOwnable {
    private static final TrackedData<Optional<UUID>> OWNER_ID = DataTracker.registerData(SheerHeartAttackEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
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

    public @Nullable UUID getOwnerId() {
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
    protected void applyDamage(DamageSource source, float amount) {
        if (source.isOf(DamageTypes.EXPLOSION)) return;
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

        if (getWorld().isClient()) {
            JCraft.getClientEntityHandler().sheerHeartAttackEntityTick(this);
            return;
        }

        if (master == null) {
            // Run every 2 seconds (player lists are rather expensive)
            if (age % 40 == 0) {
                // If the owner name is set, but the owner isn't (when loaded via NBT data), find owner
                UUID ownerId = getOwnerId();
                if (ownerId != null) {
                    ServerWorld serverWorld = (ServerWorld) getWorld();
                    for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.world(serverWorld)) {
                        if (serverPlayerEntity.getUuid().equals(ownerId))
                            master = serverPlayerEntity;
                    }
                }
            }

            LivingEntity attacking = getAttacking();
            if (attacking != null && canTarget(attacking))
                setTarget(attacking);
        } else {
            if (age % 19 == 0 && isOnGround() && getVelocity().lengthSquared() > 0.005)
                playSound(JSoundRegistry.SHA_TREAD, 0.5f, 1.0f);

            //50s is the cooldown period
            //15s is how long SHA can be out for
            if (age > 300 || !master.isAlive()) kill();

            Vec3d pos = getPos();
            LivingEntity target = getTarget();

            if (target == null) {
                if (this.age % 10 == 0) { // Entity lists are still expensive
                    List<LivingEntity> toTrack = getWorld().getEntitiesByClass(
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
            } else if (!canTarget(target)) setTarget(null);
        }
    }

    public void Explode() {
        JUtils.explode(getWorld(), this, getX(), getY(), getZ(), 1.8f,
                JExplosionModifier.builder().particle(JParticleTypeRegistry.BOOM_1)
                        .destructionType(
                        getWorld().getGameRules().getBoolean(JCraft.STAND_GRIEFING) ? Explosion.DestructionType.DESTROY : Explosion.DestructionType.KEEP)
                        .particleVelocity(Vec3d.ZERO)
                        .build());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<SheerHeartAttackEntity> state) {
        state.setAnimation(
                state.isMoving() ? RawAnimation.begin().thenLoop("animation.sha.walk") : RawAnimation.begin().thenLoop("animation.sha.idle")
        );
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
