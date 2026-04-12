package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import mod.azure.azurelib.util.AzureLibUtil;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class AnkhProjectile extends AbstractArrow {
    private int ticksInAir;
    private boolean variation = false;
    private double orbitRange = 3;
    private double orbitOffset = 0;

    public AnkhProjectile(final Level world) {
        super(JEntityTypeRegistry.ANKH.get(), world);
    }

    public AnkhProjectile(final Level world, final LivingEntity owner) {
        super(JEntityTypeRegistry.ANKH.get(), owner, world);
        this.pickup = Pickup.DISALLOWED;
    }

    public void setOrbitRange(final double range) {
        this.orbitRange = range;
    }

    public void setOrbitOffset(final double offset) {
        this.orbitOffset = offset;
    }

    public void setVariation(final boolean variation) {
        this.variation = variation;
    }

    @Override
    public @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected boolean updateInWaterStateAndDoFluidPushing() {
        return false;
    }

    @Override
    public boolean isNoPhysics() {
        return this.variation;
    }

    @Override
    protected @NonNull SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.FIRECHARGE_USE;
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

        entity.setSecondsOnFire(3);
        JUtils.projectileDamageLogic(this, level(), entity, Vec3.ZERO, 5, 1, false, 3.5f, 8, CommonHitPropertyComponent.HitAnimation.MID);
        discard();
    }

    @Override
    protected void onHitBlock(final BlockHitResult blockHitResult) {
        MagiciansRedEntity.ignite(level(), blockHitResult.getBlockPos());
        super.onHitBlock(blockHitResult);
    }

    @Override
    public void addAdditionalSaveData(final @NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("variation", this.variation);
        tag.putShort("life", (short) this.ticksInAir);
    }

    @Override
    public void readAdditionalSaveData(final @NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ticksInAir = tag.getShort("life");
        this.variation = tag.getBoolean("variation");
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            final Vec3 vel = getDeltaMovement();
            this.level().addParticle(
                    ParticleTypes.FLAME,
                    getX() + random.nextFloat() * 0.5f - 0.25f,
                    getY() + random.nextFloat() * 0.5f - 0.25f,
                    getZ() + random.nextFloat() * 0.5f - 0.25f,
                    vel.x / 2, vel.y / 2, vel.z / 2
            );
        } else {
            if (this.inGround) {
                discard();
            } else {
                this.ticksInAir++;
                if (this.ticksInAir >= 600) {
                    discard();
                }
            }

            if (this.getOwner() instanceof LivingEntity owner) {
                if (owner.isAlive()) {
                    if (this.variation) {
                        this.inGround = false;
                        this.inGroundTime = 0;

                        // Orbiting logic
                        double orbitProg = Math.toRadians(this.tickCount * 3 + this.orbitOffset);
                        final Vec3 orbitPos = owner.getEyePosition().add(
                                Math.sin(orbitProg) * this.orbitRange,
                                0.0,
                                Math.cos(orbitProg) * this.orbitRange
                        );

                        final Vec3 pos = this.position();

                        final Vec3 towardsVel = orbitPos.subtract(pos).normalize().scale(0.2);
                        double stabilization = Math.min(pos.distanceTo(orbitPos), 0.8);
                        this.setDeltaMovement(this.getDeltaMovement().scale(stabilization).add(towardsVel));
                        this.hurtMarked = true;

                        // Entity hit logic, due to variations being noclipped
                        final Vec3 nextPos = pos.add(this.getDeltaMovement());
                        final EntityHitResult entityHitResult = this.findHitEntity(pos, nextPos);
                        if (entityHitResult != null) {
                            this.onHitEntity(entityHitResult);
                        }
                    }
                } else {
                    this.variation = false;
                }
            } else {
                discard();
            }
        }
    }

}
