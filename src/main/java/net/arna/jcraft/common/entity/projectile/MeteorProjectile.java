package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.common.component.living.HitPropertyComponent;
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
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.arna.jcraft.common.entity.stand.StandEntity.damageLogic;
import static net.arna.jcraft.common.util.JUtils.canDamage;

public class MeteorProjectile extends PersistentProjectileEntity implements IAnimatable {
    public static final TrackedData<Integer> SKIN;
    private static final DamageSource damageSource = DamageSource.ON_FIRE;
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

        if (world.isClient) {
            // Hack that displays explosion without needing sync
            inGround = true;
            return;
        }

        entity.setOnFireFor(3);
        JUtils.projectileDamageLogic(this, world, entity, getVelocity(), 20, 1, false,
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
        Direction movementDirection = getMovementDirection();
        BlockPos blockPos2 = getBlockPos(); //.offset(movementDirection);
        if (AbstractFireBlock.canPlaceAt(world, blockPos2, movementDirection)) {
            //world.playSound(null, blockPos2, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
            BlockState blockState2 = AbstractFireBlock.getState(world, blockPos2);
            world.setBlockState(blockPos2, blockState2, 11);
        }
        MagiciansRedEntity.ignite(getWorld(), blockHitResult.getBlockPos());
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

        if (world.isClient()) {
            Vec3d vel = getVelocity();
            this.world.addParticle(
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

            TheSunEntity.dryOut((ServerWorld) world, getBlockPos());
        }
    }

    private void explode() {
        Entity owner = getOwner();
        Set<Entity> filter = new HashSet<>();
        filter.add(owner);
        filter.add(this);

        List<LivingEntity> hurtAll = new ArrayList<>(JUtils.generateHitbox(world, getPos(), 2, filter));
        hurtAll.removeIf(e -> !canDamage(damageSource, e));

        if (!hurtAll.isEmpty()) {
            for (LivingEntity l : hurtAll) {
                LivingEntity target = JUtils.getUserIfStand(l);
                damageLogic(world, target, l.getPos().subtract(getPos()).normalize(), 20, 3, false, 5f,
                        false, 10, damageSource, owner, HitPropertyComponent.HitAnimation.LAUNCH);
            }
        }
    }

    @Override
    public boolean isFireImmune() {
        return true;
    }

    // Animations
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @SuppressWarnings("SameReturnValue")
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (inGround)
            event.getController().setAnimation(new AnimationBuilder().playOnce("animation.meteor.explode"));
        else
            event.getController().setAnimation(new AnimationBuilder().playOnce("animation.meteor.spawn")
                    .loop("animation.meteor.idle"));
        return PlayState.CONTINUE;
    }
}
