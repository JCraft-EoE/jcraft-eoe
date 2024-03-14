package net.arna.jcraft.common.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class HGNetEntity extends JAttackEntity implements IAnimatable, IAnimationTickable {
    public static final TrackedData<Integer> STATE;
    private int animTimer = 5;

    public HGNetEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    static {
        STATE = DataTracker.registerData(HGNetEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(STATE, 0);
    }

    public int getState() {
        return dataTracker.get(STATE);
    }

    @Override
    public void tick() {
        super.tick();
        if (master == null) kill();

        if (world.isClient) {
            world.addParticle(
                    ParticleTypes.FLAME,
                    this.getX() + random.nextFloat() - 0.5f,
                    this.getY() + random.nextFloat() - 0.5f,
                    this.getZ() + random.nextFloat() - 0.5f,
                    0.0, 0.0, 0.0);
        } else {

        }
    }

    @Override
    public int tickTimer() { return age; }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLOCK_LAVA_EXTINGUISH;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLOCK_LAVA_EXTINGUISH;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        return false;
    }

    public static DefaultAttributeContainer.Builder createNetAttributes() {
        return createLivingAttributes() // This must be used instead of DefaultAttributeContainer.builder() due to compatibility with step-height-entity-attribute
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 20)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0)
                .add(EntityAttributes.GENERIC_ARMOR, 10)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, 10);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        if (master == null) return;
        boolean ownerIsPlayer = master instanceof PlayerEntity;
        tag.putBoolean("playerOwner", ownerIsPlayer);
        if (ownerIsPlayer) tag.putUuid("ownerUUID", master.getUuid());
        else tag.putInt("ownerID", master.getId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        boolean ownerIsPlayer = tag.getBoolean("playerOwner");
        if (ownerIsPlayer) master = world.getPlayerByUuid(tag.getUuid("ownerUUID"));
        else master = (LivingEntity) world.getEntityById(tag.getInt("ownerID")); // Always is living
    }

    // Animations
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @SuppressWarnings("SameReturnValue")
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController<E> anim = event.getController();
        if (age < 5)
            anim.setAnimation(new AnimationBuilder().playOnce("animation.hg_nets.spawn"));
        else {
            switch (getState()) {
                case 0 -> anim.setAnimation(new AnimationBuilder().loop("animation.hg_nets.idle"));
                default -> {

                }
            }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
