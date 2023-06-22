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
        this.sha = mob;
        this.shaLookControl = sha.getLookControl();
        this.shaNavigation = sha.getNavigation();
        this.speed = speed;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        this.target = this.sha.getTarget();
        return this.target != null;
    }

    public boolean shouldContinue() {
        if (!this.target.isAlive() || this.target.isRemoved())
            return false;
        else if (this.sha.squaredDistanceTo(this.target) > 1024.0D)
            return false;
        else
            return !this.sha.getNavigation().isIdle() || this.canStart();
    }

    public void stop() {
        this.target = null;
        this.sha.getNavigation().stop();
    }

    public boolean shouldRunEveryTick() {
        return true;
    }

    public void tick() {
        shaLookControl.lookAt(this.target, 30.0F, 30.0F);
        shaNavigation.startMovingTo(this.target, this.speed);

        double d = 3.0; // SHA_width^2 * 4
        double e = sha.squaredDistanceTo(this.target);

        if (e <= d && cooldown-- <= 0) {
            cooldown = 200;
            sha.Explode();
        }
    }
}
