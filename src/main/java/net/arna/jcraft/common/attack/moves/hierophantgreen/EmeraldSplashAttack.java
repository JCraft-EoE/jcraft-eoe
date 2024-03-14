package net.arna.jcraft.common.attack.moves.hierophantgreen;

import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.moves.base.AbstractMultiHitAttack;
import net.arna.jcraft.common.entity.projectile.AnkhProjectile;
import net.arna.jcraft.common.entity.projectile.EmeraldProjectile;
import net.arna.jcraft.common.entity.stand.HGEntity;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class EmeraldSplashAttack extends AbstractMultiHitAttack<EmeraldSplashAttack, HGEntity> {
    public EmeraldSplashAttack(int cooldown, int duration, float moveDistance, float damage, int stun, float knockback, float offset, IntSet hitMoments) {
        super(cooldown, duration, moveDistance, damage, stun, 0, knockback, offset, hitMoments);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(HGEntity attacker, LivingEntity user, MoveContext ctx) {
        for (int i = 0; i < 3; i++) {
            EmeraldProjectile emerald = new EmeraldProjectile(attacker.world, user);
            emerald.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 5F);

            Vec3d upVec = GravityChangerAPI.getEyeOffset(attacker.getUserOrThrow());
            Vec3d heightOffset = upVec.multiply(0.75);
            emerald.setPosition(attacker.getBaseEntity().getPos().add(heightOffset));

            attacker.world.spawnEntity(emerald);
        }

        return Set.of();
    }

    @Override
    protected @NonNull EmeraldSplashAttack getThis() {
        return this;
    }

    @Override
    public @NonNull EmeraldSplashAttack copy() {
        return copyExtras(new EmeraldSplashAttack(getCooldown(), getDuration(), getMoveDistance(), getDamage(), getStun(), getKnockback(), getOffset(), getHitMoments()));
    }
}
