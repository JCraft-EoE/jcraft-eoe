package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.common.compat.FtbChunksCompat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class AerobombProjectile extends AbstractArrow {

    public AerobombProjectile(final Level level) {
        super(JEntityTypeRegistry.AEROBOMB.get(), level);
//        setSoundEvent(???); // TODO record
    }

    @Override
    protected boolean tryPickup(final Player player) {
        return false;
    }

    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void onHitBlock(final BlockHitResult result) {
        mayExplode();
    }

    @Override
    protected void onHitEntity(final EntityHitResult result) {
        mayExplode();
    }

    protected void mayExplode() {
        if (level() instanceof ServerLevel serverLevel) {
            if (getDeltaMovement().length() >= 0.5) { // base speed for boom
                final boolean chunkAccess = !(getOwner() instanceof ServerPlayer player) || FtbChunksCompat.get().mayEdit(player, serverLevel, blockPosition());
                final boolean griefing = serverLevel.getGameRules().getRule(JCraft.STAND_GRIEFING).get();
                Level.ExplosionInteraction interaction = chunkAccess && griefing ? Level.ExplosionInteraction.TNT : Level.ExplosionInteraction.NONE;
                serverLevel.explode(this, getX(), getY(), getZ(), 4f, interaction);
            }

            discard();
        }
    }
}
