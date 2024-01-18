package net.arna.jcraft.common.entity;

import net.arna.jcraft.common.util.IOwnable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
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

import java.util.Arrays;

public class GEButterflyEntity extends FlyingEntity implements IAnimatable, IAnimationTickable, IOwnable {
    public GEButterflyEntity(EntityType<? extends FlyingEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new FlightMoveControl(this, 10, false);
        this.navigation = new BirdNavigation(this, world);
        Arrays.fill(this.handDropChances, 2.0F);
    }

    private boolean hasMaster = false;
    private LivingEntity master;

    @Override
    public LivingEntity getMaster() {
        return master;
    }

    @Override
    public void setMaster(LivingEntity m) {
        master = m;
        hasMaster = true;
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new SwimGoal(this));
        goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        if (player == master) {
            if (player.getStackInHand(hand).isEmpty()) {
                player.setStackInHand(hand, getMainHandStack());
                discard();
            } else return ActionResult.FAIL;
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient || !hasMaster) return;
        if (master == null) {
            kill();
            return;
        }

        if (squaredDistanceTo(master) > 16)
            navigation.startMovingTo(master, 1.0);
        else if (navigation.isFollowingPath())
            navigation.stop();
    }

    public static DefaultAttributeContainer.Builder createButterflyAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_FLYING_SPEED, 1.0)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("HasMaster", hasMaster);
        if (master instanceof PlayerEntity player)
            nbt.putUuid("MasterUUID", player.getUuid());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        hasMaster = nbt.getBoolean("HasMaster");
        if (nbt.containsUuid("MasterUUID"))
            setMaster(world.getPlayerByUuid(nbt.getUuid("MasterUUID")));
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

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (isAlive()) {
            event.getController().setAnimation(new AnimationBuilder().loop("animation.gebutterfly.idle"));
            return PlayState.CONTINUE;
        } else return PlayState.STOP;
    }
}
