package net.arna.jcraft.entity;

import net.arna.jcraft.registry.ModEntityRegister;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import static net.arna.jcraft.entity.StandEntity.DamageLogic;

public class BloodProjectile extends PersistentProjectileEntity implements IAnimatable {
    private int ticksInAir;
    private AnimationFactory factory = new AnimationFactory(this);

    public BloodProjectile(EntityType<? extends BloodProjectile> entityType, World world) {
        super(entityType, world);
        this.pickupType = PickupPermission.DISALLOWED;
    }

    public BloodProjectile(World world, LivingEntity owner) {
        super(ModEntityRegister.BLOODPROJECTILE, owner, world);
        this.setSound(SoundEvents.ITEM_BUCKET_EMPTY);
        this.setOwner(owner);
    }

    @Override
    public void registerControllers(AnimationData data) { }
    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void age() {
        if (!this.inGround) {
            if (this.ticksInAir++ >= 160)
                this.remove(RemovalReason.DISCARDED);
        } else {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity owner = this.getOwner();
        if (owner == null) { return; }
        Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner) { return; }

        if (entity instanceof LivingEntity living) {
            DamageLogic(world, living, Vec3d.ZERO, 10, 1, false, 2f, false, DamageSource.thrownProjectile(this, owner), owner);
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0, false, true));
            this.remove(RemovalReason.DISCARDED);
        }

        if (entity instanceof EndCrystalEntity endCrystal)
            endCrystal.damage(DamageSource.thrownProjectile(this, this.getOwner()), 2f);

        this.playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1, 0.5f);
    }

    @Override
    public void setVelocity(double x, double y, double z, float speed, float divergence) {
        super.setVelocity(x, y, z, speed, divergence);
        this.ticksInAir = 0;
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
    public boolean hasNoGravity() { return false; }
}
