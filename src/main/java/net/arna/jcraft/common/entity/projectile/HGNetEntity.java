package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.ICustomDamageHandler;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JSoundRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
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
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class HGNetEntity extends JAttackEntity implements GeoEntity, ICustomDamageHandler {
    public static final TrackedData<Integer> SKIN;
    public static final TrackedData<Integer> STATE;
    public static final TrackedData<Boolean> CHARGED;

    private int animTimer = 0;
    private Vec3d target;

    private int lifeTime = 30 * 20 + 20;

    private static final int FIRE_COOLDOWN = 10 * 20;
    private static final int CONSTRICT_COOLDOWN = 10 * 20;
    private int fireCooldown = 0, constrictCooldown = 0;

    private boolean finalAttack = false;

    public HGNetEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    static {
        STATE = DataTracker.registerData(HGNetEntity.class, TrackedDataHandlerRegistry.INTEGER);
        SKIN = DataTracker.registerData(HGNetEntity.class, TrackedDataHandlerRegistry.INTEGER);
        CHARGED = DataTracker.registerData(HGNetEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(SKIN, 0);
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

    public int getSkin() {
        return dataTracker.get(SKIN);
    }

    public void setState(int state) {
        if (getState() != state)
            dataTracker.set(STATE, state);
    }

    public void setSkin(int skin) {
        dataTracker.set(SKIN, skin);
    }

    public void tryFireAt(Vec3d target, boolean finalAttack) {
        if (isCharged() && JUtils.canAct(this) && getState() != 2) {
            playSound(JSoundRegistry.HG_SPLASH, 1, 1);
            this.target = target;
            fireCooldown = FIRE_COOLDOWN;
            setCharged(false);

            if (finalAttack) {
                this.finalAttack = true;
                animTimer = 50;
            } else {
                animTimer = 25;
            }
        }
    }

    @Override
    public void tick() {
        if (getBlockStateAtPos().isOpaque())
            setVelocity(0, 0, 0);

        super.tick();

        if (!getWorld().isClient) {
            if (--lifeTime <= 0 || master == null) {
                discard();
                return;
            }

            if (lifeTime <= 20) {
                setState(3);
                return;
            }

            if (JUtils.canAct(this)) {
                Vec3d upVec = GravityChangerAPI.getEyeOffset(this);

                if (age == 1) {
                    Vec3d launchVec = upVec.multiply(0.2);

                    JUtils.displayHitbox(getWorld(), getBoundingBox());
                    getInsideEntities().forEach(
                            living -> {
                                if (!living.isConnectedThroughVehicle(master))
                                    StandEntity.damageLogic(
                                            getWorld(), living, launchVec, 15, 3, false, 5f, false, 10,
                                            getWorld().getDamageSources().mobAttack(this), master, HitPropertyComponent.HitAnimation.HIGH
                                    );
                            }
                    );
                }

                if (getState() == 2) {
                    if (animTimer == 0) {
                        JUtils.displayHitbox(getWorld(), getBoundingBox());
                        getInsideEntities().forEach(
                                living -> {
                                    if (!JUtils.isBlocking(living) && !living.isConnectedThroughVehicle(master))
                                        StandEntity.stun(living, 17, 0);
                                }
                        );
                    } else if (animTimer <= -20)
                        setState(0);
                } else {
                    if (animTimer > 0) {
                        if (animTimer % 8 == 0) {
                            for (int i = 0; i < 3; i++) {
                                EmeraldProjectile emerald = new EmeraldProjectile(getWorld(), getMaster());

                                Vec3d heightOffset = upVec.multiply(0.8);
                                Vec3d emeraldPos = getPos().add(heightOffset).add(JUtils.randUnitVec(getRandom()));
                                emerald.setPosition(emeraldPos);
                                emerald.setVelocity(target.subtract(emeraldPos).normalize().multiply(1.5));
                                if (finalAttack)
                                    emerald.withReflect();

                                getWorld().spawnEntity(emerald);
                            }
                        }
                    } else if (finalAttack)
                        lifeTime = 20;
                }
            } else {
                if (animTimer > 0) {
                    setState(0);
                    animTimer = 0;
                }
            }

            if (--fireCooldown < 0)
                setCharged(true);
            constrictCooldown--;
            animTimer--;
        }

        //JCraft.LOGGER.info("STATE: " + getState() + " ltime: " + lifeTime);
    }

    private List<LivingEntity> getInsideEntities() {
        return getWorld().getEntitiesByClass(LivingEntity.class, getBoundingBox(),
                EntityPredicates.VALID_LIVING_ENTITY.and(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR).and(entity -> !entity.equals(this)));
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.isOf(DamageTypes.IN_WALL))
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
        if (!JUtils.canAct(this)) return;
        if (entity == null) return;
        if (master == null || entity.isConnectedThroughVehicle(master)) return;
        if (entity instanceof JAttackEntity attackEntity && attackEntity.getMaster() == master) return;
        if (entity instanceof StandEntity<?,?> stand && stand.getUser() == master) return;

        // Not constricting or dying
        if (getState() < 2 && constrictCooldown <= 0) {
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
        if (effect.getEffectType() == JStatusRegistry.DAZED)
            return super.addStatusEffect(effect, source);
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
    public boolean reflectsDamage() {
        return false;
    }

    @Override
    public boolean handleDamage(Vec3d kbVec, int stunTicks, int stunLevel, boolean overrideStun, float damage, boolean lift, int blockstun, DamageSource source, Entity attacker, HitPropertyComponent.HitAnimation hitAnimation, boolean canBackstab, boolean unblockable) {
        if (attacker == master || (attacker instanceof IOwnable ownable && ownable.getMaster() == master))
            return false;
        return true;
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
        if (ownerIsPlayer) master = getWorld().getPlayerByUuid(tag.getUuid("ownerUUID"));
        else master = (LivingEntity) getWorld().getEntityById(tag.getInt("ownerID")); // Always is living
    }

    // Animations
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GeoAnimatable>(this, "controller", 6, this::predicate));
    }

    private PlayState predicate(AnimationState<GeoAnimatable> state) {
        if (age < 5)
            state.setAnimation(RawAnimation.begin().thenPlay("animation.hg_nets.spawn"));
        else {
            if (getState() == 3) {
                state.setAnimation(RawAnimation.begin().thenPlay("animation.hg_nets.wilt"));
            } else if (getState() == 2) {
                state.setAnimation(RawAnimation.begin().thenPlay("animation.hg_nets.constrict"));
            } else {
                state.setAnimation(RawAnimation.begin().thenLoop("animation.hg_nets.idle"));
            }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
