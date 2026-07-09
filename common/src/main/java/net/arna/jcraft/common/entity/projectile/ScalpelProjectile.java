package net.arna.jcraft.common.entity.projectile;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.NonNull;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.MetallicaEntity;
import net.arna.jcraft.common.tickable.MagneticFields;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ScalpelProjectile extends AbstractArrow {
    public static final float IRON_COST = 5.0f;
    private final IntOpenHashSet pierced = new IntOpenHashSet(4);
    private boolean tempNoGrav = false;

    public ScalpelProjectile(Level world) {
        super(JEntityTypeRegistry.SCALPEL.get(), world);
    }

    public ScalpelProjectile(Level world, LivingEntity owner) {
        super(JEntityTypeRegistry.SCALPEL.get(), owner, world);
    }

    public static ScalpelProjectile fromMetallica(MetallicaEntity metallica) {
        if (metallica.drainIron(IRON_COST)) {
            return new ScalpelProjectile(metallica.level(), metallica.getUserOrThrow());
        }
        return null;
    }

    public void setTempNoGrav() {
        this.tempNoGrav = true;
        setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            if (inGround && tickCount % 20 == 0) {
                JCraft.getClientEntityHandler().spawnGroundedMoshParticles(this);
            }
            return;
        }
        if (tempNoGrav) {
            if (tickCount > 3) {
                if (tickCount == 4) {
                    final MagneticFields.MagneticField field = MagneticFields.nearestTo(position());
                    if (field != null && field.pos.distanceToSqr(position()) < field.getStrength() * field.getStrength()) {
                        setDeltaMovement(field.pos
                                .subtract(position())
                                .normalize()
                                .scale(2.0)
                        );
                    }
                }
                if (isNoGravity()) setNoGravity(false);
            } else {
                setDeltaMovement(getDeltaMovement().scale(0.1));
            }
        }
        if (inGroundTime >= 300) discard();
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult result) {
        super.onHitBlock(result);
        if (level().isClientSide()) return;
        pierced.clear();
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == EntityEvent.DEATH) remove(RemovalReason.KILLED);
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult entityHitResult) {
        if (level().isClientSide) return;
        Entity entity = entityHitResult.getEntity();
        if (entity instanceof StandEntity<?,?> stand) entity = stand.getUser();
        if (entity == null) return;
        if (pierced.contains(entity.getId())) return;

        Entity owner = this.getOwner();
        if (owner != null && owner.hasPassenger(entity) || entity == owner) {
            return;
        }

        if (isOnFire()) entity.setSecondsOnFire(5);

        final int blockstun = 4;
        final int stunT = 10;

        JUtils.projectileDamageLogic(this, level(), entity, Vec3.ZERO, stunT, 1, false, 2, blockstun, CommonHitPropertyComponent.HitAnimation.MID);
        playSound(SoundEvents.TRIDENT_HIT, 1, 1);
        // if (entity instanceof LivingEntity living) JComponentPlatformUtils.getMiscData(living).stab();
        setDeltaMovement(getDeltaMovement().scale(0.5));
        hurtMarked = true;
        pierced.add(entity.getId());
    }

    @Override
    protected boolean tryPickup(@NonNull Player player) {
        if (MetallicaEntity.ironProjectilePickup(player, IRON_COST)) return true;
        return super.tryPickup(player);
    }

    @Override
    protected @NonNull ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

}
