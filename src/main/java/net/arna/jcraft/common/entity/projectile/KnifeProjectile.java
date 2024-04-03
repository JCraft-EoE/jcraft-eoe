package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.arna.jcraft.registry.JObjectRegistry;
import net.arna.jcraft.registry.JSoundRegistry;
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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KnifeProjectile extends PersistentProjectileEntity implements GeoEntity {
    private static final TrackedData<Boolean> LIGHTNING;
    private int ticksInAir;
    private boolean delayed = false;
    private boolean delayFired = false;
    private int delayTime;

    static {
        LIGHTNING = DataTracker.registerData(KnifeProjectile.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public KnifeProjectile(EntityType<? extends KnifeProjectile> entityType, World world) {
        super(entityType, world);
    }

    public KnifeProjectile(World world) {
        super(JEntityTypeRegistry.KNIFE, world);
    }

    public KnifeProjectile(World world, LivingEntity owner) {
        super(JEntityTypeRegistry.KNIFE, owner, world);
        this.setOwner(owner);
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

        if (!getLightning()) {
            if (ticksInAir > 640 && !getWorld().isClient) discard();
            return;
        }
        if (getWorld().isClient) {
            double x = getX();
            double y = getY();
            double z = getZ();
            getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, 0, 0, 0);
            getWorld().addParticle(ParticleTypes.ELECTRIC_SPARK, (x + prevX) / 2, (y + prevY) / 2, (z + prevZ) / 2, 0, 0, 0);
            return;
        }

        if (ticksInAir > 200 || inGround)
            discard();
        if (!delayed) return;

        delayTime--;
        Entity owner = getOwner();
        if (owner == null) return;

        if (delayTime >= 1) {
            setVelocity(getVelocity().multiply(0.5));
            return;
        }

        if (delayFired) return;
        Vec3d eP = owner.getEyePos();
        Vec3d rangeMod = owner.getRotationVector().multiply(24);

        EntityHitResult eHit = ProjectileUtil.raycast(owner, eP, eP.add(rangeMod),
                owner.getBoundingBox().expand(24),
                EntityPredicates.VALID_LIVING_ENTITY, // This is a hack, and will miss on stuff like End Crystals, but also makes it miss on other knives
                576 // Squared
        );

        HitResult hitResult = getWorld().raycast(new RaycastContext(eP, eP.add(rangeMod), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, owner));

        playSound(JSoundRegistry.TWOH_SHOOT, 1, 1);

        Vec3d hitPos = hitResult.getPos();
        if (eHit != null) hitPos = eHit.getPos();
        setVelocity(new Vec3d(hitPos.x - getX(), hitPos.y - getY(), hitPos.z - getZ()).normalize());

        velocityDirty = true;
        delayFired = true;
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
        this.setLightning(true);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (getWorld().isClient) return;
        if (delayed && delayTime > 1) return;
        Entity entity = entityHitResult.getEntity();
        Entity owner = this.getOwner();

        if (owner != null && owner.hasPassenger(entity) || entity == owner) return;

        if (isOnFire()) entity.setOnFireFor(5);

        int blockstun = 4;
        int stunT = 0;
        if (getLightning()) {
            stunT = 20;
            blockstun = 6;
        } else dropStack(asItemStack(), 0.1F);

        JUtils.projectileDamageLogic(this, getWorld(), entity, Vec3d.ZERO, stunT, 1, false, 2, blockstun, HitPropertyComponent.HitAnimation.MID);
        playSound(SoundEvents.ITEM_TRIDENT_HIT, 1, 1);
        if (entity instanceof LivingEntity living) JComponents.getMiscData(living).stab();
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
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
