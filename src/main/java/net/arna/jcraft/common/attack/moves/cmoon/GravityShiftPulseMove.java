package net.arna.jcraft.common.attack.moves.cmoon;

import com.google.common.reflect.TypeToken;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.IntMoveVariable;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.CMoonEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.Gravity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.arna.jcraft.common.entity.stand.CMoonEntity.GRAVITY_CHANGE_DURATION;

public class GravityShiftPulseMove extends AbstractMove<GravityShiftPulseMove, CMoonEntity> {
    public GravityShiftPulseMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(CMoonEntity attacker, LivingEntity user, MoveContext ctx) {
        JComponents.getGravityShift(user).startDirectional();
        return Set.of();
    }

    @Override
    protected @NonNull GravityShiftPulseMove getThis() {
        return this;
    }

    @Override
    public @NonNull GravityShiftPulseMove copy() {
        return copyExtras(new GravityShiftPulseMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
