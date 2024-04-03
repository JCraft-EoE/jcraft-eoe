package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.splatter.SplatterType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import static net.arna.jcraft.common.entity.stand.StandEntity.damageLogic;

public class WSAcidProjectile extends PersistentProjectileEntity implements GeoEntity {
    private static final TrackedData<Boolean> MYH; // Melt your Heart variant
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    static {
        MYH = DataTracker.registerData(WSAcidProjectile.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public WSAcidProjectile(World world) {
        super(JEntityTypeRegistry.WS_ACID_PROJECTILE, world);
    }

    public WSAcidProjectile(World world, LivingEntity owner) {
        super(JEntityTypeRegistry.WS_ACID_PROJECTILE, owner, world);
        setSound(SoundEvents.BLOCK_SLIME_BLOCK_FALL);
        setOwner(owner);
        pickupType = PickupPermission.DISALLOWED;
        ignoreCameraFrustum = true;
    }

    public void markMeltYourHeart() {
        dataTracker.set(MYH, true);
    }

    private void splat() {
        JUtils.getSplatterManager(getWorld()).addSplatter(getPos(), SplatterType.ACID, 1, getOwner());
        discard();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(MYH, false);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (getWorld().isClient) return;

        Entity owner = getOwner();
        if (owner == null) return;

        if (dataTracker.get(MYH)) return; // Melt your Heart variants of this phase through entities

        Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner) return;

        if (entity instanceof LivingEntity living) {
            LivingEntity target = living;
            if (entity instanceof StandEntity<?, ?> stand && stand.hasUser())
                target = stand.getUserOrThrow();
            damageLogic(getWorld(), target, Vec3d.ZERO, 10, 1, false, 5f, false, 6,
                    getWorld().getDamageSources().thrown(this, owner), owner, HitPropertyComponent.HitAnimation.MID);
            target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.WSPOISON, 60, 0, false, true));
            discard();
        }

        if (entity instanceof EndCrystalEntity endCrystal)
            endCrystal.damage(getWorld().getDamageSources().thrown(this, owner), 2f);

        playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1, 0.5f);
    }

    private int timeOnSurface = 0;

    @Override
    protected void age() {
        super.age();
        if (getWorld().isClient) return;
        if (timeOnSurface++ >= 100) discard();
        splat();
    }

    @Override
    public void tick() {
        Entity owner = getOwner();
        if (owner == null) {
            if (!getWorld().isClient) discard();
            return;
        }

        // Display spit effects
        if (firstUpdate) {
            double x = getX();
            double y = getY();
            double z = getZ();
            for (int h = 0; h < 128; ++h) {
                double pX = x + random.nextDouble() * 2 - 1;
                double pY = y + random.nextDouble() * 2 - 1;
                double pZ = z + random.nextDouble() * 2 - 1;
                Vec3d awayVector = getRotationVecClient().multiply(0.3);

                getWorld().addParticle(
                        ParticleTypes.SPIT,
                        pX, pY, pZ,
                        -awayVector.x, -awayVector.y, awayVector.z);
            }
        }

        super.tick();

        if (!inGround) {
            Vec3d vel = getVelocity();
            getWorld().addParticle(
                    ParticleTypes.SPIT,
                    getX(), getY(), getZ(),
                    vel.x, vel.y, vel.z);
        }
    }

    @Override
    public ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean hasNoGravity() {
        return false;
    }

    // Animations

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GeoAnimatable>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<GeoAnimatable> state) {
        return state.setAndContinue(RawAnimation.begin().thenLoop(dataTracker.get(MYH) ? "animation.wsacid.meltidle" : "animation.wsacid.idle"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
