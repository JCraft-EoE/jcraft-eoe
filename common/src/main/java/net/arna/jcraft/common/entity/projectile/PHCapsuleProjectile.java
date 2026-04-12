package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.entity.PurpleHazeCloudEntity;
import net.arna.jcraft.common.entity.stand.AbstractPurpleHazeEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class PHCapsuleProjectile extends AbstractArrow {
    private final AbstractPurpleHazeEntity.PoisonType poisonType;

    public PHCapsuleProjectile(Level world) {
        super(JEntityTypeRegistry.PH_CAPSULE.get(), world);
        this.poisonType = AbstractPurpleHazeEntity.PoisonType.HARMING;
    }

    public PHCapsuleProjectile(LivingEntity owner, Level world, AbstractPurpleHazeEntity.PoisonType poisonType) {
        super(JEntityTypeRegistry.PH_CAPSULE.get(), owner, world);
        this.poisonType = poisonType;
    }

    @Override
    protected @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHit(@NonNull HitResult hitResult) {
        if (level().isClientSide()) {
            return;
        }
        if (hitResult.getType() == HitResult.Type.MISS) {
            return;
        }
        if (hitResult instanceof EntityHitResult entityHitResult) {
            if (getOwner() != null && entityHitResult.getEntity().isPassengerOfSameVehicle(getOwner())) {
                return;
            }

            JUtils.projectileDamageLogic(this, level(), entityHitResult.getEntity(), getDeltaMovement().scale(0.1),
                    2, 1, false, 2f, 2, CommonHitPropertyComponent.HitAnimation.MID);
        }

        final PurpleHazeCloudEntity cloud = new PurpleHazeCloudEntity(level(), 2.0f, poisonType);
        cloud.copyPosition(this);
        level().addFreshEntity(cloud);
        discard();
    }

}
