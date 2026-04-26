package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class ElectricBoltProjectile extends AbstractArrow {
    private int ticksInAir;
    private float damage = 2.0f;

    public ElectricBoltProjectile(Level world) {
        super(JEntityTypeRegistry.ELECTRIC_BOLT.get(), world);
    }

    public ElectricBoltProjectile(Level world, LivingEntity owner) {
        super(JEntityTypeRegistry.ELECTRIC_BOLT.get(), owner, world);
        setSoundEvent(SoundEvents.LIGHTNING_BOLT_THUNDER);
        setNoGravity(true);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void tickDespawn() {
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        if (!inGround) {
            ++ticksInAir;
        }
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    getX(), getY(), getZ(),
                    random.nextGaussian() * 0.05,
                    random.nextGaussian() * 0.05,
                    random.nextGaussian() * 0.05);
            level().addParticle(ParticleTypes.FLASH,
                    getX(), getY(), getZ(), 0, 0, 0);
        }
        if (!level().isClientSide && ticksInAir > 200) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult entityHitResult) {
        if (level().isClientSide) return;
        Entity entity = entityHitResult.getEntity();
        Entity owner = getOwner();
        if (owner != null && (owner.hasPassenger(entity) || entity == owner)) return;
        if (entity instanceof JAttackEntity attackEntity && attackEntity.getMaster() == owner) return;

        JUtils.projectileDamageLogic(this, level(), entity,
                getDeltaMovement().normalize().scale(0.1),
                15, 1, false, damage, 3,
                CommonHitPropertyComponent.HitAnimation.MID);
        playSound(SoundEvents.LIGHTNING_BOLT_IMPACT, 0.5f, 1.5f);
        discard();
    }

    @Override
    protected float getWaterInertia() {
        return 0.95F;
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("life", (short) ticksInAir);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ticksInAir = tag.getShort("life");
    }
}
