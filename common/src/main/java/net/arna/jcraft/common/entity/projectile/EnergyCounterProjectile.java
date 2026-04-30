package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;

import static net.arna.jcraft.api.Attacks.damageLogic;

public class EnergyCounterProjectile extends AbstractArrow {
    private static final double SPEED = 0.75;
    private static final double HOME_STRENGTH = 0.15;

    private float reflectedDamage;
    private Vec3 spawnPos = Vec3.ZERO;
    private WeakReference<Entity> target = new WeakReference<>(null);
    private boolean hitSoundPlayed = false;

    public EnergyCounterProjectile(Level world) {
        super(JEntityTypeRegistry.ENERGY_COUNTER.get(), world);
        setNoGravity(true);
    }

    public EnergyCounterProjectile(Level world, LivingEntity owner, float reflectedDamage, Vec3 direction, Entity target) {
        super(JEntityTypeRegistry.ENERGY_COUNTER.get(), owner, world);
        setNoGravity(true);
        this.reflectedDamage = reflectedDamage;
        this.spawnPos = position();
        this.target = new WeakReference<>(target);
        setDeltaMovement(direction.normalize().scale(SPEED));
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    JParticleTypeRegistry.NAIL_TRAIL.get(),
                    getX(), getY(), getZ(),
                    2, 0, 0, 0, 0
            );

            Entity t = target.get();
            if (t != null && t.isAlive()) {
                Vec3 toTarget = t.position().add(0, t.getBbHeight() * 0.5, 0).subtract(position()).normalize();
                Vec3 current = getDeltaMovement();
                Vec3 steered = current.add(toTarget.scale(HOME_STRENGTH)).normalize().scale(SPEED);
                setDeltaMovement(steered);
            }
        }

        if (position().distanceTo(spawnPos) > 10.0) {
            discard();
        }
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult blockHitResult) {
        if (!level().isClientSide()) {
            level().playSound(null, getX(), getY(), getZ(), JSoundRegistry.CMOON_BLOCKHALT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            hitSoundPlayed = true;
        }
        discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (!(entity instanceof LivingEntity living)) {
            discard();
            return;
        }
        if (!level().isClientSide() && level() instanceof ServerLevel serverLevel) {
            JCraft.createHitsparks(serverLevel, getX(), getY(), getZ(), JParticleType.HIT_SPARK_1, 3, 0.3);
            level().playSound(null, getX(), getY(), getZ(), JSoundRegistry.IMPACT_4.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            hitSoundPlayed = true;
            Entity owner = getOwner();
            DamageSource source = level().damageSources().thrown(this, owner);
            AttackData attackData = new AttackData(
                    getDeltaMovement().normalize(),
                    5, 1, false,
                    reflectedDamage, true, 10,
                    source, owner,
                    CommonHitPropertyComponent.HitAnimation.MID,
                    null, false, false, false
            );
            damageLogic(level(), living, attackData);
            discard();
        }
    }

    @Override
    public void remove(@NonNull RemovalReason reason) {
        if (!level().isClientSide() && reason == RemovalReason.DISCARDED && !hitSoundPlayed) {
            level().playSound(null, getX(), getY(), getZ(), JSoundRegistry.TCB_POP.get(), SoundSource.PLAYERS, 0.6f, 1.2f);
        }
        super.remove(reason);
    }

    @Override
    protected void tickDespawn() {
        discard();
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putFloat("ReflectedDamage", reflectedDamage);
        nbt.putDouble("SpawnX", spawnPos.x);
        nbt.putDouble("SpawnY", spawnPos.y);
        nbt.putDouble("SpawnZ", spawnPos.z);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        reflectedDamage = nbt.getFloat("ReflectedDamage");
        spawnPos = new Vec3(nbt.getDouble("SpawnX"), nbt.getDouble("SpawnY"), nbt.getDouble("SpawnZ"));
    }

    @Override
    protected @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }
}
