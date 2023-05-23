package net.arna.jcraft.entity.ai.goal;

import net.arna.jcraft.entity.SheerHeartAttackEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

public class SHAAttackGoal extends Goal {

    private final SheerHeartAttackEntity sha;
    private LivingEntity target;
    private final double speed;
    private int cooldown;

    public SHAAttackGoal(SheerHeartAttackEntity mob, double speed) {
        this.sha = mob;
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        this.target = this.sha.getTarget();
        return this.target != null;
    }

    public boolean shouldContinue() {
        if (!this.target.isAlive()) {
            return false;
        } else if (this.sha.squaredDistanceTo(this.target) > 1024.0D) {
            return false;
        } else {
            return !this.sha.getNavigation().isIdle() || this.canStart();
        }
    }

    public void stop() {
        this.target = null;
        this.sha.getNavigation().stop();
    }

    public boolean shouldRunEveryTick() {
        return true;
    }

    public void tick() {
        this.sha.getLookControl().lookAt(this.target, 30.0F, 30.0F);
        double d = (this.sha.getWidth() * 2.0F * this.sha.getWidth() * 2.0F) + 2;
        double e = this.sha.squaredDistanceTo(this.target);

        this.sha.getNavigation().startMovingTo(this.target, this.speed);
        this.cooldown = Math.max(this.cooldown - 1, 0);

        if (e <= d && this.cooldown <= 0) {
            this.cooldown = 200;
            this.sha.Explode();
        }
    }
}
