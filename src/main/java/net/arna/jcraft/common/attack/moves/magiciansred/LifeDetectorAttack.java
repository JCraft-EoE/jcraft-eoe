package net.arna.jcraft.common.attack.moves.magiciansred;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.projectile.LifeDetectorEntity;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

public class LifeDetectorAttack extends AbstractMove<LifeDetectorAttack, MagiciansRedEntity> {
    public LifeDetectorAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MagiciansRedEntity attacker, LivingEntity user, MoveContext ctx) {
        LifeDetectorEntity lifeDetector = new LifeDetectorEntity(JEntityTypeRegistry.LIFE_DETECTOR, attacker.getWorld());
        lifeDetector.setMaster(user);
        lifeDetector.refreshPositionAndAngles(attacker.getX(), attacker.getY() + 1.5, attacker.getZ(), attacker.getYaw(), attacker.getPitch());
        attacker.getWorld().spawnEntity(lifeDetector);

        return Set.of();
    }

    @Override
    protected @NonNull LifeDetectorAttack getThis() {
        return this;
    }

    @Override
    public @NonNull LifeDetectorAttack copy() {
        return copyExtras(new LifeDetectorAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
