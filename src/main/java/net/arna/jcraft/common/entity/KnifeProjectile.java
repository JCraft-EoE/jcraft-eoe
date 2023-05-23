package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.util.JCraftUtils;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegister;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

public class KnifeProjectile extends PersistentProjectileEntity implements IAnimatable {
    private int ticksInAir;

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

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
    public void registerControllers(AnimationData data) {
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public ItemStack asItemStack() {
        return new ItemStack(JObjectRegistry.KNIFE);
    }

    @Override
    protected void age() {
        if (!this.inGround) {
            ++this.ticksInAir;
            if (this.ticksInAir >= 160)
                this.remove(Entity.RemovalReason.DISCARDED);
        } else if (getLightning())
            this.remove(Entity.RemovalReason.DISCARDED);
    }

    @Override
    public void tick() {
        super.tick();

        if (getLightning()) {
            if (world.isClient) {
                double x = getX();
                double y = getY();
                double z = getZ();
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0, 0, 0);
                world.addParticle(ParticleTypes.ELECTRIC_SPARK, (x + prevX) / 2, (y + prevY) / 2, (z + prevZ) / 2, 0, 0, 0);
            } else {
                if (this.age > 1600)
                    this.discard();
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

                                HitResult hitResult = this.world.raycast(new RaycastContext(eP, eP.add(rangeMod), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, owner));

                                playSound(JSoundRegister.TWOH_SHOOT, 1, 1);

                                Vec3d hitPos = hitResult.getPos();
                                if (eHit != null) {
                                    hitPos = eHit.getPos();
                                    this.setVelocity(new Vec3d(hitPos.x - getX(), hitPos.y - getY(), hitPos.z - getZ()).normalize());
                                } else {
                                    this.setVelocity(new Vec3d(hitPos.x - getX(), hitPos.y - getY(), hitPos.z - getZ()).normalize());
                                }
                                velocityDirty = true;
                                delayFired = true;
                            }
                        } else {
                            this.setVelocity(this.getVelocity().multiply(0.5));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        this.setLightning(true);
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

        if (this.isOnFire())
            entity.setOnFireFor(5);

        int stunT = 0;
        if (this.getLightning()) {
            stunT = 20;
        } else {
            this.dropStack(this.asItemStack(), 0.1F);
        }

        JCraftUtils.ProjectileDamageLogic(this, world, entity, Vec3d.ZERO, stunT, 1, false, 2);

        this.remove(RemovalReason.DISCARDED);
        this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1, 1);
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
        tag.putBoolean("lightning", getLightning());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        this.ticksInAir = tag.getShort("life");
        setLightning(tag.getBoolean("lightning"));
    }

    @Override
    @Environment(EnvType.CLIENT)
    public boolean shouldRender(double distance) {
        return true;
    }
}
