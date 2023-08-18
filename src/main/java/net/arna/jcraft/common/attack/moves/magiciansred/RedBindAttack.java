package net.arna.jcraft.common.attack.moves.magiciansred;

import net.arna.jcraft.common.attack.core.base.AbstractSimpleAttack;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.projectile.RedBindEntity;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class RedBindAttack extends AbstractSimpleAttack<RedBindAttack, MagiciansRedEntity> {
    public RedBindAttack(int cooldown, int windup, int moveStunTicks, float attackDistance, float damage, float hitBoxSize, float knockBack, float offset) {
        super(cooldown, windup, moveStunTicks, attackDistance, damage, hitBoxSize, knockBack, offset);
    }

    @Override
    public @NotNull Set<LivingEntity> perform(MagiciansRedEntity stand, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(stand, user, ctx);
        if (targets.isEmpty()) return targets;

        LivingEntity boundEntity = JUtils.getUserIfStand(targets.stream().findFirst().orElseThrow());

        if (JUtils.isBlocking(boundEntity)) return Set.of();

        // Remove Stand
        StandEntity<?, ?> boundStand = JUtils.getStand(boundEntity);
        if (boundStand != null) {
            boundStand.curAttack = null;
            boundStand.setMoveStun(0);
            boundStand.desummon();
        }

        // Stun
        boundEntity.removeStatusEffect(JStatusRegistry.DAZED);
        StandEntity.stun(boundEntity, RedBindEntity.ticksToLive, 0);

        // Create and bind
        RedBindEntity redBind = new RedBindEntity(JEntityTypeRegistry.RED_BIND, stand.world);
        redBind.setPosition(boundEntity.getPos());
        redBind.setMaster(user);
        redBind.setBoundEntity(boundEntity);
        stand.world.spawnEntity(redBind);

        return targets;
    }

    @Override
    protected RedBindAttack getThis() {
        return this;
    }
}
