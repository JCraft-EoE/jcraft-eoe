package net.arna.jcraft.common.attack.moves.magiciansred;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.projectile.AnkhProjectile;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class CrossfireVariationAttack extends AbstractMove<CrossfireVariationAttack, MagiciansRedEntity> {
    private static final int variationAnkhs = 6;

    public CrossfireVariationAttack(int cooldown, int windup, int moveStunTicks, float moveDistance) {
        super(cooldown, windup, moveStunTicks, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MagiciansRedEntity attacker, LivingEntity user, MoveContext ctx) {
        int orbitRange = user.isSneaking() ? 7 : 5;
        for (int i = 0; i < variationAnkhs; i++) {
            AnkhProjectile ankh = new AnkhProjectile(attacker.getWorld(), user);
            ankh.setVelocity(0.0, 1.0, 0.0);
            ankh.setPosition(getOffsetHeightPos(attacker).add(0.0, 1.0, 0.0));
            ankh.setVariation(true);
            ankh.setOrbitRange(orbitRange);
            ankh.setOrbitOffset((360f / variationAnkhs) * i);
            attacker.getWorld().spawnEntity(ankh);
        }

        return Set.of();
    }

    @Override
    protected @NonNull CrossfireVariationAttack getThis() {
        return this;
    }

    @Override
    public @NonNull CrossfireVariationAttack copy() {
        return copyExtras(new CrossfireVariationAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
