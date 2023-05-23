package net.arna.jcraft.common.entity;

import net.arna.jcraft.registry.JEntityTypeRegister;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class BubbleProjectile extends PersistentProjectileEntity implements IAnimatable {

    private int ticksInAir = 0;

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public BubbleProjectile(EntityType<? extends BubbleProjectile> entityType, World world) {
        super(entityType, world);
        this.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
    }

    public BubbleProjectile(World world, LivingEntity owner) {
        super(JEntityTypeRegister.BUBBLE, owner, world);
        this.setOwner(owner);
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().loop("animation.bubble.idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public ItemStack asItemStack() {
        return new ItemStack(Items.AIR);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();

        if (type == HitResult.Type.BLOCK) {
            this.onBlockHit((BlockHitResult) hitResult);
            this.emitGameEvent(GameEvent.PROJECTILE_LAND, this.getOwner());
            this.discard();
        }
    }

    public boolean isInGround() {
        return inGround;
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isClient()) {
            if (this.getOwner() == null || this.age > 1600)
                this.discard();
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        tag.putShort("life", (short) this.ticksInAir);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        this.ticksInAir = tag.getShort("life");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean shouldRender(double distance) {
        return true;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP;
    }
}
