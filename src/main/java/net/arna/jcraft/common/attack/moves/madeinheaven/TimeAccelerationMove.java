package net.arna.jcraft.common.attack.moves.madeinheaven;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.config.JServerConfig;
import net.arna.jcraft.common.entity.stand.MadeInHeavenEntity;
import net.arna.jcraft.common.network.s2c.TimeAccelStatePacket;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.predicate.entity.EntityPredicates;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TimeAccelerationMove extends AbstractMove<TimeAccelerationMove, MadeInHeavenEntity> {
    public TimeAccelerationMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MadeInHeavenEntity attacker, LivingEntity user, MoveContext ctx) {
        int accelTime = JServerConfig.MIH_TIME_ACCELERATION_DURATION.getValue();
        attacker.setAccelTime(accelTime);
        attacker.setAfterimage(true);
        TimeAccelStatePacket.sendStart(Objects.requireNonNull(attacker.getServer()).getPlayerManager(), attacker, accelTime);
        attacker.setSpeedometer(0);

        return Set.of();
    }

    public void tickTimeAcceleration(MadeInHeavenEntity attacker) {
        int aTime = attacker.getAccelTime();
        attacker.setAccelTime(aTime - 1);

        if (aTime > 1) {
            List<Entity> toCatch = attacker.getWorld().getEntitiesByClass(Entity.class,
                    attacker.getBoundingBox().expand(96), EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR);
            for (Entity entity : toCatch) {
                if (entity instanceof LivingEntity) continue;
                entity.tick();
            }
        } else if (aTime == 1) {
            if (attacker.getSpeedometer() == MadeInHeavenEntity.MAXIMUM_SPEEDOMETER) {
                List<LivingEntity> toCatch = attacker.getWorld().getEntitiesByClass(LivingEntity.class,
                        attacker.getBoundingBox().expand(96),
                        EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != attacker && e != attacker.getUser()));

                for (LivingEntity entity : toCatch) // 15s of Standless to any victims of Universe Reset
                    entity.addStatusEffect(new StatusEffectInstance(JStatusRegistry.STANDLESS, 300, 0, true, false));
            }

            attacker.setAfterimage(false);
            attacker.setSpeedometer(0);
        }
    }

    @Override
    protected @NonNull TimeAccelerationMove getThis() {
        return this;
    }

    @Override
    public @NonNull TimeAccelerationMove copy() {
        return copyExtras(new TimeAccelerationMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
