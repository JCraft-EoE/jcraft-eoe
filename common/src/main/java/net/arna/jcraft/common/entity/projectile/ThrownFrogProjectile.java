package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.common.entity.BluePoisonFrogEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownFrogProjectile extends AbstractArrow {

    private LivingEntity thrower;

    public ThrownFrogProjectile(Level world) {
        super(JEntityTypeRegistry.THROWN_FROG.get(), world);
    }

    public ThrownFrogProjectile(Level world, LivingEntity owner) {
        super(JEntityTypeRegistry.THROWN_FROG.get(), owner, world);
        this.thrower = owner;
        setSoundEvent(SoundEvents.FROG_AMBIENT);
        setNoGravity(false);
        setBaseDamage(0);
    }

    @Override
    public @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void tickDespawn() {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                    getX() + random.nextGaussian() * 0.1,
                    getY() + random.nextGaussian() * 0.1,
                    getZ() + random.nextGaussian() * 0.1,
                    0, 0.05, 0);
        }
        if (!level().isClientSide && tickCount > 200) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult hit) {
        if (level().isClientSide) return;
        spawnFrogAt(hit.getEntity().position());
        discard();
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult hit) {
        if (level().isClientSide) return;
        final Vec3 loc = hit.getLocation();
        spawnFrogAt(new Vec3(loc.x, loc.y + 0.05, loc.z));
        level().playSound(null, blockPosition(), SoundEvents.FROG_AMBIENT,
                getSoundSource(), 1.0f, 0.8f + random.nextFloat() * 0.4f);
        discard();
    }

    private void spawnFrogAt(Vec3 pos) {
        final BluePoisonFrogEntity frog = new BluePoisonFrogEntity(
                JEntityTypeRegistry.BLUE_POISON_FROG.get(), level());
        if (thrower != null) frog.setMaster(thrower);
        frog.setPos(pos.x, pos.y, pos.z);
        level().addFreshEntity(frog);
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
    }

    @Override
    protected float getWaterInertia() {
        return 0.8f;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }
}
