package net.arna.jcraft.common.attack.moves.whitesnake;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.WSAcidProjectile;
import net.arna.jcraft.common.entity.stand.WhiteSnakeEntity;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class ChargedSpewAttack extends AbstractSimpleAttack<ChargedSpewAttack, WhiteSnakeEntity> {
    public ChargedSpewAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                             float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(WhiteSnakeEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        for (int i = 0; i < 5; i++) {
            WSAcidProjectile acidProjectile = new WSAcidProjectile(attacker.getWorld(), user);
            acidProjectile.setVelocity(user, user.getPitch(), user.getYaw() - 75F + i * 37.5F, 0, 0.66F, 0);
            acidProjectile.setPosition(attacker.getEyePos());
            attacker.getWorld().spawnEntity(acidProjectile);
        }

        return targets;
    }

    @Override
    protected @NonNull ChargedSpewAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ChargedSpewAttack copy() {
        return new ChargedSpewAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset());
    }
}
