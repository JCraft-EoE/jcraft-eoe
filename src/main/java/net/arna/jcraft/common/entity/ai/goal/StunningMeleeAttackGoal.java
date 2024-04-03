package net.arna.jcraft.common.entity.ai.goal;

import net.arna.jcraft.registry.JStatusRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.Hand;

import java.util.EnumSet;

public class StunningMeleeAttackGoal extends Goal {
    protected final PathAwareEntity mob;
    private final double speed;
    private final boolean pauseWhenMobIdle;
    private final int stunT;
    private Path path;
    private double targetX;
    private double targetY;
    private double targetZ;
    private int updateCountdownTicks;
    private int cooldown;
    private long lastUpdateTime;

    public StunningMeleeAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle, int stunT) {
        this.mob = mob;
        this.speed = speed;
        this.pauseWhenMobIdle = pauseWhenMobIdle;
        this.stunT = stunT;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    public boolean canStart() {
        long l = this.mob.getWorld().getTime();
        if (l - this.lastUpdateTime < 20L) return false;

        lastUpdateTime = l;
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) return false;
        if (!livingEntity.isAlive()) return false;

        path = mob.getNavigation().findPathTo(livingEntity, 0);
        if (path != null) return true;

        return this.getSquaredMaxAttackDistance(livingEntity) >=
                mob.squaredDistanceTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
    }

    public boolean shouldContinue() {
        LivingEntity livingEntity = mob.getTarget();
        if (livingEntity == null) return false;
        if (!livingEntity.isAlive()) return false;
        if (!pauseWhenMobIdle) return !mob.getNavigation().isIdle();
        if (!mob.isInWalkTargetRange(livingEntity.getBlockPos())) return false;

        return !(livingEntity instanceof PlayerEntity) || !livingEntity.isSpectator() && !((PlayerEntity) livingEntity).isCreative();
    }

    public void start() {
        mob.getNavigation().startMovingAlong(path, speed);
        mob.setAttacking(true);
        updateCountdownTicks = 0;
        cooldown = 0;
    }

    public void stop() {
        LivingEntity livingEntity = mob.getTarget();
        if (!EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test(livingEntity)) mob.setTarget(null);

        mob.setAttacking(false);
        mob.getNavigation().stop();
    }

    public boolean shouldRunEveryTick() {
        return true;
    }

    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        mob.getLookControl().lookAt(target, 30.0F, 30.0F);
        double d = mob.squaredDistanceTo(target.getX(), target.getY(), target.getZ());
        updateCountdownTicks = Math.max(updateCountdownTicks - 1, 0);
        if ((pauseWhenMobIdle || mob.getVisibilityCache().canSee(target)) && updateCountdownTicks <= 0 &&
                (targetX == 0.0 && targetY == 0.0 && targetZ == 0.0 ||
                        target.squaredDistanceTo(targetX, targetY, targetZ) >= 1.0 ||
                        mob.getRandom().nextFloat() < 0.05F)) {
            targetX = target.getX();
            targetY = target.getY();
            targetZ = target.getZ();
            updateCountdownTicks = 4 + mob.getRandom().nextInt(7) + (d > 256 ? d > 1024 ? 10 : 5 : 0);

            if (!mob.getNavigation().startMovingTo(target, speed)) updateCountdownTicks += 15;

            updateCountdownTicks = getTickCount(updateCountdownTicks);
        }

        cooldown = Math.max(cooldown - 1, 0);
        attack(target, d);
    }

    protected void attack(LivingEntity target, double squaredDistance) {
        double d = getSquaredMaxAttackDistance(target);
        if (!(squaredDistance <= d) || cooldown > 0) return;

        resetCooldown();
        mob.swingHand(Hand.MAIN_HAND);
        if (mob.tryAttack(target))
            target.addStatusEffect(new StatusEffectInstance(JStatusRegistry.DAZED, this.stunT, 1, true, false));
    }

    protected void resetCooldown() {
        this.cooldown = this.getTickCount(20);
    }

    protected boolean isCooledDown() {
        return this.cooldown <= 0;
    }

    protected int getCooldown() {
        return this.cooldown;
    }

    protected int getMaxCooldown() {
        return this.getTickCount(20);
    }

    protected double getSquaredMaxAttackDistance(LivingEntity entity) {
        return (this.mob.getWidth() * 2.0F * this.mob.getWidth() * 2.0F + entity.getWidth());
    }
}
