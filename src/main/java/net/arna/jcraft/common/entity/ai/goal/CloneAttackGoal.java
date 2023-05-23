package net.arna.jcraft.common.entity.ai.goal;

import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.util.Hand;

import java.util.EnumSet;

public class CloneAttackGoal extends Goal {

    private final PlayerCloneEntity mob;
    private Path path;
    private LivingEntity target;
    private final double speed;
    private int cooldown;
    private long lastUpdateTime;

    public CloneAttackGoal(PlayerCloneEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    public boolean canStart() {
        long l = this.mob.world.getTime();
        if (l - this.lastUpdateTime < 20L) {
            return false;
        } else {
            this.lastUpdateTime = l;
            this.target = this.mob.getTarget();
            if (target == null) {
                return false;
            } else if (!target.isAlive()) {
                return false;
            } else {
                this.path = this.mob.getNavigation().findPathTo(target, 0);
                if (this.path != null) {
                    return true;
                } else {
                    return this.getSquaredMaxAttackDistance(target) >= this.mob.squaredDistanceTo(target);
                }
            }
        }
    }

    public boolean shouldContinue() {
        if (this.target == null) {
            return false;
        } else if (!this.target.isAlive()) {
            return false;
        } else if (this.mob.squaredDistanceTo(this.target) > 1024.0D) {
            return false;
        } else if (this.target == this.mob.getOwner()) {
            return false;
        } else {
            return !this.mob.getNavigation().isIdle() || this.canStart();
        }
    }

    public void start() {
        this.mob.getNavigation().startMovingAlong(this.path, this.speed);
        this.mob.setAttacking(true);
        this.cooldown = 0;
    }

    public void stop() {
        this.target = null;
        this.mob.setTarget(null);
        this.mob.setAttacking(false);
        this.mob.getNavigation().stop();
    }

    public boolean shouldRunEveryTick() {
        return true;
    }

    public void tick() {
        if (this.target != null) {
            this.mob.getLookControl().lookAt(this.target, 30.0F, 30.0F);
            this.cooldown = Math.max(this.cooldown - 1, 0);
            double d = this.getSquaredMaxAttackDistance(target);

            if (target.squaredDistanceTo(this.mob) <= d && this.cooldown <= 0) {
                this.cooldown = 20;
                this.mob.swingHand(Hand.MAIN_HAND);
                this.mob.tryAttack(this.target);
            }
        }
    }

    private double getSquaredMaxAttackDistance(LivingEntity entity) {
        return this.mob.getWidth() * 2.0F * this.mob.getWidth() * 2.0F + entity.getWidth();
    }
}
