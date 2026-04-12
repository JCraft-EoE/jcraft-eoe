package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.animation.play_behavior.AzPlayBehaviors;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.arna.jcraft.common.entity.stand.TheSunEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;
import static net.arna.jcraft.common.util.JUtils.canDamage;

public class MeteorProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Integer> SKIN;
    private int ticksInAir = 0;
    private int ticksInGround = 0;
    private final @Nullable TheSunEntity sun;
    private boolean explosive = false;

    static {
        SKIN = SynchedEntityData.defineId(MeteorProjectile.class, EntityDataSerializers.INT);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SKIN, 0);
    }

    public int getSkin() {
        return entityData.get(SKIN);
    }

    public void setSkin(int skin) {
        entityData.set(SKIN, skin);
    }

    public MeteorProjectile(Level world) {
        super(JEntityTypeRegistry.METEOR.get(), world);
        this.sun = null;
    }

    public MeteorProjectile(Level world, LivingEntity owner, @Nullable TheSunEntity sun) {
        super(JEntityTypeRegistry.METEOR.get(), owner, world);
        this.sun = sun;
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    public @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean updateInWaterStateAndDoFluidPushing() {
        return false;
    }

    @Override
    protected @NonNull SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.FIRECHARGE_USE;
    }

    public void setExplosive(boolean explosive) {
        this.explosive = explosive;
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult entityHitResult) {
        Entity owner = getOwner();
        if (owner == null) {
            return;
        }
        Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner || entity == sun) {
            return;
        }

        if (level().isClientSide) {
            // Hack that displays explosion without needing sync
            inGround = true;
            return;
        }

        entity.setSecondsOnFire(3);
        JUtils.projectileDamageLogic(this, level(), entity, getDeltaMovement(), 20, 1, false,
                6f, 10, CommonHitPropertyComponent.HitAnimation.HIGH);
        if (explosive && ticksInGround < 1) {
            explode();
            playSound(getHitGroundSoundEvent(), 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
            // Hack that prevents another explosion
            ticksInGround = 1;
        } else {
            discard();
        }
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult blockHitResult) {
        if (!level().isClientSide()) {
            final Direction movementDirection = getMotionDirection();
            final BlockPos blockPos2 = blockPosition(); //.offset(movementDirection);
            if (BaseFireBlock.canBePlacedAt(level(), blockPos2, movementDirection)) {
                //world.playSound(null, blockPos2, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
                BlockState blockState2 = BaseFireBlock.getState(level(), blockPos2);
                level().setBlock(blockPos2, blockState2, 11);
            }
            MagiciansRedEntity.ignite(level(), blockHitResult.getBlockPos());
        }
        inGround = true;
        super.onHitBlock(blockHitResult);
    }

    @Override
    public void addAdditionalSaveData(@NonNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("life", (short) this.ticksInAir);
    }

    @Override
    public void readAdditionalSaveData(@NonNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.ticksInAir = tag.getShort("life");
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            final Vec3 vel = getDeltaMovement();
            level().addParticle(
                    getSkin() == 2 ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                    getX() + random.nextFloat() * 0.5f - 0.25f,
                    getY() + random.nextFloat() * 0.5f - 0.25f,
                    getZ() + random.nextFloat() * 0.5f - 0.25f,
                    vel.x / 2, vel.y / 2, vel.z / 2
            );

            if (inGround) ticksInGround++;
            else ANIMATION.sendForEntity(this);
        } else {
            if (this.inGround) {
                if (explosive && ticksInGround == 0) {
                    explode();
                }
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

            TheSunEntity.dryOut((ServerLevel) level(), blockPosition());
        }
    }

    private void explode() {
        final Entity owner = getOwner();
        final Set<Entity> filter = new HashSet<>();
        filter.add(owner);
        filter.add(this);

        final List<LivingEntity> hurtAll = new ArrayList<>(JUtils.generateHitbox(level(), position(), 2, filter));
        hurtAll.removeIf(e -> !canDamage(JDamageSources.create(level(), DamageTypes.ON_FIRE), e));

        if (!hurtAll.isEmpty()) {
            for (LivingEntity l : hurtAll) {
                final LivingEntity target = JUtils.getUserIfStand(l);
                damageLogic(level(), target, l.position().subtract(position()).normalize(), 20, 3, false, 5f,
                        false, 10, JDamageSources.create(level(), DamageTypes.ON_FIRE), owner, CommonHitPropertyComponent.HitAnimation.LAUNCH);
            }
        }

        EXPLODE.sendForEntity(this);
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    private static final AzCommand EXPLODE = AzCommand.create(JCraft.BASE_CONTROLLER, "animation.meteor.explode");
    public static final AzCommand ANIMATION = AzCommand.controllerBuilder().
            playSequence(
                    JCraft.BASE_CONTROLLER,
                    sequenceBuilder -> sequenceBuilder.queue(
                            "animation.meteor.spawn",
                            props -> props.withPlayBehavior(AzPlayBehaviors.PLAY_ONCE)
                    ).queue(
                            "animation.meteor.idle",
                            props -> props.withPlayBehavior(AzPlayBehaviors.LOOP)
                    )
            )
            .setFreezeTickOffset(JCraft.BASE_CONTROLLER, 0)
            .setStartTickOffset(JCraft.BASE_CONTROLLER, 0)
            .setSpeed(JCraft.BASE_CONTROLLER, 1)
            .setRepeatAmount(JCraft.BASE_CONTROLLER, 0)
            .setReverseAnimation(JCraft.BASE_CONTROLLER, false)
            .build();
}
