package net.arna.jcraft.common.attack.moves.cmoon;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.Set;

public class GravitationalHopMove extends AbstractMove<GravitationalHopMove, CMoonEntity> {
    public GravitationalHopMove(int cooldown) {
        super(cooldown, 0, 0, 0f);
        mobilityType = MobilityType.HIGHJUMP;
    }

    @Override
    public void onInitiate(CMoonEntity attacker) {
        getInitActions().forEach(action -> action.perform(attacker, attacker.getUser(), attacker.getMoveContext()));
    }

    @Override
    public @NonNull Set<LivingEntity> perform(CMoonEntity attacker, LivingEntity user, MoveContext ctx) {
        if (user.isOnGround()) {
            if (user.hasStatusEffect(JStatusRegistry.WEIGHTLESS))
                user.removeStatusEffect(JStatusRegistry.WEIGHTLESS);
            user.addStatusEffect(new StatusEffectInstance(JStatusRegistry.WEIGHTLESS, 200, 1));
        } else {
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
