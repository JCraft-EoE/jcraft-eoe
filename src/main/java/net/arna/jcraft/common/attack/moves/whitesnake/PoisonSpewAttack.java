package net.arna.jcraft.common.attack.moves.whitesnake;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.WSAcidProjectile;
import net.arna.jcraft.common.entity.stand.WhiteSnakeEntity;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class PoisonSpewAttack extends AbstractSimpleAttack<PoisonSpewAttack, WhiteSnakeEntity> {
    public PoisonSpewAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                            float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(WhiteSnakeEntity attacker, LivingEntity user, MoveContext ctx) {
        WSAcidProjectile acidProjectile = new WSAcidProjectile(attacker.getWorld(), user);
        acidProjectile.setVelocity(user, user.getPitch(), user.getYaw(), 0, 1.33F, 0);
        acidProjectile.setPosition(attacker.getEyePos());
        attacker.getWorld().spawnEntity(acidProjectile);

        return super.perform(attacker, user, ctx);
    }

    @Override
    protected @NonNull PoisonSpewAttack getThis() {
        return this;
    }

    @Override
    public @NonNull PoisonSpewAttack copy() {
        return copyExtras(new PoisonSpewAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
