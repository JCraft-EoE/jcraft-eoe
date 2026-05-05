package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.util.JExplosionModifier;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class AerobombProjectile extends AbstractArrow {

    public AerobombProjectile(final Level level) {
        super(JEntityTypeRegistry.AEROBOMB.get(), level);
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
            playSound(JSoundRegistry.AS_BOMB_LAND.get());

            final boolean mayAlter = JUtils.mayAlter(serverLevel, getOwner() instanceof LivingEntity ? (LivingEntity)getOwner() : null, getOnPos(), null);
            final boolean griefing = serverLevel.getGameRules().getRule(JCraft.STAND_GRIEFING).get();
            JUtils.explode(level(), this, getX(), getY(), getZ(), 4f,
                    JExplosionModifier.builder().particle(JParticleTypeRegistry.BOOM_1.get())
                            .blockInteraction(griefing && mayAlter ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP)
                            .particleVelocity(Vec3.ZERO)
                            .build());
            discard();
        }
    }
}
