package net.arna.jcraft.common.entity.ai.goal;

import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;

import java.util.EnumSet;

public class SHAAttackGoal extends Goal {
    private final SheerHeartAttackEntity sha;
    private final LookControl shaLookControl;
    private final EntityNavigation shaNavigation;
    private final double speed;
    private int cooldown;
    private LivingEntity target;

    public SHAAttackGoal(SheerHeartAttackEntity mob, double speed) {
        sha = mob;
        shaLookControl = sha.getLookControl();
        shaNavigation = sha.getNavigation();
        this.speed = speed;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        target = sha.getTarget();
        return target != null;
    }

    public boolean shouldContinue() {
        if (!target.isAlive() || target.isRemoved()) return false;
        else if (sha.squaredDistanceTo(target) > 1024.0D) return false;
        else return !sha.getNavigation().isIdle() || canStart();
    }

    public void stop() {
        target = null;
        sha.getNavigation().stop();
    }

    public boolean shouldRunEveryTick() {
        return true;
    }

    public void tick() {
        shaLookControl.lookAt(target, 30.0F, 30.0F);
        shaNavigation.startMovingTo(target, speed);

        double d = 3.0; // SHA_width^2 * 4
        double e = sha.squaredDistanceTo(target);

        if (cooldown-- <= 0 && e <= d) {
            cooldown = 100;
            sha.Explode();
        }
    }
}
