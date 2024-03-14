package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
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

import java.util.List;

public class HGNetEntity extends JAttackEntity implements IAnimatable, IAnimationTickable {
    public static final TrackedData<Integer> STATE;
    public static final TrackedData<Boolean> CHARGED;

    private int animTimer = 0;
    private Vec3d target;

    private int lifeTime = 30 * 20;

    private static final int FIRE_COOLDOWN = 10 * 20;
    private static final int CONSTRICT_COOLDOWN = 10 * 20;
    private int fireCooldown = 0, constrictCooldown = 0;

    public HGNetEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    static {
        STATE = DataTracker.registerData(HGNetEntity.class, TrackedDataHandlerRegistry.INTEGER);
        CHARGED = DataTracker.registerData(HGNetEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(STATE, 0);
        dataTracker.startTracking(CHARGED, true);
    }

    public boolean isCharged() {
        return dataTracker.get(CHARGED);
    }

    public void setCharged(boolean charged) {
        if (isCharged() != charged)
            dataTracker.set(CHARGED, charged);
    }

    public int getState() {
        return dataTracker.get(STATE);
    }

    public void setState(int state) {
        if (getState() != state)
            dataTracker.set(STATE, state);
    }

    public void tryFireAt(Vec3d target) {
        if (isCharged()) {
            animTimer = 25;
            this.target = target;
            fireCooldown = FIRE_COOLDOWN;
            setCharged(false);
        }
    }

    @Override
    public void tick() {
        if (getBlockStateAtPos().isOpaque())
            setVelocity(0, 0, 0);

        super.tick();

        if (!world.isClient) {
            if (--lifeTime <= 0 || master == null) {
                discard();
                return;
            }

            Vec3d upVec = GravityChangerAPI.getEyeOffset(this);

            if (age == 1) {
                Vec3d launchVec = upVec.multiply(0.2);

                JUtils.displayHitbox(world, getBoundingBox());
                getInsideEntities().forEach(
                        living -> {
                            if (!living.isConnectedThroughVehicle(master))
                                StandEntity.damageLogic(
                                        world, living, launchVec, 15, 3, false, 5f, false, 10,
                                        DamageSource.mob(this), this, HitPropertyComponent.HitAnimation.HIGH
                                );
                        }
                );
            }

            if (getState() == 2) {
                if (animTimer == 0) {
                    JUtils.displayHitbox(world, getBoundingBox());
                    getInsideEntities().forEach(
                            living -> {
                                if (!JUtils.isBlocking(living) && !living.isConnectedThroughVehicle(master))
                                    StandEntity.stun(living, 17, 0);
                            }
                    );
                } else if (animTimer <= -16)
                    setState(0);
            } else {
                if (animTimer > 0 && animTimer % 8 == 0)
                    for (int i = 0; i < 3; i++) {
                        EmeraldProjectile emerald = new EmeraldProjectile(world, getMaster());

                        Vec3d heightOffset = upVec.multiply(0.8);
                        Vec3d emeraldPos = getPos().add(heightOffset).add(JUtils.randUnitVec(getRandom()));
                        emerald.setPosition(emeraldPos);

                        emerald.setVelocity(target.subtract(emeraldPos).normalize().multiply(1.5));

                        world.spawnEntity(emerald);
                    }
            }

            if (--fireCooldown < 0)
                setCharged(true);
            constrictCooldown--;
            animTimer--;
        }
    }

    private List<LivingEntity> getInsideEntities() {
        return world.getEntitiesByClass(LivingEntity.class, getBoundingBox(),
                EntityPredicates.VALID_LIVING_ENTITY.and(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR).and(entity -> !entity.equals(this)));
    }

    @Override
    public int tickTimer() { return age; }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source == DamageSource.IN_WALL)
            return false;
        return super.damage(source, amount);
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        tryConstrict(entity);
    }

    @Override
    public void pushAway(Entity entity) {
        tryConstrict(entity);
    }

    private void tryConstrict(Entity entity) {
        if (entity == null || master == null || entity.isConnectedThroughVehicle(master)) return;
        if (entity instanceof JAttackEntity attackEntity && attackEntity.getMaster() == master) return;

        if (getState() != 2 && constrictCooldown <= 0) {
            setState(2);
            constrictCooldown = CONSTRICT_COOLDOWN;
            animTimer = 6;
        }
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SLIME_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLOCK_CHORUS_FLOWER_DEATH;
    }

    @Override
    public boolean hasNoGravity() {
        return false;
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        return false;
    }

    @Override
    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
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
        tag.putInt("lifeTime", lifeTime);

        if (master == null) return;
        boolean ownerIsPlayer = master instanceof PlayerEntity;
        tag.putBoolean("playerOwner", ownerIsPlayer);
        if (ownerIsPlayer) tag.putUuid("ownerUUID", master.getUuid());
        else tag.putInt("ownerID", master.getId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        lifeTime = tag.getInt("lifeTime");

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
                case 2 -> anim.setAnimation(new AnimationBuilder().playOnce("animation.hg_nets.constrict"));
                default -> anim.setAnimation(new AnimationBuilder().loop("animation.hg_nets.idle"));
            }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
