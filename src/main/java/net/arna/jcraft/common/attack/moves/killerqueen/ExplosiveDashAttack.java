package net.arna.jcraft.common.attack.moves.killerqueen;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.AbstractKillerQueenEntity;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

import java.util.Set;

public class ExplosiveDashAttack extends AbstractMove<ExplosiveDashAttack, AbstractKillerQueenEntity<?, ?>> {
    public ExplosiveDashAttack(int cooldown) {
        super(cooldown, 0, 0, 0);
        dash = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(AbstractKillerQueenEntity<?, ?> attacker, LivingEntity user, MoveContext ctx) {
        Vec3d lookVec = user.getRotationVector().multiply(0.9);
        attacker.getWorld().createExplosion(user,
                user.getX() - lookVec.x,
                user.getY() + user.getHeight() / 2 - lookVec.y,
                user.getZ() - lookVec.z,
                1f, World.ExplosionSourceType.NONE);

        user.setVelocity(user.getVelocity().add(lookVec));
        user.velocityModified = true;

        attacker.playSound(JSoundRegistry.KQ_DETONATE, 1, 1);

        return Set.of();
    }

    @Override
    protected @NonNull ExplosiveDashAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ExplosiveDashAttack copy() {
        return copyExtras(new ExplosiveDashAttack(getCooldown()));
    }
}
