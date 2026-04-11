package net.arna.jcraft.common.entity.projectile;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.NonNull;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.MetallicaEntity;
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

import java.util.Set;

public class RazorProjectile extends AbstractArrow {
    public static final float IRON_COST = 5.0f;
    private final IntOpenHashSet pierced = new IntOpenHashSet(4);

    public RazorProjectile(Level world) {
        super(JEntityTypeRegistry.RAZOR.get(), world);
        // setSoundEvent();
    }

    public RazorProjectile(Level world, LivingEntity owner) {
        super(JEntityTypeRegistry.RAZOR.get(), owner, world);
        this.pickup = Pickup.ALLOWED;
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
        if (!inGround) return;
        if (inGroundTime >= 300) discard();

        final float width = getBbWidth();
        final Set<LivingEntity> hurt = JUtils.generateHitbox(
                level(),
                position().subtract(
                width / 2,
                width / 2,
                width / 2
                ),
                width,
                Set.of(this)
        );

        for (LivingEntity living : hurt) {
            final LivingEntity target = JUtils.getUserIfStand(living);
            final int targetId = target.getId();
            if (target == getOwner()) continue;
            if (pierced.contains(targetId)) continue;
            JUtils.projectileDamageLogic(this, level(), target, Vec3.ZERO, 5, 1,false, 1f, 2, CommonHitPropertyComponent.HitAnimation.LOW);
            pierced.add(targetId);
        }
    }

    @Override
    protected void onHitBlock(@NonNull BlockHitResult result) {
        super.onHitBlock(result);
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == EntityEvent.DEATH) remove(RemovalReason.KILLED);
    }

    @Override
    protected void onHitEntity(@NonNull EntityHitResult entityHitResult) {
        if (level().isClientSide || tickCount < 3) return;
        Entity entity = entityHitResult.getEntity();
        doDamage(entity);
    }

    private void doDamage(Entity entity) {
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
        playSound(SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, 1, 1);
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
