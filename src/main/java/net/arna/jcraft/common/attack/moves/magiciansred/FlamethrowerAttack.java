package net.arna.jcraft.common.attack.moves.magiciansred;

import net.arna.jcraft.common.attack.core.base.AbstractBarrageAttack;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class FlamethrowerAttack extends AbstractBarrageAttack<FlamethrowerAttack, MagiciansRedEntity> {
    public FlamethrowerAttack(int cooldown, int windup, int moveStunTicks, float damage, float hitBoxSize, float knockBack, float range, float offset, int interval) {
        super(cooldown, windup, moveStunTicks, range, damage, hitBoxSize, knockBack, offset, interval);
    }

    @Override
    public void tick(MagiciansRedEntity stand) {
        super.tick(stand);
        if (!stand.world.isClient || !stand.hasUser() || !hasWindupPassed(stand)) return;

        Vec3d rotVec = getRotVec(stand);
        Vec3d mouthPos = stand.getEyePos().add(rotVec);
        for (int i = 0; i < 16; i++) {
            Vec3d vel = stand.getUserOrThrow().getVelocity().add(
                    rotVec
                            .rotateX(stand.getRandom().nextFloat() - 0.5f)
                            .rotateY(stand.getRandom().nextFloat() - 0.5f)
                            .rotateZ(stand.getRandom().nextFloat() - 0.5f)
                            .multiply(0.2)
            );
            stand.world.addParticle(
                    stand.getRandom().nextInt(6) == 5 ? ParticleTypes.LAVA : ParticleTypes.FLAME,
                    mouthPos.x, mouthPos.y, mouthPos.z,
                    vel.x, vel.y, vel.z
            );
        }
    }

    @Override
    public @NotNull Set<LivingEntity> perform(MagiciansRedEntity stand, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(stand, user, ctx);
        for (LivingEntity target : targets)
            if (!target.isOnFire())
                target.setOnFireFor(getInterval());
        return targets;
    }

    @Override
    protected FlamethrowerAttack getThis() {
        return this;
    }
}
