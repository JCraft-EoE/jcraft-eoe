package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import static net.arna.jcraft.api.Attacks.damageLogic;

public class BloodProjectile extends AbstractArrow {
    public BloodProjectile(final Level world) {
        super(JEntityTypeRegistry.BLOOD_PROJECTILE.get(), world);
        this.pickup = Pickup.DISALLOWED;
    }

    public BloodProjectile(final Level world, final LivingEntity owner) {
        super(JEntityTypeRegistry.BLOOD_PROJECTILE.get(), owner, world);
        this.setSoundEvent(SoundEvents.SLIME_BLOCK_FALL);
    }

    @Override
    protected void tickDespawn() {
        discard();
    }

    @Override
    protected void onHitEntity(final @NonNull EntityHitResult entityHitResult) {
        if (level().isClientSide) {
            return;
        }
        final Entity owner = getOwner();
        if (owner == null) {
            return;
        }
        final Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner) {
            return;
        }

        if (entity instanceof LivingEntity living) {
            final LivingEntity target = JUtils.getUserIfStand(living);
            damageLogic(level(), target, Vec3.ZERO, 10, 1, false, 2f,
                    false, 6, level().damageSources().thrown(this, owner), owner, CommonHitPropertyComponent.HitAnimation.MID);
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, true));
            discard();
        }

        if (entity instanceof EndCrystal endCrystal) {
            endCrystal.hurt(level().damageSources().thrown(this, owner), 2f);
        }

        playSound(SoundEvents.SLIME_BLOCK_FALL, 1, 0.5f);
    }

    @Override
    public @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }

}
