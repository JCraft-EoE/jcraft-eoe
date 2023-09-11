package net.arna.jcraft.common.attack.moves.dirtydeedsdonedirtcheap;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.D4CEntity;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.Set;

public class FlagMove extends AbstractMove<FlagMove, D4CEntity> {
    public FlagMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        mobilityType = MobilityType.HIGHJUMP;
    }

    @Override
    public void onInitiate(D4CEntity attacker) {
        super.onInitiate(attacker);

        attacker.getUserOrThrow().addStatusEffect(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, getDuration(),
                0, true, false));
        attacker.getUserOrThrow().addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, getDuration(),
                0, true, false));
    }

    @Override
    public @NonNull Set<LivingEntity> perform(D4CEntity attacker, LivingEntity user, MoveContext ctx) {
        int duration = getWindupPoint();
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, duration, 0, true, false));
        user.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, duration, 2, true, false));

        return Set.of();
    }

    @Override
    protected @NonNull FlagMove getThis() {
        return this;
    }

    @Override
    public @NonNull FlagMove copy() {
        return copyExtras(new FlagMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
