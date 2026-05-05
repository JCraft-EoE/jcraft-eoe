package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.util.JExplosionModifier;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;

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
                            .noDamage()
                            .build());

            final Set<LivingEntity> hurt = JUtils.generateHitbox(level(), position(), 4.0, e -> true);

            final Entity owner = getOwner();
            final StandEntity<?, ?> ownerStand = owner instanceof LivingEntity livingOwner ? JUtils.getStand(livingOwner) : null;
            final DamageSource damageSource = ownerStand == null ? level().damageSources().explosion(owner, this) : JDamageSources.stand(ownerStand);

            for (final LivingEntity living : hurt) {
                if (hurt == owner) continue;

                final Vec3 kbVec = JUtils.getEyePos(living).subtract(position()).normalize();

                damageLogic(level(), living, kbVec, 2, 3, true, 15f, false, 4, damageSource, owner, null);

                living.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), 35, 0, true, false));
            }

            discard();
        }
    }
}
