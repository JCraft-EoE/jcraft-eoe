package net.arna.jcraft.common.entity.projectile;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;
import static net.arna.jcraft.common.util.JUtils.canDamage;

public class BisectProjectile extends AbstractArrow {
    private final IntOpenHashSet hit = new IntOpenHashSet(8);

    private static final EntityDataAccessor<Float> SCALE;
    static {
        SCALE = SynchedEntityData.defineId(BisectProjectile.class, EntityDataSerializers.FLOAT);
    }

    public BisectProjectile(final Level level) {
        super(JEntityTypeRegistry.BISECT.get(), level);
        setNoGravity(true);
    }

    public BisectProjectile(final Level level, final LivingEntity owner) {
        super(JEntityTypeRegistry.BISECT.get(), owner, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SCALE, 1.0f);
    }

    public void setScale(final float scale) {
        entityData.set(SCALE, scale);
    }
    public float getScale() {
        return entityData.get(SCALE);
    }

    @Override
    protected void tickDespawn() {
        discard();
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            return;
        }

        final Vec3 curPos = position();
        final Entity owner = getOwner();
        final float scale = getScale();

        if (owner instanceof LivingEntity livingOwner) {
            final Set<Entity> filter = new HashSet<>(2);
            filter.add(owner);
            filter.add(this);
            if (owner.isVehicle()) {
                filter.addAll(owner.getPassengers());
            }
            final DamageSource damageSource = level().damageSources().mobAttack(livingOwner);
            // Recursive hitbox check between current and previous position
            final Vec3 towardsVec = curPos.subtract(new Vec3(xo, yo, zo));
            //todo: support other gravs?
            final float yaw = (getYRot() + 90.0f) * JUtils.DEG_TO_RAD;
            final Vec3 sideVec = new Vec3(
                    Mth.sin(yaw), 0, Mth.cos(yaw)
            );
            final Set<LivingEntity> hurtAll = new HashSet<>();
            for (double i = 0; i < 2; i++) {
                hurtAll.addAll(
                        JUtils.generateHitbox(level(), curPos.add(towardsVec.scale(i / 2)), 1.0 * scale, filter)
                );
                hurtAll.addAll(
                        JUtils.generateHitbox(level(), curPos.add(towardsVec.scale(i / 2)).add(sideVec.scale(scale)), 1.0 * scale, filter)
                );
                hurtAll.addAll(
                        JUtils.generateHitbox(level(), curPos.add(towardsVec.scale(i / 2)).subtract(sideVec.scale(scale)), 1.0 * scale, filter)
                );
            }

            hurtAll.removeIf(e -> !canDamage(damageSource, e));
            hurtAll.removeIf(e -> hit.contains(e.getId()));

            if (!hurtAll.isEmpty()) {
                for (LivingEntity l : hurtAll) {
                    final LivingEntity target = JUtils.getUserIfStand(l);
                    damageLogic(level(), target, getDeltaMovement(), (int) (10 * scale), 3, false, 5f * scale,
                            true, 0, damageSource, owner, CommonHitPropertyComponent.HitAnimation.LAUNCH, false, true);
                    hit.add(target.getId());
                }
                JCraft.createParticle((ServerLevel) this.level(),
                        curPos.x + random.nextGaussian() * 0.25,
                        curPos.y + random.nextGaussian() * 0.25,
                        curPos.z + random.nextGaussian() * 0.25,
                        JParticleType.HIT_SPARK_3);
                playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1, 0.5f);
            }

            if (tickCount > 240) discard();
        }
    }

    @Override
    protected void onHit(final @NonNull HitResult result) {

    }

    @Override
    protected @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    // Animations
    /*
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private static final RawAnimation FIRE = RawAnimation.begin().thenPlayAndHold("animation.bisect.spawn");
    private PlayState predicate(AnimationState<BisectProjectile> state) {
        return state.setAndContinue(FIRE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }*/
}
