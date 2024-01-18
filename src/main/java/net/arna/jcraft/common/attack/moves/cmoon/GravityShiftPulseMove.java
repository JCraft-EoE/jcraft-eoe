package net.arna.jcraft.common.attack.moves.cmoon;

import com.google.common.reflect.TypeToken;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.ctx.IntMoveVariable;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
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
    public static final String GRAVITY_SOURCE = JCraft.MOD_ID + "$" + GravityShiftMove.class.getSimpleName();
    public static final IntMoveVariable SHIFT_AGE = new IntMoveVariable();
    public static final MoveVariable<List<Entity>> SHIFTED_ENTITIES = new MoveVariable<>(new TypeToken<>() {});

    public GravityShiftPulseMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(CMoonEntity attacker, LivingEntity user, MoveContext ctx) {
        Direction lookDir = JUtils.getLookDirection(user);
        List<Entity> toCatch = attacker.getWorld().getEntitiesByClass(Entity.class, attacker.getBoundingBox().expand(16),
                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != attacker && e != user));

        ctx.setInt(SHIFT_AGE, attacker.age);

        Gravity gravity = new Gravity(lookDir, 3, GRAVITY_CHANGE_DURATION, GRAVITY_SOURCE);
        List<Entity> shiftedEntities = ctx.get(SHIFTED_ENTITIES);
        for (Entity entity : toCatch) {
            shiftedEntities.add(entity);

            GravityChangerAPI.addGravity(entity, gravity);
            if (entity instanceof LivingEntity living)
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, GRAVITY_CHANGE_DURATION, 0, true, false));
        }

        return Set.of();
    }

    public void tickGravShift(CMoonEntity attacker) {
        MoveContext ctx = attacker.getMoveContext();
        int shiftAge = ctx.getInt(SHIFT_AGE);
        List<Entity> shiftedEntities = ctx.get(SHIFTED_ENTITIES);

        if (attacker.age - shiftAge >= GRAVITY_CHANGE_DURATION && !shiftedEntities.isEmpty())
            shiftedEntities.clear();
        else for (Entity entity : shiftedEntities)
            if (entity.squaredDistanceTo(attacker) > 10000) // 100m
                GravityChangerAPI.setGravity(entity, GravityChangerAPI.getGravityList(entity).stream()
                        .filter(g -> !GRAVITY_SOURCE.equals(g.source()))
                        .toList());
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(SHIFT_AGE);
        ctx.register(SHIFTED_ENTITIES, new ArrayList<>());
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
