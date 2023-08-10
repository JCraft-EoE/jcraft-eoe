package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.entity.StandEntity;
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
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import static net.arna.jcraft.common.entity.StandEntity.damageLogic;

public class WSAcidProjectile extends PersistentProjectileEntity implements IAnimatable {
    private static final TrackedData<Boolean> MYH; // Melt your Heart variant
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

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
        JUtils.getSplatterManager(world).addSplatter(getPos(), SplatterType.ACID, 1, getOwner());
        discard();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(MYH, false);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (world.isClient) return;

        Entity owner = getOwner();
        if (owner == null) return;

        if (dataTracker.get(MYH)) return; // Melt your Heart variants of this phase through entities

        Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner) return;

        if (entity instanceof LivingEntity living) {
            LivingEntity target = living;
            if (entity instanceof StandEntity<?, ?> stand && stand.hasUser())
                target = stand.getUserOrThrow();
            damageLogic(world, target, Vec3d.ZERO, 10, 1, false, 5f, false, 6, DamageSource.thrownProjectile(this, owner), owner);
            target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.WSPOISON, 60, 0, false, true));
            discard();
        }

        if (entity instanceof EndCrystalEntity endCrystal)
            endCrystal.damage(DamageSource.thrownProjectile(this, owner), 2f);

        playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1, 0.5f);
    }

    private int timeOnSurface = 0;

    @Override
    protected void age() {
        super.age();
        if (world.isClient) return;
        if (timeOnSurface++ >= 100) discard();
        splat();
    }

    @Override
    public void tick() {
        Entity owner = getOwner();
        if (owner == null) {
            if (!world.isClient) discard();
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

                world.addParticle(
                        ParticleTypes.SPIT,
                        pX, pY, pZ,
                        -awayVector.x, -awayVector.y, awayVector.z);
            }
        }

        super.tick();

        if (!inGround) {
            Vec3d vel = getVelocity();
            world.addParticle(
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
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @SuppressWarnings("SameReturnValue")
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation( new AnimationBuilder().loop(dataTracker.get(MYH) ? "animation.wsacid.meltidle" : "animation.wsacid.idle") );
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
