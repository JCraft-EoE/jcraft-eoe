package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.util.JCraftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.builder.ILoopType.EDefaultLoopTypes;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.List;

public class GETreeEntity extends Entity implements IAnimatable, IAnimationTickable {
    public LivingEntity owner;

    public GETreeEntity(EntityType<? extends Entity> type, World world) { super(type, world); }
    @Override
    protected void initDataTracker() { }

    @Override
    public void tick() {
        super.tick();

        if (owner != null) {
            if (age == 4) {
                List<LivingEntity> hurt = JCraftUtils.generateHitbox(world, getPos().add(0, 1, 0), 2.5, null);
                for (LivingEntity living :
                        hurt) {
                    LivingEntity target = JCraftUtils.getUserIfStand(living);
                    if (owner != target)
                        StandEntity.damageLogic(world, target, new Vec3d(0, 1, 0), 25, 1, false, 7f, true, 11, DamageSource.mob(owner), owner);

                    target.setVelocity(0, 1, 0);
                    target.velocityModified = true;
                }
            }
        }

        if (age > 120) discard();
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    // Animations
    AnimationFactory factory = GeckoLibUtil.createFactory(this);
    @Override
    public AnimationFactory getFactory() { return this.factory; }
    @Override
    public void registerControllers(AnimationData animationData) { animationData.addAnimationController(new AnimationController(this, "controller", 0, this::predicate)); }
    @Override
    public int tickTimer() { return age; }
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController controller = event.getController();
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
