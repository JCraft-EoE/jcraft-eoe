package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.IOwnable;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Arm;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
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

public class BlockProjectile extends LivingEntity implements IOwnable, GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final int maxTimeToLaunch = 15;
    private int timeToLaunch = maxTimeToLaunch;
    private int timeLaunched = 0;
    private boolean toRefresh = false;
    private boolean launched = false;
    private boolean hit = false;

    private static final TrackedData<Integer> EFFECT;
    private static final TrackedData<ItemStack> BLOCKSTACK;

    static {
        EFFECT = DataTracker.registerData(BlockProjectile.class, TrackedDataHandlerRegistry.INTEGER);
        BLOCKSTACK = DataTracker.registerData(BlockProjectile.class, TrackedDataHandlerRegistry.ITEM_STACK);
    }

    public BlockProjectile(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        setNoGravity(true);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(EFFECT, 0);
        dataTracker.startTracking(BLOCKSTACK, Items.STONE.getDefaultStack());
    }

    public void setBlockStack(ItemStack stack) {
        dataTracker.set(BLOCKSTACK, stack);
    }

    public void setEffect(int effect) {
        dataTracker.set(EFFECT, effect);
    }

    public void markRefresh() {
        toRefresh = true;
    }

    private void breakBlock() {
        setPosition(getPos().add(getVelocity()));
        setVelocity(0, 0, 0);
        setEffect(1);
        setNoDrag(false);
        kill();
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClient) {
            Vec3d vel = getVelocity();
            int effect = dataTracker.get(EFFECT);
            if (effect != 0) {
                for (int i = 0; i < 32; i++) {
                    getWorld().addParticle(
                            effect == 1 ? new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.STONE.getDefaultState()) : ParticleTypes.REVERSE_PORTAL,
                            getX() + vel.x + random.nextDouble() - 0.5,
                            getY() + vel.y + random.nextDouble() - 0.5,
                            getZ() + vel.z + random.nextDouble() - 0.5,
                            vel.x + random.nextDouble() * 2 - 1,
                            vel.y + random.nextDouble() * 2 - 1,
                            vel.z + random.nextDouble() * 2 - 1
                    );
                }
            }
            getWorld().addParticle(ParticleTypes.REVERSE_PORTAL,
                    getX() + random.nextDouble() - 0.5,
                    getY() + random.nextDouble() - 0.5,
                    getZ() + random.nextDouble() - 0.5,
                    vel.x / 2,
                    vel.y / 2,
                    vel.z / 2
            );
        } else {
            if (master == null || deathTime > 1) {
                discard();
                return;
            }

            if (dataTracker.get(EFFECT) != 0)
                setEffect(0);

            if (hit || isOnGround() || age > 200) // Placing this here delays it by 1 tick, allowing the client to see the proper end position
                breakBlock();

            timeToLaunch--;
            if (timeToLaunch == 0) {
                if (toRefresh) {
                    timeToLaunch = maxTimeToLaunch;
                    toRefresh = false;
                    setVelocity(0, 0, 0);
                    setEffect(2);
                    playSound(JSoundRegistry.CMOON_BLOCKHALT, 1, 1);
                } else if (!launched) {
                    Vec3d targetPos;
                    Vec3d eP = master.getEyePos();
                    Vec3d rangeMod = master.getRotationVector().multiply(32);
                    EntityHitResult eHit = ProjectileUtil.raycast(master, eP, eP.add(rangeMod),
                            master.getBoundingBox().expand(32),
                            EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(entity -> entity != this),
                            1024 // Squared
                    );

                    if (eHit != null) {
                        targetPos = eHit.getPos();
                    } else {
                        targetPos = getWorld().raycast(
                                new RaycastContext(eP, eP.add(rangeMod), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, master)
                        ).getPos();
                    }

                    setVelocity(targetPos.subtract(getPos()).normalize()); //.multiply(1)

                    playSound(JSoundRegistry.CMOON_BLOCKLAUNCH, 1, 1);
                    launched = true;
                    setNoDrag(true);
                }
            }

            if (launched && timeLaunched < 20 && !hit) {
                timeLaunched++;
                Set<LivingEntity> toHurt = JUtils.generateHitbox(getWorld(), getPos(), 1, Set.of(master));
                DamageSource damageSource = getWorld().getDamageSources().mobAttack(master);
                for (LivingEntity living : toHurt) {
                    LivingEntity target = JUtils.getUserIfStand(living);
                    if (target == master || target == this || !JUtils.canDamage(damageSource, target)) continue;
                    hit = true;
                    StandEntity.damageLogic(getWorld(), target, getVelocity(), 15, 1, true,
                            6, false, 11, damageSource, master, HitPropertyComponent.HitAnimation.MID, false);
                }
            }

            if (timeLaunched == 20) setNoGravity(false);
        }
    }

    public static DefaultAttributeContainer.Builder createBlockAttributes() {
        return createLivingAttributes() // This must be used instead of DefaultAttributeContainer.builder() due to compatibility with step-height-entity-attribute
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED)
                .add(EntityAttributes.GENERIC_ARMOR, 10)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
    }

    @Override
    public void pushAwayFrom(Entity entity) {
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (source.getSource() != null) return false;
        return super.damage(source, amount);
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BLOCK_STONE_STEP;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BLOCK_STONE_BREAK;
    }

    @Override
    protected Box calculateBoundingBox() { // Centered around 0,0,0 instead of 0,0.5,0
        double x = getX();
        double y = getY();
        double z = getZ();
        double s = 0.5;
        return new Box(x + s, y + s, z + s, x - s, y - s, z - s);
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        return false;
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
        return dataTracker.get(BLOCKSTACK);
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public Arm getMainArm() {
        return null;
    }

    private LivingEntity master;

    @Override
    public LivingEntity getMaster() {
        return master;
    }

    public void setMaster(LivingEntity l) {
        this.master = l;
    }

    // Animations

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GeoAnimatable>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<GeoAnimatable> state) {
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.block.idle"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
