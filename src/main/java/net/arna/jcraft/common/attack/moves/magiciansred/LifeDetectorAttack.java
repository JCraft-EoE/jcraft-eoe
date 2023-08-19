package net.arna.jcraft.common.attack.moves.magiciansred;

import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.entity.projectile.LifeDetectorEntity;
import net.arna.jcraft.common.entity.stand.MagiciansRedEntity;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class LifeDetectorAttack extends AbstractMove<LifeDetectorAttack, MagiciansRedEntity> {
    public LifeDetectorAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NotNull Set<LivingEntity> perform(MagiciansRedEntity stand, LivingEntity user, MoveContext ctx) {
        LifeDetectorEntity lifeDetector = new LifeDetectorEntity(JEntityTypeRegistry.LIFE_DETECTOR, stand.world);
        lifeDetector.setMaster(user);
        lifeDetector.refreshPositionAndAngles(stand.getX(), stand.getY() + 1.5, stand.getZ(), stand.getYaw(), stand.getPitch());
        stand.world.spawnEntity(lifeDetector);

        return Set.of();
    }

    @Override
    protected LifeDetectorAttack getThis() {
        return this;
    }

    @Override
    public LifeDetectorAttack copy() {
        return new LifeDetectorAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance());
    }
}
