package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.minecraft.core.particles.ParticleTypes;

import static net.arna.jcraft.api.Attacks.damageLogic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public class WindGustEntity extends JAttackEntity {

    public static final EntityDataAccessor<Boolean> LARGE =
            SynchedEntityData.defineId(WindGustEntity.class, EntityDataSerializers.BOOLEAN);

    private static final double SPEED = 1.8;
    private static final int LIFETIME_SMALL = 10;
    private static final int LIFETIME_LARGE = 15;
    private static final float RADIUS_SMALL = 1.2f;
    private static final float RADIUS_LARGE = 2.5f;

    private Vec3 velocity = Vec3.ZERO;
    private float damage = 1.5f;
    private int stun = 8;
    private final Set<Integer> hit = new HashSet<>();

    public WindGustEntity(Level world) {
        super(JEntityTypeRegistry.WIND_GUST.get(), world);
        setNoGravity(true);
    }

    public void setVelocity(Vec3 vel) {
        this.velocity = vel.normalize().scale(SPEED);
    }

    public void setDamageValues(float damage, int stun) {
        this.damage = damage;
        this.stun = stun;
    }

    public void setLarge(boolean large) {
        entityData.set(LARGE, large);
    }

    public boolean isLarge() {
        return entityData.get(LARGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(LARGE, false);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            spawnTrailParticles();
            return;
        }

        if (tickCount > (isLarge() ? LIFETIME_LARGE : LIFETIME_SMALL) || master == null) {
            discard();
            return;
        }

        setPos(getX() + velocity.x, getY() + velocity.y, getZ() + velocity.z);

        if (tickCount % 2 == 0) {
            damageEntities();
        }
    }

    private void damageEntities() {
        final float radius = isLarge() ? RADIUS_LARGE : RADIUS_SMALL;
        final AABB box = AABB.ofSize(position(), radius * 2, radius * 2, radius * 2);
        final LivingEntity user = master;

        level().getEntitiesOfClass(LivingEntity.class, box,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e ->
                        e != user && e != getBaseEntity() && !hit.contains(e.getId())))
                .forEach(target -> {
                    hit.add(target.getId());
                    final Vec3 kb = velocity.normalize().scale(0.35);
                    damageLogic(level(), target, kb, stun, 1, false, damage, false, 3,
                            level().damageSources().mobAttack(master), master,
                            CommonHitPropertyComponent.HitAnimation.MID);
                    level().playSound(null, target.blockPosition(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, target.getSoundSource(), 0.6f, 1.4f);
                });
    }

    private void spawnTrailParticles() {
        final boolean large = isLarge();
        final int count = large ? 6 : 3;
        for (int i = 0; i < count; i++) {
            final double spread = large ? 1.0 : 0.4;
            level().addParticle(ParticleTypes.CLOUD,
                    getX() + random.nextGaussian() * spread,
                    getY() + random.nextGaussian() * spread,
                    getZ() + random.nextGaussian() * spread,
                    -velocity.x * 0.1, -velocity.y * 0.1, -velocity.z * 0.1);
            level().addParticle(ParticleTypes.POOF,
                    getX() + random.nextGaussian() * spread * 0.5,
                    getY() + random.nextGaussian() * spread * 0.5,
                    getZ() + random.nextGaussian() * spread * 0.5,
                    0, 0.02, 0);
        }
    }

    public LivingEntity getBaseEntity() {
        return master;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return !source.is(DamageTypes.FELL_OUT_OF_WORLD) && !source.is(DamageTypes.GENERIC_KILL);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void doPush(@NonNull Entity entity) {}

    @Override
    public void push(@NonNull Entity entity) {}

    @Override
    public boolean canCollideWith(@NonNull Entity other) {
        return false;
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        writeMasterNbt(tag);
        tag.putDouble("VelX", velocity.x);
        tag.putDouble("VelY", velocity.y);
        tag.putDouble("VelZ", velocity.z);
        tag.putBoolean("Large", isLarge());
        tag.putFloat("Damage", damage);
        tag.putInt("Stun", stun);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        readMasterNbt(tag);
        velocity = new Vec3(tag.getDouble("VelX"), tag.getDouble("VelY"), tag.getDouble("VelZ"));
        setLarge(tag.getBoolean("Large"));
        damage = tag.getFloat("Damage");
        stun = tag.getInt("Stun");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1)
                .add(Attributes.KNOCKBACK_RESISTANCE)
                .add(Attributes.MOVEMENT_SPEED)
                .add(Attributes.ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS);
    }
}
