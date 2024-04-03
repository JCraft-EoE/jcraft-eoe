package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
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

import java.util.Set;

public class GETreeEntity extends JAttackEntity implements GeoEntity {
    private final Vec3d launchVec;
    public GETreeEntity(EntityType<? extends LivingEntity> type, World world) {
        this(type, world, Vec3d.ZERO);
    }
    public GETreeEntity(EntityType<? extends LivingEntity> type, World world, Vec3d launchVec) {
        super(type, world);
        this.setInvulnerable(true);
        this.launchVec = launchVec;
    }

    @Override
    public void tick() {
        super.tick();
        if (age > 120) discard();

        if (getWorld().isClient || master == null) return;

        if (age == 4) {
            DamageSource ds = getWorld().getDamageSources().mobAttack(master);
            Set<LivingEntity> hurt = JUtils.generateHitbox(getWorld(), getPos().add(launchVec.normalize()), 2.5, Set.of(this, master));

            for (LivingEntity living : hurt) {
                if (!JUtils.canDamage(ds, living)) continue;

                LivingEntity target = JUtils.getUserIfStand(living);
                if (master != target)
                    StandEntity.damageLogic(getWorld(), target, Vec3d.ZERO, 25, 3,
                            false, 7f, false, 11, ds, master, HitPropertyComponent.HitAnimation.MID, false);
                JUtils.addVelocity(target, launchVec.x, launchVec.y, launchVec.z);
            }
        }
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        return false;
    }

    // Animations
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GeoAnimatable>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<GeoAnimatable> state) {
        if (state.getController().getCurrentAnimation() == null) {
            state.setAnimation(
                    RawAnimation.begin()
                            .thenPlay("animation.getree.spawn")
                            .thenPlay("animation.getree.idle")
                            .thenPlay("animation.getree.return")
            );
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
