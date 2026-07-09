package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;

public class KnifeProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Boolean> LIGHTNING;
    public boolean explosive = false;
    private int ticksInAir;
    private boolean delayed = false;
    private boolean delayFired = false;
    private int delayTime;

    static {
        LIGHTNING = SynchedEntityData.defineId(KnifeProjectile.class, EntityDataSerializers.BOOLEAN);
    }

    public KnifeProjectile(Level world) {
        super(JEntityTypeRegistry.KNIFE.get(), world);
    }

    public KnifeProjectile(Level world, LivingEntity owner) {
        super(JEntityTypeRegistry.KNIFE.get(), owner, world);
    }

    public Boolean getLightning() {
        return this.entityData.get(LIGHTNING);
    }

    public void setLightning(Boolean li) {
        this.entityData.set(LIGHTNING, li);
    }

    public void setDelayedLightning(int dt) {
        setLightning(true);
        delayed = true;
        delayTime = dt;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LIGHTNING, false);
    }

    @Override
    public @NonNull ItemStack getPickupItem() {
        return new ItemStack(JItemRegistry.KNIFE.get());
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.inGround) {
            ++this.ticksInAir;
        }

        if (!getLightning()) {
            if (ticksInAir > 640 && !level().isClientSide) {
                discard();
            }
            return;
        }

        // Lightning Knives

        if (level().isClientSide) {
            final double x = getX(), y = getY(), z = getZ();

            level().addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0, 0, 0);
            level().addParticle(ParticleTypes.ELECTRIC_SPARK, (x + xo) / 2, (y + yo) / 2, (z + zo) / 2, 0, 0, 0);
            return;
        }

        if (ticksInAir > 200 || inGround) {
            if (explosive) explode();
            discard();
        }
        if (!delayed) {
            return;
        }

        delayTime--;
        final Entity owner = getOwner();
        if (owner == null) {
            return;
        }

        if (delayTime >= 1) {
            setDeltaMovement(getDeltaMovement().scale(0.5));
            return;
        }

        if (delayFired) {
            return;
        }
        final Vec3 eP = owner.getEyePosition();
        final Vec3 rangeMod = owner.getLookAngle().scale(24);

        final EntityHitResult eHit = ProjectileUtil.getEntityHitResult(owner, eP, eP.add(rangeMod),
                owner.getBoundingBox().inflate(24),
                EntitySelector.LIVING_ENTITY_STILL_ALIVE, // This is a hack, and will miss on stuff like End Crystals, but also makes it miss on other knives
                576 // Squared
        );

        final HitResult hitResult = level().clip(new ClipContext(eP, eP.add(rangeMod), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));

        playSound(JSoundRegistry.TWOH_SHOOT.get(), 1, 1);

        Vec3 hitPos = hitResult.getLocation();
        if (eHit != null) {
            hitPos = eHit.getLocation();
        }
        setDeltaMovement(new Vec3(hitPos.x - getX(), hitPos.y - getY(), hitPos.z - getZ()).normalize());

        hasImpulse = true;
        delayFired = true;
    }

    private void explode() {
        final double x = getX(), y = getY(), z = getZ();

        JCraft.createParticle((ServerLevel) level(), x, y, z, JParticleType.FLASH);

        if (!isSilent()) {
            level().playSound(null, x, y, z, JSoundRegistry.TWOH_SHOOT.get(), SoundSource.PLAYERS, 0.5f, 0.5f);
        }

        final Entity owner = getOwner(); // Guaranteed non-null due to spawning conditions of explosive knives
        final Vec3 pos = position();
        final Set<LivingEntity> hurt = JUtils.generateHitbox(level(), pos, 1.5, e -> !e.isPassengerOfSameVehicle(owner));
        for (LivingEntity living : hurt) {
            final LivingEntity target = JUtils.getUserIfStand(living);
            final Vec3 kbVec = target.position().subtract(pos).normalize();
            damageLogic(level(), target, kbVec, 10, 1, false, 1f, true, 9,
                    level().damageSources().indirectMagic(owner, this), getOwner(), CommonHitPropertyComponent.HitAnimation.MID, false);
        }
    }

    @Override
    public void thunderHit(@NonNull ServerLevel world, @NonNull LightningBolt lightning) {
        this.setLightning(true);
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult entityHitResult) {
        if (level().isClientSide) {
            return;
        }
        if (delayed && delayTime > 1) {
            return;
        }
        Entity entity = entityHitResult.getEntity();
        Entity owner = this.getOwner();

        if (owner != null && owner.hasPassenger(entity) || entity == owner) {
            return;
        }

        if (isOnFire()) {
            entity.setSecondsOnFire(5);
        }

        int blockstun = 4;
        int stunT = 0;
        if (getLightning()) {
            stunT = 20;
            blockstun = 6;
        } else {
            spawnAtLocation(getPickupItem(), 0.1F);
        }

        if (explosive) explode();

        JUtils.projectileDamageLogic(this, level(), entity, Vec3.ZERO, stunT, 1, false, 2,
                blockstun, CommonHitPropertyComponent.HitAnimation.MID, false, false, getLightning());
        playSound(SoundEvents.TRIDENT_HIT, 1, 1);
        if (entity instanceof LivingEntity living) {
            JComponentPlatformUtils.getMiscData(living).stab();
        }
        discard();
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("life", (short) this.ticksInAir);
        tag.putBoolean("lightning", getLightning());
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ticksInAir = tag.getShort("life");
        setLightning(tag.getBoolean("lightning"));
    }
}
