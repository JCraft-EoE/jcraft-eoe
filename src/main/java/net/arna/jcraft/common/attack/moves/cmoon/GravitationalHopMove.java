package net.arna.jcraft.common.attack.moves.cmoon;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.Set;

public class GravitationalHopMove extends AbstractMove<GravitationalHopMove, CMoonEntity> {
    public GravitationalHopMove(int cooldown) {
        super(cooldown, 1, 0, 0f);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(CMoonEntity attacker, LivingEntity user, MoveContext ctx) {
        if (user.isOnGround()) user.addStatusEffect(new StatusEffectInstance(JStatusRegistry.WEIGHTLESS, 200, 1));
        else {
            user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 60, 1));
            user.addVelocity(0, 1.0, 0);
        }

        user.velocityModified = true;

        return Set.of();
    }

    @Override
    protected @NonNull GravitationalHopMove getThis() {
        return this;
    }

    @Override
    public @NonNull GravitationalHopMove copy() {
        return copyExtras(new GravitationalHopMove(getCooldown()));
    }
}
