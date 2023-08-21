package net.arna.jcraft.common.attack.moves.silverchariot;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class CircleSlashAttack extends AbstractSimpleAttack<CircleSlashAttack, SilverChariotEntity> {
    private final float originalDamage;

    public CircleSlashAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                             float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        originalDamage = damage;
    }

    @Override
    public boolean onInitialize(SilverChariotEntity attacker) {
        if (!super.onInitialize(attacker)) return false;

        // Reset damage
        withDamage(originalDamage);
        return true;
    }

    @Override
    public void tick(SilverChariotEntity attacker) {
        // This is fine cuz a copy is made when this attack is used.
        if (attacker.getMoveStun() % 20 == 0) withDamage(getDamage() + 1.5f);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(SilverChariotEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        Vec3d pos = user.getPos();
        double launchMultiplier = getDamage() / 5; // damage [6.5 to 11]

        for (LivingEntity living : targets) {
            Vec3d launchVec = living.getPos().subtract(pos).normalize().multiply(launchMultiplier);
            living.addVelocity(launchVec.x, launchVec.y + 0.2, launchVec.z);

            living.velocityModified = true;
            if (living instanceof ServerPlayerEntity serverPlayer)
                serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(serverPlayer));
        }

        return targets;
    }

    @Override
    protected @NonNull CircleSlashAttack getThis() {
        return this;
    }

    @Override
    public @NonNull CircleSlashAttack copy() {
        return new CircleSlashAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset());
    }
}
