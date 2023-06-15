package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;
import net.arna.jcraft.registry.*;

public class KnifeProjectile extends PersistentProjectileEntity implements IAnimatable {
    private int ticksInAir;

    private static final TrackedData<Boolean> LIGHTNING;
    private boolean delayed = false;
    private boolean delayFired = false;
    private int delayTime;

    public KnifeProjectile(EntityType<? extends KnifeProjectile> entityType, World world) {
        super(entityType, world);
        this.pickupType = PersistentProjectileEntity.PickupPermission.DISALLOWED;
    }

    public KnifeProjectile(World world, LivingEntity owner) {
        super(JEntityTypeRegister.KNIFE, owner, world);
        this.setOwner(owner);
    }

    static {
        LIGHTNING = DataTracker.registerData(KnifeProjectile.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public Boolean getLightning() {
        return this.dataTracker.get(LIGHTNING);
    }

    public void setLightning(Boolean li) {
        this.dataTracker.set(LIGHTNING, li);
    }

    public void setDelayedLightning(int dt) {
        setLightning(true);
        delayed = true;
        delayTime = dt;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(LIGHTNING, false);
    }

    @Override
    public ItemStack asItemStack() {
        return new ItemStack(JObjectRegistry.KNIFE);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.inGround) ++this.ticksInAir;

        if (getLightning()) {
            if (world.isClient) {
                double x = getX();
                double y = getY();
                double z = getZ();
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0, 0, 0);
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, (x + prevX) / 2, (y + prevY) / 2, (z + prevZ) / 2, 0, 0, 0);
            } else {
                if (ticksInAir > 200 || inGround)
                    discard();
                if (delayed) {
                    delayTime--;
                    Entity owner = getOwner();
                    if (owner != null) {
                        if (delayTime < 1) {
                            if (!delayFired) {
                                Vec3d eP = owner.getEyePos();
                                Vec3d rangeMod = owner.getRotationVector().multiply(24);

                                EntityHitResult eHit = ProjectileUtil.raycast(owner, eP, eP.add(rangeMod),
                                        owner.getBoundingBox().expand(24),
                                        EntityPredicates.VALID_LIVING_ENTITY, // This is a hack, and will miss on stuff like End Crystals, but also makes it miss on other knives
                                        576 // Squared
                                );

                                HitResult hitResult = world.raycast(new RaycastContext(eP, eP.add(rangeMod), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, owner));

                                playSound(JSoundRegister.TWOH_SHOOT, 1, 1);

                                Vec3d hitPos = hitResult.getPos();
                                if (eHit != null) hitPos = eHit.getPos();
                                setVelocity(new Vec3d(hitPos.x - getX(), hitPos.y - getY(), hitPos.z - getZ()).normalize());

                                velocityDirty = true;
                                delayFired = true;
                            }
                        } else setVelocity(getVelocity().multiply(0.5));
                    }
                }
            }
        } else if (ticksInAir > 640 && !world.isClient) discard();
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        this.setLightning(true);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (world.isClient) return;
        if (delayed && delayTime > 1) return;
        Entity owner = this.getOwner();
        if (owner == null) return;
        Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner) return;

        if (this.isOnFire()) entity.setOnFireFor(5);

        int blockstun = 4;
        int stunT = 0;
        if (this.getLightning()) {
            stunT = 20;
            blockstun = 6;
        } else {
            dropStack(this.asItemStack(), 0.1F);
        }

        JCraftUtils.projectileDamageLogic(this, world, entity, Vec3d.ZERO, stunT, 1, false, 2, blockstun);
        playSound(SoundEvents.ITEM_TRIDENT_HIT, 1, 1);
        discard();
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        tag.putShort("life", (short) this.ticksInAir);
        tag.putBoolean("lightning", getLightning());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        this.ticksInAir = tag.getShort("life");
        setLightning(tag.getBoolean("lightning"));
    }

    // Animations
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    @Override
    public void registerControllers(AnimationData data) { }
    @Override
    public AnimationFactory getFactory() { return this.factory; }
}
