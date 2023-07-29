package net.arna.jcraft.common.entity;

import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import static net.arna.jcraft.common.entity.StandEntity.damageLogic;

public class BloodProjectile extends PersistentProjectileEntity implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public BloodProjectile(EntityType<? extends BloodProjectile> entityType, World world) {
        super(entityType, world);
        this.pickupType = PickupPermission.DISALLOWED;
    }

    public BloodProjectile(World world, LivingEntity owner) {
        super(JEntityTypeRegistry.BLOOD_PROJECTILE, owner, world);
        this.setSound(SoundEvents.BLOCK_SLIME_BLOCK_FALL);
        this.setOwner(owner);
    }

    @Override
    protected void age() {
        discard();
    } // Disappear instantly upon contact with the ground

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (world.isClient) return;
        Entity owner = getOwner();
        if (owner == null) return;
        Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner) return;

        if (entity instanceof LivingEntity living) {
            LivingEntity target = living;
            if (entity instanceof StandEntity<?, ?> stand && stand.hasUser())
                target = stand.getUserOrThrow();
            damageLogic(world, target, Vec3d.ZERO, 10, 1, false, 2f,
                    false, 6, DamageSource.thrownProjectile(this, owner), owner);
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0, false, true));
            discard();
        }

        if (entity instanceof EndCrystalEntity endCrystal)
            endCrystal.damage(DamageSource.thrownProjectile(this, owner), 2f);

        playSound(SoundEvents.BLOCK_SLIME_BLOCK_FALL, 1, 0.5f);
    }

    @Override
    public ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean hasNoGravity() {
        return false;
    }

    // Animations
    @Override
    public void registerControllers(AnimationData data) {
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
