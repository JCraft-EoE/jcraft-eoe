package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
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
import java.util.Set;

public class SandTornadoEntity extends LivingEntity implements GeoEntity, IOwnable {
    private static final TrackedData<Boolean> DISAPPEARED;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private LivingEntity master;
    private int hitsLeft = 5;

    static {
        DISAPPEARED = DataTracker.registerData(SandTornadoEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    }

    public SandTornadoEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    public boolean hasDisappeared() {
        return this.dataTracker.get(DISAPPEARED);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(DISAPPEARED, false);
    }

    @Override
    public LivingEntity getMaster() {
        return master;
    }

    public void setMaster(LivingEntity l) {
        this.master = l;
    }

    private void disappear() {
        dataTracker.set(DISAPPEARED, true);
        kill();
    }

    @Override
    public void tick() {
        super.tick();
        if (hasDisappeared()) return;

        Vec3d circulation = new Vec3d(MathHelper.sin(age * 0.25f) * 0.3f, 0.0, MathHelper.cos(age * 0.25f) * 0.3f);

        if (getWorld().isClient) {
            for (int i = 0; i < 3; i++)
                getWorld().addParticle(
                        new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.SAND.getDefaultState()),
                        getX() + random.nextFloat() - 0.5f,
                        getY() + random.nextFloat() * 2f,
                        getZ() + random.nextFloat() - 0.5f,
                        circulation.x, 0, circulation.z
                );
        } else if (age % 5 == 0) {
            if (master == null) {
                if (isAlive()) kill();
                return;
            }

            Set<LivingEntity> toHurt = JUtils.generateHitbox(getWorld(), getEyePos(), 1.8, Set.of(this, master));

            if (toHurt.isEmpty()) {
                setVelocity(getVelocity().add(getRotationVector().multiply(0.5)).multiply(0.4));
            } else {
                setVelocity(getVelocity().multiply(0.25));
                for (LivingEntity living : toHurt) {
                    LivingEntity target = JUtils.getUserIfStand(living);
                    if (target.isConnectedThroughVehicle(master)) return;
                    StandEntity.damageLogic(getWorld(), target, circulation, 10, 1, false, 2f, true, 6,
                            getWorld().getDamageSources().mobAttack(master), master, HitPropertyComponent.HitAnimation.MID, false);
                }
                hitsLeft--;
            }

            velocityModified = true;

            if (hitsLeft < 1 || getHealth() <= 0 || age >= 500)
                disappear();
        }
    }

    // Physical properties
    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return !damageSource.isOf(DamageTypes.OUT_OF_WORLD);
    }

    @Override
    protected void pushAway(Entity entity) {
    }

    @Override
    public void pushAwayFrom(Entity entity) {
    }

    @Override
    public boolean collidesWith(Entity other) {
        return false;
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLOCK_SAND_STEP;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLOCK_SAND_BREAK;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        return false;
    }

    public static DefaultAttributeContainer.Builder createTornadoAttributes() {
        return createLivingAttributes() // This must be used instead of DefaultAttributeContainer.builder() due to compatibility with step-height-entity-attribute
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                .add(EntityAttributes.GENERIC_ARMOR)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        if (master == null) return;
        boolean ownerIsPlayer = master instanceof PlayerEntity;
        tag.putBoolean("playerOwner", ownerIsPlayer);
        if (ownerIsPlayer)
            tag.putUuid("ownerUUID", master.getUuid());
        else
            tag.putInt("ownerID", master.getId());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        boolean ownerIsPlayer = tag.getBoolean("playerOwner");
        if (ownerIsPlayer)
            master = getWorld().getPlayerByUuid(tag.getUuid("ownerUUID"));
        else
            master = (LivingEntity) getWorld().getEntityById(tag.getInt("ownerID")); // Always is living
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return List.of();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public Arm getMainArm() {
        return null;
    }

    // Animations

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GeoAnimatable>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<GeoAnimatable> state) {
        return state.setAndContinue(RawAnimation.begin().thenLoop(hasDisappeared() ? "animation.sandtornado.disappear" : "animation.sandtornado.idle"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
