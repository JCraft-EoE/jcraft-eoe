package net.arna.jcraft.api.spec;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.constant.DefaultAnimations;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.player.CommonSpecComponent;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.events.JServerEvents;
import net.arna.jcraft.common.food.IFoodData;
import net.arna.jcraft.common.tickable.JEnemies;
import net.arna.jcraft.common.util.DashData;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static net.arna.jcraft.api.stand.StandTypeUtil.generateStandTypeForMob;

public class SpecUserMob extends PathfinderMob implements JSpecHolder, GeoEntity, IFoodData {
    // TODO: add metallica anims to the player anims these guys use
    // TODO: healing
    // TODO: anubis-specific sheathing

    protected final CommonSpecComponent component;
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> ANIMATION_RESET = SynchedEntityData.defineId(SpecUserMob.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(SpecUserMob.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Float> ANIMATION_SPEED = SynchedEntityData.defineId(SpecUserMob.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(SpecUserMob.class, EntityDataSerializers.INT);

    protected FoodData foodData = null;

    public SpecUserMob(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);

        component = JComponentPlatformUtils.getSpecData(this);

        if (level.isClientSide()) return;
        JEnemies.add(this);
    }

    @Override
    public void tick() {
        setSpeed((float) getAttribute(Attributes.MOVEMENT_SPEED).getValue());

        super.tick();

        if (level().isClientSide()) return;

        entityData.set(ANIMATION_RESET, false);

        JSpec<?, ?> spec = getSpec();
        if (spec != null) {
            final LivingEntity target = getTarget();

            if (target != null) {
                lookAt(EntityAnchorArgument.Anchor.EYES, target.getEyePosition());

                if (target.distanceToSqr(this) > 1.0) {
                    Vec3 posTowards = DefaultRandomPos.getPosTowards(this, 2, 7, target.position(), 1.5707963705062866);
                    if (posTowards != null) getNavigation().moveTo(posTowards.x, posTowards.y, posTowards.z, 1.0);
                }
            }

            spec.tickSpec();

            // TODO: make animations run once without needing to be reset like this (like player animator)
            if (spec.getMoveStun() <= 0 && !DashData.isDashing(this)) setAnimation("", 1.0f);
        } else {
            setAnimation("", 1.0f);
        }
    }

    protected int getInitialVariant() { return 0; }

    public void setVariant(int variant) {
        entityData.set(VARIANT, variant);
    }

    public int getVariant() {
        return entityData.get(VARIANT);
    }

    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(3, new RandomStrollGoal(this, 0.6));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(10, new OpenDoorGoal(this, true));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        if (compound.contains("Reroll")) {
            reroll(compound.getCompound("Reroll"));
        }
    }

    protected void rerollVariant() {
        setVariant(random.nextInt(3));
    }

    protected void rerollStand(float standChance) {
        final var standComponent = JComponentPlatformUtils.getStandComponent(this);

        if (random.nextFloat() * 100.0f > standChance) {
            standComponent.setType(JStandTypeRegistry.NONE.get());
            return;
        }

        standComponent.setType(generateStandTypeForMob(this, level().getGameRules()));

        if (random.nextFloat() > 0.9f) {
            standComponent.setSkin(random.nextInt(3));
        }
    }

    protected void rerollSpec() {
        component.setType(SpecTypeUtil.getRandom(random));
    }

    protected void rerollEquipment() {
        JServerEvents.armorMob(this);
    }

    protected void reroll(CompoundTag nbt) {
        if (nbt.contains("Variant")) {
            rerollVariant();
        }

        if (nbt.contains("Stand")) {
            float standChance = nbt.contains("StandChance")
                    ? nbt.getFloat("StandChance")
                    : level().getGameRules().getInt(JCraft.CHANCE_MOB_SPAWNS_WITH_STAND);
            rerollStand(standChance);
        }

        if (nbt.contains("Spec")) {
            rerollSpec();
        }

        if (nbt.contains("Equipment")) {
            rerollEquipment();
        }

        nbt.remove("Reroll");
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ANIMATION, "");
        entityData.define(ANIMATION_SPEED, 1.0f);
        entityData.define(ANIMATION_RESET, false);
        entityData.define(VARIANT, getInitialVariant());
    }

    @Override
    public @Nullable FoodData getFoodData() {
        return foodData;
    }

    @Override
    public void setSpecType(SpecType type) {
        component.setType(type);
    }

    @Override
    public JSpec<?, ?> getSpec() {
        return component.getSpec();
    }

    @Override
    public void setAnimation(String animationID, float animationSpeed) {
        entityData.set(ANIMATION_RESET, entityData.get(ANIMATION).equals(animationID));

        entityData.set(ANIMATION, animationID);
        entityData.set(ANIMATION_SPEED, animationSpeed);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkIdleController(this));
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private static final Map<String, RawAnimation> cachedAnimations = new HashMap<>();
    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> state) {
        if (entityData.get(ANIMATION_RESET)) {
            state.resetCurrentAnimation();
        }

        final String animation = entityData.get(ANIMATION);

        if (animation.isEmpty()) return PlayState.STOP;
        if (!cachedAnimations.containsKey(animation)) cachedAnimations.put(animation, RawAnimation.begin().thenLoop(animation));

        final float speed = entityData.get(ANIMATION_SPEED);
        state.setControllerSpeed(speed);

        return state.setAndContinue(cachedAnimations.get(animation));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
