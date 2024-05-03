package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.arna.jcraft.common.entity.stand.TheSunEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.arna.jcraft.common.entity.stand.StandEntity.damageLogic;
import static net.arna.jcraft.common.util.JUtils.canDamage;

public class MeteorProjectile extends PersistentProjectileEntity implements GeoAnimatable {
    public static final TrackedData<Integer> SKIN;
    private int ticksInAir = 0;
    private int ticksInGround = 0;
    private TheSunEntity sun;
    boolean explosive = false;

    static {
        SKIN = DataTracker.registerData(MeteorProjectile.class, TrackedDataHandlerRegistry.INTEGER);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        dataTracker.startTracking(SKIN, 0);
    }

    public void assignSun(TheSunEntity sunEntity) {
        this.sun = sunEntity;
    }

    public int getSkin() {
        return dataTracker.get(SKIN);
    }

    public void setSkin(int skin) {
        dataTracker.set(SKIN, skin);
    }

    public MeteorProjectile(EntityType<? extends MeteorProjectile> entityType, World world) {
        super(entityType, world);
    }

    public MeteorProjectile(World world, LivingEntity owner) {
        super(JEntityTypeRegistry.METEOR, owner, world);
        this.setOwner(owner);
        this.pickupType = PickupPermission.DISALLOWED;
    }

    @Override
    public ItemStack asItemStack() {
        return new ItemStack(Items.AIR);
    }

    @Override
    protected boolean updateWaterState() {
        return false;
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.ITEM_FIRECHARGE_USE;
    }

    public void setExplosive(boolean explosive) {
        this.explosive = explosive;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity owner = getOwner();
        if (owner == null) return;
        Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner || entity == sun) return;

        if (getWorld().isClient) {
            // Hack that displays explosion without needing sync
            inGround = true;
            return;
        }

        entity.setOnFireFor(3);
        JUtils.projectileDamageLogic(this, getWorld(), entity, getVelocity(), 20, 1, false,
                6f, 10, HitPropertyComponent.HitAnimation.HIGH);
        if (explosive && ticksInGround < 1) {
            explode();
            playSound(getSound(), 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
            // Hack that prevents another explosion
            ticksInGround = 1;
        } else
            discard();
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (!getWorld().isClient()) {
            Direction movementDirection = getMovementDirection();
            BlockPos blockPos2 = getBlockPos(); //.offset(movementDirection);
            if (AbstractFireBlock.canPlaceAt(getWorld(), blockPos2, movementDirection)) {
                //world.playSound(null, blockPos2, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
                BlockState blockState2 = AbstractFireBlock.getState(getWorld(), blockPos2);
                getWorld().setBlockState(blockPos2, blockState2, 11);
            }
            MagiciansRedEntity.ignite(getWorld(), blockHitResult.getBlockPos());
        }
        super.onBlockHit(blockHitResult);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        tag.putShort("life", (short) this.ticksInAir);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        this.ticksInAir = tag.getShort("life");
    }

    @Override
    public void tick() {
        super.tick();

        if (getWorld().isClient()) {
            Vec3d vel = getVelocity();
            this.getWorld().addParticle(
                    getSkin() == 2 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                    getX() + random.nextFloat() * 0.5f - 0.25f,
                    getY() + random.nextFloat() * 0.5f - 0.25f,
                    getZ() + random.nextFloat() * 0.5f - 0.25f,
                    vel.x / 2, vel.y / 2, vel.z / 2
            );
        } else {
            if (this.inGround) {
                if (explosive && ticksInGround == 0)
                    explode();
                this.ticksInGround++;
                if (!explosive || ticksInGround > 10) {
                    discard();
                    return;
                }
            } else {
                this.ticksInAir++;
                if (ticksInAir >= 600) {
                    discard();
                    return;
                }
            }

            if (!(getOwner() instanceof LivingEntity)) {
                discard();
                return;
            }

            TheSunEntity.dryOut((ServerWorld) getWorld(), getBlockPos());
        }
    }

    private void explode() {
        Entity owner = getOwner();
        Set<Entity> filter = new HashSet<>();
        filter.add(owner);
        filter.add(this);

        List<LivingEntity> hurtAll = new ArrayList<>(JUtils.generateHitbox(getWorld(), getPos(), 2, filter));
        hurtAll.removeIf(e -> !canDamage(JDamageSources.create(getWorld(), DamageTypes.ON_FIRE), e));

        if (!hurtAll.isEmpty()) {
            for (LivingEntity l : hurtAll) {
                LivingEntity target = JUtils.getUserIfStand(l);
                damageLogic(getWorld(), target, l.getPos().subtract(getPos()).normalize(), 20, 3, false, 5f,
                        false, 10, JDamageSources.create(getWorld(), DamageTypes.ON_FIRE), owner, HitPropertyComponent.HitAnimation.LAUNCH);
            }
        }
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    // Animations
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GeoAnimatable>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<GeoAnimatable> state) {
        if (inGround)
            state.getController().setAnimation(RawAnimation.begin().thenPlay("animation.meteor.explode"));
        else
            state.getController().setAnimation(RawAnimation.begin().thenPlay("animation.meteor.spawn")
                    .thenLoop("animation.meteor.idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object object) {
        return 0;
    }
}
