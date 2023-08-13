package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType.EDefaultLoopTypes;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.Set;

public class GETreeEntity extends JAttackEntity implements IAnimatable, IAnimationTickable {
    public GETreeEntity(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
        setInvulnerable(true);
    }

    @Override
    public void tick() {
        super.tick();

        if (master != null) {
            if (age == 4) {
                DamageSource ds = DamageSource.mob(master);
                Set<LivingEntity> hurt = JUtils.generateHitbox(world, getPos().add(0, 1.5, 0), 2.5, Set.of(this, master));
                for (LivingEntity living : hurt) {
                    if (!JUtils.canDamage(ds, living)) continue;

                    LivingEntity target = JUtils.getUserIfStand(living);
                    if (master != target)
                        StandEntity.damageLogic(world, target, new Vec3d(0, 1, 0), 25, 3,
                                false, 7f, true, 11, ds, master);

                    target.setVelocity(0, 1, 0);
                    target.velocityModified = true;
                }
            }
        }

        if (age > 120) discard();
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        return false;
    }

    // Animations
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public int tickTimer() {
        return age;
    }

    @SuppressWarnings("SameReturnValue")
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController<E> controller = event.getController();
        if (controller.getCurrentAnimation() == null) {
            controller.setAnimation(
                    new AnimationBuilder().addAnimation("animation.getree.spawn", EDefaultLoopTypes.PLAY_ONCE)
                            .addAnimation("animation.getree.idle", EDefaultLoopTypes.PLAY_ONCE)
                            .addAnimation("animation.getree.return", EDefaultLoopTypes.PLAY_ONCE)
            );
        }
        return PlayState.CONTINUE;
    }
}
