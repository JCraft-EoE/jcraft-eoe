package net.arna.jcraft.common.attack.moves.magiciansred;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.RedBindEntity;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class RedBindAttack extends AbstractSimpleAttack<RedBindAttack, MagiciansRedEntity> {
    public RedBindAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun,
                         float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MagiciansRedEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        if (targets.isEmpty()) return targets;

        LivingEntity boundEntity = JUtils.getUserIfStand(targets.stream().findFirst().orElseThrow());

        if (JUtils.isBlocking(boundEntity)) return Set.of();

        // Remove Stand
        StandEntity<?, ?> boundStand = JUtils.getStand(boundEntity);
        if (boundStand != null) {
            boundStand.curMove = null;
            boundStand.setMoveStun(0);
            boundStand.desummon();
        }

        // Stun
        boundEntity.removeStatusEffect(JStatusRegistry.DAZED);
        StandEntity.stun(boundEntity, RedBindEntity.ticksToLive, 0);

        // Create and bind
        RedBindEntity redBind = new RedBindEntity(JEntityTypeRegistry.RED_BIND, attacker.getWorld());
        redBind.setPosition(boundEntity.getPos());
        redBind.setMaster(user);
        redBind.setBoundEntity(boundEntity);
        attacker.getWorld().spawnEntity(redBind);

        return targets;
    }

    @Override
    protected @NonNull RedBindAttack getThis() {
        return this;
    }

    @Override
    public @NonNull RedBindAttack copy() {
        return copyExtras(new RedBindAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }
}
