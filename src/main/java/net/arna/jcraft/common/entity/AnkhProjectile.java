package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.util.JCraftUtils;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Random;

public class AnkhProjectile extends PersistentProjectileEntity implements IAnimatable {

    private int ticksInAir;
    private boolean variation = false;
    private double orbitRange = 1;
    private double orbitOffset = 0;

    private final AnimationFactory factory = new AnimationFactory(this);

    public AnkhProjectile(EntityType<? extends AnkhProjectile> entityType, World world) {
        super(entityType, world);
    }

    public AnkhProjectile(World world, LivingEntity owner) {
        super(JEntityTypeRegister.ANKH, owner, world);

        Random random = new Random();

        this.setOwner(owner);
        this.orbitRange = random.nextDouble(3, 4);
        this.orbitOffset = random.nextDouble(-180, 180);
        this.pickupType = PickupPermission.DISALLOWED;
    }

    public void setVariation(boolean variation) {
        this.variation = variation;
    }

    @Override
    public void registerControllers(AnimationData data) {
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public ItemStack asItemStack() {
        return new ItemStack(Items.AIR);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity owner = this.getOwner();
        if (owner == null) {
            return;
        }
        Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner) {
            return;
        }

        entity.setOnFireFor(3);
        JCraftUtils.ProjectileDamageLogic(this, world, entity, Vec3d.ZERO, 10, 1, false, 2.5f);

        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    public void setVelocity(Vec3d velocity) {
        super.setVelocity(velocity);
        this.ticksInAir = 0;
    }

    @Override
    public void setVelocity(double x, double y, double z, float speed, float divergence) {
        super.setVelocity(x, y, z, speed, divergence);
        this.ticksInAir = 0;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        tag.putBoolean("variation", this.variation);
        tag.putShort("life", (short) this.ticksInAir);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        this.ticksInAir = tag.getShort("life");
        this.variation = tag.getBoolean("variation");
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
    protected boolean updateWaterState() {
        return false;
    }

    @Override
    public boolean isNoClip() {
        return this.variation;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isClient()) {
            this.world.addParticle(
                    ParticleTypes.FLAME,
                    this.getX() + random.nextFloat() * 0.5f - 0.25f,
                    this.getY() + random.nextFloat() * 0.5f - 0.25f,
                    this.getZ() + random.nextFloat() * 0.5f - 0.25f,
                    0.0, 0.0, 0.0
            );
        } else {
            if (this.inGround) {
                this.remove(RemovalReason.DISCARDED);
            } else {
                this.ticksInAir++;
                int removalTicks = this.variation ? 600 : 160;

                if (this.ticksInAir >= removalTicks) {
                    this.remove(RemovalReason.DISCARDED);
                }
            }

            if (this.getOwner() instanceof LivingEntity owner) {
                if (owner.isAlive()) {
                    if (this.variation) {
                        this.inGround = false;
                        this.inGroundTime = 0;

                        // Orbiting logic
                        Vec3d orbitPos = owner.getEyePos().add(
                                Math.sin(Math.toRadians(this.age + this.orbitOffset) * 5) * this.orbitRange,
                                0.0,
                                Math.cos(Math.toRadians(this.age + this.orbitOffset) * 5) * this.orbitRange
                        );

                        Vec3d towardsVel = orbitPos.subtract(this.getPos()).normalize().multiply(0.2);
                        double stabilization = this.getPos().distanceTo(orbitPos);
                        if (stabilization > 0.8) {
                            stabilization = 0.8;
                        }

                        this.setVelocity(this.getVelocity().multiply(stabilization).add(towardsVel));
                        this.velocityModified = true;

                        // Entity hit logic, due to variations being noclipped
                        Vec3d pos = this.getPos();
                        Vec3d nextPos = pos.add(this.getVelocity());
                        HitResult hitResult = this.world.raycast(new RaycastContext(pos, nextPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));

                        if (hitResult.getType() != HitResult.Type.MISS) {
                            nextPos = hitResult.getPos();
                        }

                        EntityHitResult entityHitResult = this.getEntityCollision(pos, nextPos);

                        if (entityHitResult != null) {
                            this.onEntityHit(entityHitResult);
                        }
                    }
                } else {
                    this.variation = false;
                }
            } else {
                this.discard();
            }
        }
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.ITEM_FIRECHARGE_USE;
    }
}
