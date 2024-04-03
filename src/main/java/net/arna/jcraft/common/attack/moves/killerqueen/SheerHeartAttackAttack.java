package net.arna.jcraft.common.attack.moves.killerqueen;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.arna.jcraft.common.entity.stand.KillerQueenEntity;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class SheerHeartAttackAttack extends AbstractMove<SheerHeartAttackAttack, KillerQueenEntity> {
    public SheerHeartAttackAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(KillerQueenEntity attacker, LivingEntity user, MoveContext ctx) {
        SheerHeartAttackEntity sha = new SheerHeartAttackEntity(JEntityTypeRegistry.SHEER_HEART_ATTACK, attacker.getWorld());
        sha.setMaster(user);
        sha.refreshPositionAndAngles(attacker.getX(), attacker.getY() + 0.5, attacker.getZ(), attacker.getYaw(), attacker.getPitch());
        attacker.getWorld().spawnEntity(sha);

        return Set.of();
    }

    @Override
    protected @NonNull SheerHeartAttackAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SheerHeartAttackAttack copy() {
        return copyExtras(new SheerHeartAttackAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
