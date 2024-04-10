package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.stand.TheSunEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.arna.jcraft.common.entity.stand.StandEntity.damageLogic;
import static net.arna.jcraft.common.util.JUtils.canDamage;

public class SunBeamProjectile extends PersistentProjectileEntity implements IAnimatable, IAnimationTickable {
    private final int stun = 10;
    private int length = 0;
    private int maxLength = 64;
    private TheSunEntity sun;

    public static final TrackedData<Integer> SKIN;

    static {
        SKIN = DataTracker.registerData(SunBeamProjectile.class, TrackedDataHandlerRegistry.INTEGER);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(SKIN, 0);
    }

    public int getSkin() {
        return dataTracker.get(SKIN);
    }

    public void setSkin(int skin) {
        dataTracker.set(SKIN, skin);
    }

    public SunBeamProjectile(World world) {
        super(JEntityTypeRegistry.SUN_BEAM, world);
        this.setNoGravity(true);
        ignoreCameraFrustum = true;
    }

    public void assignSun(TheSunEntity sunEntity) {
        this.sun = sunEntity;
    }

    @Override
    protected ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    // Scorpions aren't very heavy
    @Override
    public void pushAwayFrom(Entity entity) {
    }

    @Override
    public boolean collidesWith(Entity other) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        Vec3d curPos = getPos();

        if (age <= 10)
            length += maxLength / 10;

        if (world.isClient()) {
            if (age <= 20) {
                Vec3d velocity = getVelocity().multiply(random.nextDouble() * length * 10.0);
                world.addParticle(
                        getSkin() == 2 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                        curPos.x + random.nextGaussian() * 0.25,
                        curPos.y + random.nextGaussian() * 0.25,
                        curPos.z + random.nextGaussian() * 0.25,
                        velocity.x,
                        velocity.y,
                        velocity.z
                );
            }
        } else {
            if (age <= 20) {
                if (age % 3 == 0 && getOwner() instanceof LivingEntity owner) {
                    Set<Entity> filter = new HashSet<>();
                    filter.add(owner);
                    filter.add(sun);
                    filter.add(this);
                    if (owner.hasPassengers()) filter.addAll(owner.getPassengerList());

                    DamageSource damageSource = DamageSource.mob(owner);

                    // Recursive hitbox check between current and previous position
                    Vec3d towardsVec = getVelocity().normalize();
                    List<LivingEntity> hurtAll = new ArrayList<>();
                    for (double i = 0; i < length; i++) {
                        Vec3d laserPos = curPos.add(towardsVec.multiply(i));
                        hurtAll.addAll(JUtils.generateHitbox(world, laserPos, 1, filter));
                        TheSunEntity.dryOut((ServerWorld) world, new BlockPos(laserPos));
                    }
                    hurtAll.removeIf(e -> !canDamage(damageSource, e));

                    if (!hurtAll.isEmpty()) {
                        for (LivingEntity l : hurtAll) {
                            LivingEntity target = JUtils.getUserIfStand(l);
                            damageLogic(world, target, Vec3d.ZERO, stun, 1, false, 1f,
                                    true, 2, damageSource, owner, HitPropertyComponent.HitAnimation.values()[random.nextInt(3)]);
                        }

                        Vec3d hitPos = hurtAll.get(0).getPos();
                        JCraft.createParticle((ServerWorld) world,
                                hitPos.x + random.nextGaussian() * 0.25,
                                hitPos.y + random.nextGaussian() * 0.25,
                                hitPos.z + random.nextGaussian() * 0.25,
                                JParticleType.HIT_SPARK_1);
                    }
                }
            } else if (age >= 24) kill();
        }
    }

    public void updateRotation() {
        super.updateRotation();
    }

    // Animations
    private final AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

    @Override
    public int tickTimer() {
        return age;
    }

    @SuppressWarnings("SameReturnValue")
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().playOnce("animation.sunbeam.fire"));
        return PlayState.CONTINUE;
    }
}
