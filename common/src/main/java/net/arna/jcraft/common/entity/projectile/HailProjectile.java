package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class HailProjectile extends AbstractArrow {
    private int ticksInAir;
    private float damage = 1.5f;

    public HailProjectile(Level world) {
        super(JEntityTypeRegistry.HAIL.get(), world);
    }

    public HailProjectile(Level world, LivingEntity owner) {
        super(JEntityTypeRegistry.HAIL.get(), owner, world);
        setSoundEvent(SoundEvents.GLASS_BREAK);
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
            level().addParticle(ParticleTypes.SNOWFLAKE,
                    getX(), getY(), getZ(),
                    getDeltaMovement().x * 0.1,
                    getDeltaMovement().y * 0.1,
                    getDeltaMovement().z * 0.1);
        }
        if (!level().isClientSide && ticksInAir > 100) {
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
                12, 1, false, damage, 2,
                CommonHitPropertyComponent.HitAnimation.MID);
        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, true, true));
            living.setTicksFrozen(Math.max(living.getTicksFrozen(), 40));
        }
        playSound(SoundEvents.GLASS_BREAK, 0.8f, 1.2f);
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
