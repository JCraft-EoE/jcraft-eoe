package net.arna.jcraft.common.attack.moves.magiciansred;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class FlamethrowerAttack extends AbstractBarrageAttack<FlamethrowerAttack, MagiciansRedEntity> {
    public FlamethrowerAttack(int cooldown, int windup, int duration, float damage, int stun, float hitboxSize, float knockback,
                              float range, float offset, int interval) {
        super(cooldown, windup, duration, range, damage, stun, hitboxSize, knockback, offset, interval);
    }

    @Override
    public void tick(MagiciansRedEntity attacker) {
        super.tick(attacker);
        if (!attacker.world.isClient || !attacker.hasUser() || !hasWindupPassed(attacker)) return;

        Vec3d rotVec = getRotVec(attacker);
        Vec3d mouthPos = attacker.getEyePos().add(rotVec);
        for (int i = 0; i < 16; i++) {
            Vec3d vel = attacker.getUserOrThrow().getVelocity().add(
                    rotVec
                            .rotateX(attacker.getRandom().nextFloat() - 0.5f)
                            .rotateY(attacker.getRandom().nextFloat() - 0.5f)
                            .rotateZ(attacker.getRandom().nextFloat() - 0.5f)
                            .multiply(0.2)
            );
            attacker.world.addParticle(
                    attacker.getRandom().nextInt(6) == 5 ? ParticleTypes.LAVA : ParticleTypes.FLAME,
                    mouthPos.x, mouthPos.y, mouthPos.z,
                    vel.x, vel.y, vel.z
            );
        }
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MagiciansRedEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        for (LivingEntity target : targets)
            if (!target.isOnFire())
                target.setOnFireFor(getInterval());
        return targets;
    }

    @Override
    protected @NonNull FlamethrowerAttack getThis() {
        return this;
    }

    @Override
    public @NonNull FlamethrowerAttack copy() {
        return copyExtras(new FlamethrowerAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDuration(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), getInterval()));
    }
}
