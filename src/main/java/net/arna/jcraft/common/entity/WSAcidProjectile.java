package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.util.IEntityDataSaver;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegister;
import net.arna.jcraft.registry.JStatusRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
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

import java.util.List;

import static net.arna.jcraft.common.entity.StandEntity.damageLogic;

public class WSAcidProjectile extends PersistentProjectileEntity implements IAnimatable {
    private static final TrackedData<Boolean> MYH; // Melt your Heart variant
    private static final TrackedData<Boolean> SPLAT;
    private static final TrackedData<Float> FINALPITCH;
    private static final TrackedData<Float> FINALYAW;
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    static {
        MYH = DataTracker.registerData(WSAcidProjectile.class, TrackedDataHandlerRegistry.BOOLEAN);
        SPLAT = DataTracker.registerData(WSAcidProjectile.class, TrackedDataHandlerRegistry.BOOLEAN);
        FINALPITCH = DataTracker.registerData(WSAcidProjectile.class, TrackedDataHandlerRegistry.FLOAT);
        FINALYAW = DataTracker.registerData(WSAcidProjectile.class, TrackedDataHandlerRegistry.FLOAT);
    }

    public WSAcidProjectile(EntityType<? extends WSAcidProjectile> entityType, World world) {
        super(entityType, world);
    }

    public void markMeltYourHeart() {
        dataTracker.set(MYH, true);
    }

    private void splat() {
        dataTracker.set(SPLAT, true);
        setNoGravity(true);
    }

    @Override
    public float getPitch() {
        if (dataTracker.get(SPLAT))
            return dataTracker.get(FINALPITCH);
        return super.getPitch();
    }

    @Override
    public float getYaw() {
        if (dataTracker.get(SPLAT))
            return dataTracker.get(FINALYAW);
        return super.getYaw();
    }

    public WSAcidProjectile(World world, LivingEntity owner) {
        super(JEntityTypeRegister.WS_ACID_PROJECTILE, owner, world);
        setSound(SoundEvents.BLOCK_SLIME_BLOCK_FALL);
        setOwner(owner);
        pickupType = PickupPermission.DISALLOWED;
        ignoreCameraFrustum = true;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(FINALPITCH, 0.0F);
        dataTracker.startTracking(FINALYAW, 0.0F);
        dataTracker.startTracking(SPLAT, false);
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
            target.addStatusEffect(new StatusEffectInstance(JStatusRegister.WSPOISON, 60, 0, false, true));
            discard();
        }

        if (entity instanceof EndCrystalEntity endCrystal)
            endCrystal.damage(DamageSource.thrownProjectile(this, owner), 2f);

        playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1, 0.5f);
    }

    @Override
    protected Box calculateBoundingBox() {
        if (dataTracker.get(SPLAT)) {
            double x = getX();
            double y = getY();
            double z = getZ();
            return new Box(x, y, z, x, y + 0.1, z);
        }
        return super.calculateBoundingBox();
    }

    private int timeOnSurface = 0;

    @Override
    protected void age() {
        super.age();
        if (world.isClient) return;
        if (timeOnSurface++ >= 100) discard();
        if (!dataTracker.get(SPLAT)) splat();
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

        if (inGround) {
            if (!world.isClient && dataTracker.get(SPLAT) && age % 4 == 0) {
                List<Entity> except;
                StandEntity<?, ?> ownerStand = ((IEntityDataSaver) owner).getStand();
                if (ownerStand != null) except = List.of(owner, ownerStand);
                else except = List.of(owner);

                for (LivingEntity living : JUtils.generateHitbox(world, getPos(), 1.5, except)) {
                    living.addStatusEffect(new StatusEffectInstance(JStatusRegister.WSPOISON, 20, 0, true, false));
                    living.damage(JDamageSources.whitesnakePoison(owner), 2f);
                }
            }
        } else if (world.isClient) {
            if (dataTracker.get(SPLAT)) discard(); // Remove if splatted and not on ground

            Vec3d vel = getVelocity();
            world.addParticle(
                    ParticleTypes.SPIT,
                    getX(), getY(), getZ(),
                    vel.x, vel.y, vel.z);
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);

        float snappedYaw = getYaw();
        Direction direction = blockHitResult.getSide();
        int dirID = direction.getId();
        float snappedPitch = (dirID > 1) ? 0F : getPitch();

        switch (dirID) {
            case (0) -> snappedPitch = 90F;
            case (1) -> snappedPitch = -90F;

            case (2) -> snappedYaw = 0F;
            case (3) -> snappedYaw = 180F;
            case (4) -> snappedYaw = 90F;
            case (5) -> snappedYaw = -90F;
        }

        dataTracker.set(FINALPITCH, snappedPitch);
        dataTracker.set(FINALYAW, snappedYaw);
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
        event.getController().setAnimation(
                dataTracker.get(SPLAT) ?
                        new AnimationBuilder().playAndHold("animation.wsacid.splat") :
                        new AnimationBuilder().loop(dataTracker.get(MYH) ? "animation.wsacid.meltidle" : "animation.wsacid.idle")
        );
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
