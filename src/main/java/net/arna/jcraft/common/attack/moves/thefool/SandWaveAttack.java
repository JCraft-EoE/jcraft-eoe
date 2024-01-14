package net.arna.jcraft.common.attack.moves.thefool;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractEffectInflictingBarrageAttack;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.arna.jcraft.common.util.MobilityType;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class SandWaveAttack extends AbstractEffectInflictingBarrageAttack<SandWaveAttack, TheFoolEntity> {
    public SandWaveAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                          float hitboxSize, float knockback, float offset, int interval) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, interval,
                List.of(new StatusEffectInstance(JStatusRegistry.KNOCKDOWN, 15, 0, true, false)));
        mobilityType = MobilityType.DASH;
        ranged = true;
    }

    @Override
    public void onInitiate(TheFoolEntity attacker) {
        super.onInitiate(attacker);

        attacker.setSand(true);
        attacker.setFree(false);
        attacker.setWave(true);
    }

    @Override
    public void tick(TheFoolEntity attacker) {
        super.tick(attacker);

        LivingEntity user = attacker.getUser();
        if (user == null || !user.isOnGround()) return;

        Vec3d rotVec = user.getRotationVector().multiply(0.25);
        user.addVelocity(rotVec.x, 0, rotVec.z);
        user.velocityModified = true;
    }

    @Override
    protected @NonNull SandWaveAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SandWaveAttack copy() {
        return copyExtras(new SandWaveAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval()));
    }
}
