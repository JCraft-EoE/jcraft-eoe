package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.MoveUsage;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.ICustomDamageHandler;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.arna.jcraft.api.Attacks.damageLogic;

public class HGNetEntity extends JAttackEntity implements ICustomDamageHandler {
    public static final EntityDataAccessor<Integer> SKIN;
    public static final EntityDataAccessor<Integer> STATE;
    public static final EntityDataAccessor<Boolean> CHARGED;

    private int animTimer = 0;
    private Vec3 target;

    private int lifeTime = 30 * 20 + 20;

    private static final int FIRE_COOLDOWN = 10 * 20;
    private static final int CONSTRICT_COOLDOWN = 10 * 20;
    private int fireCooldown = 0, constrictCooldown = 0;

    private boolean finalAttack = false;

    public HGNetEntity(Level world) {
        super(JEntityTypeRegistry.HG_NET.get(), world);
    }

    static {
        STATE = SynchedEntityData.defineId(HGNetEntity.class, EntityDataSerializers.INT);
        SKIN = SynchedEntityData.defineId(HGNetEntity.class, EntityDataSerializers.INT);
        CHARGED = SynchedEntityData.defineId(HGNetEntity.class, EntityDataSerializers.BOOLEAN);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SKIN, 0);
        entityData.define(STATE, 0);
        entityData.define(CHARGED, true);
    }

    public boolean isCharged() {
        return entityData.get(CHARGED);
    }

    public void setCharged(boolean charged) {
        if (isCharged() != charged) {
            entityData.set(CHARGED, charged);
        }
    }

    public int getState() {
        return entityData.get(STATE);
    }

    public int getSkin() {
        return entityData.get(SKIN);
    }

    public void setState(int state) {
        if (getState() != state) {
            entityData.set(STATE, state);
        }
    }

    public void setSkin(int skin) {
        entityData.set(SKIN, skin);
    }

    public void tryFireAt(Vec3 target, boolean finalAttack) {
        if (isCharged() && JUtils.canAct(this) && getState() != 2) {
            playSound(JSoundRegistry.HG_SPLASH.get(), 1, 1);
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
        if (getFeetBlockState().canOcclude()) {
            setDeltaMovement(0, 0, 0);
        }

        super.tick();

        if (!level().isClientSide) {
            if (--lifeTime <= 0 || master == null) {
                discard();
                return;
            }

            if (lifeTime <= 20) {
                setState(3);
                return;
            }

            if (JUtils.canAct(this)) {
                final Vec3 upVec = GravityChangerAPI.getEyeOffset(this);

                if (tickCount == 1) {
                    final Vec3 launchVec = upVec.scale(0.2);

                    JUtils.displayHitbox(level(), getBoundingBox());
                    getInsideEntities().forEach(
                            living -> {
                                if (!living.isPassengerOfSameVehicle(master)) {
                                    damageLogic(
                                            level(), living, launchVec, 15, 3, false, 5f, false, 10,
                                            level().damageSources().mobAttack(this), master, CommonHitPropertyComponent.HitAnimation.HIGH
                                    );
                                }
                            }
                    );
                }

                if (getState() == 2) {
                    if (animTimer == 0) {
                        JUtils.displayHitbox(level(), getBoundingBox());
                        getInsideEntities().forEach(
                                living -> {
                                    if (!JUtils.isBlocking(living) && !living.isPassengerOfSameVehicle(master)) {
                                        JCraft.stun(living, 17, 0, master);
                                    }
                                }
                        );
                    } else if (animTimer <= -20) {
                        setState(0);
                    }
                } else {
                    if (animTimer > 0) {
                        if (animTimer % 8 == 0) {
                            for (int i = 0; i < 3; i++) {
                                final EmeraldProjectile emerald = new EmeraldProjectile(level(), getMaster());

                                final Vec3 heightOffset = upVec.scale(0.8);
                                final Vec3 emeraldPos = position().add(heightOffset).add(JUtils.randUnitVec(getRandom()));
                                emerald.setPos(emeraldPos);
                                emerald.setDeltaMovement(target.subtract(emeraldPos).normalize().scale(1.5));
                                if (finalAttack) {
                                    emerald.withReflect();
                                }

                                level().addFreshEntity(emerald);
                            }
                        }
                    } else if (finalAttack) {
                        lifeTime = 20;
                    }
                }
            } else {
                if (animTimer > 0) {
                    setState(0);
                    animTimer = 0;
                }
            }

            if (--fireCooldown < 0) {
                setCharged(true);
            }
            constrictCooldown--;
            animTimer--;
        }

        //JCraft.LOGGER.info("STATE: " + getState() + " ltime: " + lifeTime);
    }

    private List<LivingEntity> getInsideEntities() {
        return level().getEntitiesOfClass(LivingEntity.class, getBoundingBox(),
                EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_CREATIVE_OR_SPECTATOR).and(entity -> !entity.equals(this)));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void push(@NonNull Entity entity) {
        tryConstrict(entity);
    }

    @Override
    public void doPush(@NonNull Entity entity) {
        tryConstrict(entity);
    }

    private void tryConstrict(Entity entity) {
        if (entity == null) {
            return;
        }
        if ((entity instanceof Player player && (player.isCreative() || player.isSpectator()))) {
            return;
        }
        if (!JUtils.canAct(this)) {
            return;
        }
        if (master == null || entity.isPassengerOfSameVehicle(master)) {
            return;
        }
        if (entity instanceof JAttackEntity attackEntity && attackEntity.getMaster() == master) {
            return;
        }
        if (entity instanceof StandEntity<?, ?> stand && stand.getUser() == master) {
            return;
        }

        // Not constricting or dying
        if (getState() < 2 && constrictCooldown <= 0) {
            setState(2);
            constrictCooldown = CONSTRICT_COOLDOWN;
            animTimer = 6;
        }
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NonNull DamageSource source) {
        return SoundEvents.SLIME_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CHORUS_FLOWER_DEATH;
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }

    @Override
    public boolean startRiding(@NonNull Entity entity, boolean force) {
        return false;
    }

    @Override
    public boolean addEffect(MobEffectInstance effect, @Nullable Entity source) {
        if (effect.getEffect() == JStatusRegistry.DAZED.get()) {
            return super.addEffect(effect, source);
        }
        return false;
    }

    public static AttributeSupplier.Builder createNetAttributes() {
        return createLivingAttributes() // This must be used instead of DefaultAttributeContainer.builder() due to compatibility with step-height-entity-attribute
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.KNOCKBACK_RESISTANCE, 20)
                .add(Attributes.MOVEMENT_SPEED, 0)
                .add(Attributes.ARMOR, 10)
                .add(Attributes.ARMOR_TOUGHNESS, 10);
    }

    @Override
    public boolean reflectsDamage() {
        return false;
    }

    @Override
    public boolean handleDamage(Vec3 kbVec, int stunTicks, int stunLevel, boolean overrideStun, float damage,
                                boolean lift, int blockstun, DamageSource source, Entity attacker, CommonHitPropertyComponent.HitAnimation hitAnimation,
                                MoveUsage moveUsage, boolean canBackstab, boolean unblockable) {
        if (attacker == master || (attacker instanceof IOwnable ownable && ownable.getMaster() == master)) {
            return false;
        }
        return true;
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("lifeTime", lifeTime);
        writeMasterNbt(tag);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        lifeTime = tag.getInt("lifeTime");
        readMasterNbt(tag);
    }

    public static final AzCommand SPAWN = AzCommand.create( JCraft.BASE_CONTROLLER, "animation.hg_nets.spawn");
    public static final AzCommand WILT = AzCommand.create( JCraft.BASE_CONTROLLER, "animation.hg_nets.wilt");
    public static final AzCommand CONSTRICT = AzCommand.create( JCraft.BASE_CONTROLLER, "animation.hg_nets.constrict");
    public static final AzCommand IDLE = AzCommand.create( JCraft.BASE_CONTROLLER, "animation.hg_nets.idle", AzPlayBehaviors.LOOP);

    // Animations
    /*
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 6, this::predicate));
    }

    private static final RawAnimation SPAWN = RawAnimation.begin().thenPlay("animation.hg_nets.spawn");
    private static final RawAnimation WILT = RawAnimation.begin().thenPlay("animation.hg_nets.wilt");
    private static final RawAnimation CONSTRICT = RawAnimation.begin().thenPlay("animation.hg_nets.constrict");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.hg_nets.idle");

    private PlayState predicate(AnimationState<GeoAnimatable> state) {
        if (tickCount < 5) {
            state.setAnimation(SPAWN);
        } else {
            if (getState() == 3) {
                state.setAnimation(WILT);
            } else if (getState() == 2) {
                state.setAnimation(CONSTRICT);
            } else {
                state.setAnimation(IDLE);
            }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }*/
}
