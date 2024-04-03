package net.arna.jcraft.common.attack.moves.magiciansred;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.projectile.AnkhProjectile;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class CrossfireAttack extends AbstractMove<CrossfireAttack, MagiciansRedEntity> {
    public CrossfireAttack(int cooldown, int windup, int duration, float attackDistance) {
        super(cooldown, windup, duration, attackDistance);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MagiciansRedEntity attacker, LivingEntity user, MoveContext ctx) {
        for (int i = 0; i < 3; i++) {
            AnkhProjectile ankh = new AnkhProjectile(attacker.getWorld(), user);
            ankh.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1F, 5F);
            ankh.setPosition(getOffsetHeightPos(attacker));
            attacker.getWorld().spawnEntity(ankh);
        }

        return Set.of();
    }

    @Override
    protected @NonNull CrossfireAttack getThis() {
        return this;
    }

    @Override
    public @NonNull CrossfireAttack copy() {
        return copyExtras(new CrossfireAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
