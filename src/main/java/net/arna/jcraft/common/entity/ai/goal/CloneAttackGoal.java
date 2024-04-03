package net.arna.jcraft.common.entity.ai.goal;

import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.Hand;

import java.util.EnumSet;

public class CloneAttackGoal extends Goal {
    private final PlayerCloneEntity clone;
    private final LookControl cloneLookControl;
    private final EntityNavigation cloneNavigation;
    private LivingEntity target;
    private final double speed;
    private long lastUpdateTime;

    public CloneAttackGoal(PlayerCloneEntity mob, double speed) {
        clone = mob;
        cloneLookControl = mob.getLookControl();
        cloneNavigation = mob.getNavigation();
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    public boolean canStart() {
        long l = clone.getWorld().getTime();
        if (l - this.lastUpdateTime < 10L) {
            return false;
        } else {
            this.lastUpdateTime = l;
            target = clone.getTarget();
            if (target == null) {
                return false;
            } else if (!target.isAlive()) {
                return false;
            } else {
                Path path = cloneNavigation.findPathTo(target, 0);
                if (path != null) return true;
                else return this.getSquaredMaxAttackDistance(target) >= clone.squaredDistanceTo(target);
            }
        }
    }

    public boolean shouldContinue() {
        if (target == null) return false;
        else if (!target.isAlive() ||target.isRemoved()) return false;
        else if (clone.squaredDistanceTo(target) > 1024.0D) return false;
        else if (target == clone.getMaster()) return false;
        else return !cloneNavigation.isIdle() || this.canStart();
    }

    public void start() {
        clone.setAttacking(true);
    }

    public void stop() {
        target = null;
        clone.setTarget(null);
        clone.setAttacking(false);
        cloneNavigation.stop();
    }

    public boolean shouldRunEveryTick() {
        return true;
    }

    public void tick() {
        if (target == null) return;

        cloneNavigation.startMovingTo(target, speed);
        cloneLookControl.lookAt(target, 30.0F, 30.0F);
        double d = this.getSquaredMaxAttackDistance(target);
        if (target.squaredDistanceTo(clone) <= d && clone.getCooldown() <= 0) {
            clone.startCooldown();
            clone.swingHand(Hand.MAIN_HAND);
            clone.tryAttack(target);
        }
    }

    private double getSquaredMaxAttackDistance(LivingEntity entity) {
        return 1.44F + entity.getWidth(); // 0.6^2 * 4 i.e. cloneWidth^2 * 2^2
    }
}
