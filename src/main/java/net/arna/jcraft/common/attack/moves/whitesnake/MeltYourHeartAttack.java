package net.arna.jcraft.common.attack.moves.whitesnake;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.WSAcidProjectile;
import net.arna.jcraft.common.entity.stand.WhiteSnakeEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class MeltYourHeartAttack extends AbstractSimpleAttack<MeltYourHeartAttack, WhiteSnakeEntity> {
    public MeltYourHeartAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                               float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(WhiteSnakeEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        for (int i = 0; i < 10; i++) {
            float yaw = i * 36F - 180F + i * 3.6F;
            for (int j = 0; j < 10; j++) {
                WSAcidProjectile acidProjectile = new WSAcidProjectile(attacker.getWorld(), user);
                acidProjectile.markMeltYourHeart();
                JUtils.shoot(acidProjectile, user, j * 36F - 180F, yaw, 0, 0.66F, 0);
                acidProjectile.setPosition(attacker.getEyePos());
                attacker.getWorld().spawnEntity(acidProjectile);
            }
        }

        return targets;
    }

    @Override
    protected @NonNull MeltYourHeartAttack getThis() {
        return this;
    }

    @Override
    public @NonNull MeltYourHeartAttack copy() {
        return copyExtras(new MeltYourHeartAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
