package net.arna.jcraft.common.attack.moves.killerqueen;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.base.AbstractMove;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.component.CooldownsComponent;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.AbstractKillerQueenEntity;
import net.arna.jcraft.common.util.CooldownType;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;

import java.util.Set;

public class ExplosiveDashAttack extends AbstractMove<ExplosiveDashAttack, AbstractKillerQueenEntity<?, ?>> {
    public ExplosiveDashAttack(int cooldown) {
        super(cooldown, 0, 0, 0);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(AbstractKillerQueenEntity<?, ?> stand, LivingEntity user, MoveContext ctx) {
        CooldownsComponent cooldowns = JComponents.getCooldowns(user);
        if (cooldowns.getCooldown(CooldownType.UTIL) > 0) return Set.of();

        Vec3d lookVec = user.getRotationVector().multiply(0.9);
        stand.world.createExplosion(user,
                user.getX() - lookVec.x,
                user.getY() + user.getHeight() / 2 - lookVec.y,
                user.getZ() - lookVec.z,
                1f, Explosion.DestructionType.NONE);

        user.setVelocity(user.getVelocity().add(lookVec));
        user.velocityModified = true;

        cooldowns.setCooldown(CooldownType.UTIL, getCooldown());
        stand.playSound(JSoundRegistry.KQ_DETONATE, 1, 1);

        return Set.of();
    }

    @Override
    protected ExplosiveDashAttack getThis() {
        return this;
    }
}
