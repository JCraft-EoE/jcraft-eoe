package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegistry;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AnkhProjectile extends PersistentProjectileEntity implements GeoEntity {
    private int ticksInAir;
    private boolean variation = false;
    private double orbitRange = 3;
    private double orbitOffset = 0;

    public AnkhProjectile(EntityType<? extends AnkhProjectile> entityType, World world) {
        super(entityType, world);
    }

    public AnkhProjectile(World world, LivingEntity owner) {
        super(JEntityTypeRegistry.ANKH, owner, world);
        this.setOwner(owner);
        this.pickupType = PickupPermission.DISALLOWED;
    }

    public void setOrbitRange(double range) {
        this.orbitRange = range;
    }

    public void setOrbitOffset(double offset) {
        this.orbitOffset = offset;
    }

    public void setVariation(boolean variation) {
        this.variation = variation;
    }

    @Override
    public ItemStack asItemStack() {
        return new ItemStack(Items.AIR);
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
    protected SoundEvent getHitSound() {
        return SoundEvents.ITEM_FIRECHARGE_USE;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (getWorld().isClient) return;
        Entity owner = getOwner();
        if (owner == null) return;
        Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner) return;

        entity.setOnFireFor(3);
        JUtils.projectileDamageLogic(this, getWorld(), entity, Vec3d.ZERO, 5, 1, false, 3.5f, 8, HitPropertyComponent.HitAnimation.MID);
        discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        MagiciansRedEntity.ignite(getWorld(), blockHitResult.getBlockPos());
        super.onBlockHit(blockHitResult);
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
    public void tick() {
        super.tick();

        if (getWorld().isClient()) {
            Vec3d vel = getVelocity();
            this.getWorld().addParticle(
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
                if (this.ticksInAir >= 600) discard();
            }

            if (this.getOwner() instanceof LivingEntity owner) {
                if (owner.isAlive()) {
                    if (this.variation) {
                        this.inGround = false;
                        this.inGroundTime = 0;

                        // Orbiting logic
                        double orbitProg = Math.toRadians(this.age * 3 + this.orbitOffset);
                        Vec3d orbitPos = owner.getEyePos().add(
                                Math.sin(orbitProg) * this.orbitRange,
                                0.0,
                                Math.cos(orbitProg) * this.orbitRange
                        );

                        Vec3d towardsVel = orbitPos.subtract(this.getPos()).normalize().multiply(0.2);
                        double stabilization = this.getPos().distanceTo(orbitPos);
                        if (stabilization > 0.8) stabilization = 0.8;
                        this.setVelocity(this.getVelocity().multiply(stabilization).add(towardsVel));
                        this.velocityModified = true;

                        // Entity hit logic, due to variations being noclipped
                        Vec3d pos = this.getPos();
                        Vec3d nextPos = pos.add(this.getVelocity());
                        //HitResult hitResult = this.world.raycast(new RaycastContext(pos, nextPos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, this));
                        //if (hitResult.getType() != HitResult.Type.MISS) nextPos = hitResult.getPos();
                        EntityHitResult entityHitResult = this.getEntityCollision(pos, nextPos);
                        if (entityHitResult != null) this.onEntityHit(entityHitResult);
                    }
                } else {
                    this.variation = false;
                }
            } else {
                discard();
            }
        }
    }

    // Animations
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
