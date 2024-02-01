package net.arna.jcraft.common.attack.moves.madeinheaven;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.Set;

// This being a barrage is really just for the interval-based performing logic.
public class JudgementAttack extends AbstractBarrageAttack<JudgementAttack, MadeInHeavenEntity> {
    public static final MoveVariable<Vec3d> INIT_POS = new MoveVariable<>(Vec3d.class),
            INIT_ROT = new MoveVariable<>(Vec3d.class);

    public JudgementAttack(int cooldown, int windup, int duration, float moveDistance, int interval) {
        super(cooldown, windup, duration, moveDistance, 0, 0, 0, 0, 0, interval);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MadeInHeavenEntity attacker, LivingEntity user, MoveContext ctx) {
        Vec3d initPos = ctx.get(INIT_POS);
        Vec3d initRot = ctx.get(INIT_ROT);

        Set<LivingEntity> targets = Set.of();
        if (attacker.getMoveStun() > 1) {
            if (getBlow(attacker) > 0) {
                Random random = attacker.getRandom();
                targets = SpeedSliceAttack.doSpeedSlice(attacker,
                        initPos.add(initRot.multiply(random.nextTriangular(2, 2))),
                        initPos.add(random.nextTriangular(0, 5), random.nextTriangular(0, 5),
                                random.nextTriangular(0, 5)),
                        1f, 0.1f, 1.75f, 20, 1);
            } else {
                ctx.set(INIT_POS, user.getPos());
                ctx.set(INIT_ROT, Vec3d.fromPolar(0, user.getYaw()));
            }
        } else targets = SpeedSliceAttack.doSpeedSlice(attacker,
                initPos.subtract(user.getRotationVector().multiply(3)),
                initPos.add(initRot.multiply(10)), 6, 3, 2f, 5, 3);

        return targets;
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(INIT_POS, Vec3d.ZERO);
        ctx.register(INIT_ROT, Vec3d.ZERO);
    }

    @Override
    protected @NonNull JudgementAttack getThis() {
        return this;
    }

    @Override
    public @NonNull JudgementAttack copy() {
        return copyExtras(new JudgementAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getInterval()));
    }
}
