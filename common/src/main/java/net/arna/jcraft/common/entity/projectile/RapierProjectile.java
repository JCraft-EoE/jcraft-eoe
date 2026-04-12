package net.arna.jcraft.common.entity.projectile;

import lombok.NonNull;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RapierProjectile extends AbstractArrow {
    private static final EntityDataAccessor<Integer> SKIN;
    private final @Nullable StandEntity<?, ?> origin;
    private int ticksInAir, bouncesLeft = 5;

    static {
        SKIN = SynchedEntityData.defineId(RapierProjectile.class, EntityDataSerializers.INT);
    }

    public RapierProjectile(Level world) {
        super(JEntityTypeRegistry.RAPIER.get(), world);
        this.origin = null;
    }

    public RapierProjectile(Level world, LivingEntity owner, @NonNull StandEntity<?, ?> silverChariot) {
        super(JEntityTypeRegistry.RAPIER.get(), owner, world);
        this.origin = silverChariot;
    }

    public int getSkin() {
        return this.entityData.get(SKIN);
    }

    public void setSkin(int skin) {
        this.entityData.set(SKIN, skin);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SKIN, 0);
    }

    @Override
    public @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (!inGround) {
                Vec3 vel = getDeltaMovement();
                for (double i = 0; i < 3.0; i++) {
                    level().addParticle(
                            ParticleTypes.ELECTRIC_SPARK,
                            Mth.lerp(i / 3.0, getX(), xo), Mth.lerp(i / 3.0, getY(), yo), Mth.lerp(i / 3.0, getZ(), zo),
                            vel.x, vel.y, vel.z
                    );
                }
            }
        } else if (origin == null || !origin.isAlive() || ++ticksInAir > 640) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        HitResult.Type type = hitResult.getType();
        if (type == HitResult.Type.ENTITY) {
            this.onHitEntity((EntityHitResult) hitResult);
            this.level().gameEvent(GameEvent.PROJECTILE_LAND, hitResult.getLocation(), GameEvent.Context.of(this, null));
        } else if (type == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            if (bouncesLeft-- > 0) {
                Vec3i normal = blockHitResult.getDirection().getNormal();
                push(normal.getX(), normal.getY(), normal.getZ());
            } else {
                this.onHitBlock(blockHitResult);
                BlockPos blockPos = blockHitResult.getBlockPos();
                this.level().gameEvent(GameEvent.PROJECTILE_LAND, blockPos, GameEvent.Context.of(this, this.level().getBlockState(blockPos)));
            }
        }
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult entityHitResult) {
        if (level().isClientSide) {
            return;
        }
        final Entity owner = this.getOwner();
        if (owner == null) {
            return;
        }
        final Entity entity = entityHitResult.getEntity();
        if (owner.hasPassenger(entity) || entity == owner) {
            return;
        }
        if (this.isOnFire()) {
            entity.setSecondsOnFire(5);
        }

        JUtils.projectileDamageLogic(this, level(), entity, Vec3.ZERO, 20, 1, false, 2, 6, CommonHitPropertyComponent.HitAnimation.MID);
        playSound(SoundEvents.TRIDENT_HIT, 1, 1);
        discard();
    }

    @Override
    protected boolean tryPickup(@NonNull Player player) {
        if (player != getOwner() || !(JUtils.getStand(player) instanceof SilverChariotEntity silverChariot)) {
            return false;
        }

        silverChariot.setHasRapier(true);
        return true;
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

}
