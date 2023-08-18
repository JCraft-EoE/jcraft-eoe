package net.arna.jcraft.common.attack.moves.magiciansred;

import net.arna.jcraft.common.attack.core.base.AbstractMove;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.projectile.AnkhProjectile;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class CrossfireVariationAttack extends AbstractMove<CrossfireVariationAttack, MagiciansRedEntity> {
    private static final int variationAnkhs = 6;

    public CrossfireVariationAttack(int cooldown, int windup, int moveStunTicks, float moveDistance) {
        super(cooldown, windup, moveStunTicks, moveDistance);
        ranged = true;
    }

    @Override
    public @NotNull Set<LivingEntity> perform(MagiciansRedEntity stand, LivingEntity user, MoveContext ctx) {
        int orbitRange = user.isSneaking() ? 6 : 4;
        for (int i = 0; i < variationAnkhs; i++) {
            AnkhProjectile ankh = new AnkhProjectile(stand.world, user);
            ankh.setVelocity(0.0, 1.0, 0.0);
            ankh.setPosition(getOffsetHeightPos(stand).add(0.0, 1.0, 0.0));
            ankh.setVariation(true);
            ankh.setOrbitRange(orbitRange);
            ankh.setOrbitOffset((360f / variationAnkhs) * i);
            stand.world.spawnEntity(ankh);
        }

        return super.perform(stand, user, ctx);
    }

    @Override
    protected CrossfireVariationAttack getThis() {
        return this;
    }
}
