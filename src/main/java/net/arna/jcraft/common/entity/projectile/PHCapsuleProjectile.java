package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.PurpleHazeCloudEntity;
import net.arna.jcraft.common.entity.stand.AbstractPurpleHazeEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class PHCapsuleProjectile extends PersistentProjectileEntity implements IAnimatable {
    private AbstractPurpleHazeEntity.PoisonType poisonType;

    public PHCapsuleProjectile(World world) {
        super(JEntityTypeRegistry.PH_CAPSULE, world);
    }

    public PHCapsuleProjectile(LivingEntity owner, World world, AbstractPurpleHazeEntity.PoisonType poisonType) {
        super(JEntityTypeRegistry.PH_CAPSULE, owner, world);
        this.poisonType = poisonType;
    }

    @Override
    protected ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        if (world.isClient())
            return;
        if (hitResult.getType() == HitResult.Type.MISS)
            return;
        if (hitResult instanceof EntityHitResult entityHitResult) {
            if (getOwner() != null && entityHitResult.getEntity().isConnectedThroughVehicle(getOwner())) return;

            JUtils.projectileDamageLogic(this, world, entityHitResult.getEntity(), getVelocity().multiply(0.1),
                    2, 1, false, 2f, 2, HitPropertyComponent.HitAnimation.MID);
        }

        discard();
        PurpleHazeCloudEntity cloud = new PurpleHazeCloudEntity(world, 2.0f, poisonType);
        cloud.copyPositionAndRotation(this);
        world.spawnEntity(cloud);
    }

    // Animations
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    @Override
    public void registerControllers(AnimationData data) {
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
