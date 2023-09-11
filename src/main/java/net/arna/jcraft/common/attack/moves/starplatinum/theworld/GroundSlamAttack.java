package net.arna.jcraft.common.attack.moves.starplatinum.theworld;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.SPTWEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class GroundSlamAttack extends AbstractSimpleAttack<GroundSlamAttack, SPTWEntity> {
    public GroundSlamAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                            float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(SPTWEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        Vec3d pos = user.getPos();
        for (LivingEntity target : targets) {
            Vec3d launchVec = target.getPos().subtract(pos).normalize().multiply(1.3);
            target.addVelocity(launchVec.x, launchVec.y + 0.4, launchVec.z);

            target.velocityModified = true;
            if (target instanceof ServerPlayerEntity serverPlayer)
                serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayer));
        }

        return targets;
    }

    @Override
    protected @NonNull GroundSlamAttack getThis() {
        return this;
    }

    @Override
    public @NonNull GroundSlamAttack copy() {
        return copyExtras(new GroundSlamAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
