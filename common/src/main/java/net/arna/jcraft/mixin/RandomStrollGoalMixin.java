package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.arna.jcraft.api.stand.StandEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RandomStrollGoal.class)
public abstract class RandomStrollGoalMixin {

    @Shadow @Final protected PathfinderMob mob;

    @Shadow protected boolean forceTrigger;

    @Shadow @Final private boolean checkNoActionTime;

    @Shadow protected int interval;

    @Shadow @Nullable protected abstract Vec3 getPosition();

    @Shadow protected double wantedX;

    @Shadow protected double wantedZ;

    @Shadow protected double wantedY;

    @ModifyReturnValue(
            method = "canUse",
            at = @At(value = "RETURN", ordinal = 0)
    )
    private boolean jcraft$walkWithStand(final boolean original) {
        if (mob.getFirstPassenger() instanceof StandEntity<?,?>) {
            if (!this.forceTrigger) {
                if (this.checkNoActionTime && this.mob.getNoActionTime() >= 100) {
                    return false;
                }

                if (this.mob.getRandom().nextInt(copyOf$reducedTickDelay(this.interval)) != 0) {
                    return false;
                }
            }

            Vec3 vec3 = this.getPosition();
            if (vec3 == null) {
                return false;
            } else {
                this.wantedX = vec3.x;
                this.wantedY = vec3.y;
                this.wantedZ = vec3.z;
                this.forceTrigger = false;
                return true;
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "canContinueToUse", at = @At("RETURN"))
    private boolean jcraft$allowWalkingWithStand(final boolean original) {
        return original || (mob.getFirstPassenger() instanceof StandEntity<?,?> && !this.mob.getNavigation().isDone());
    }

    @Unique
    private static int copyOf$reducedTickDelay(final int reduction) {
        return Mth.positiveCeilDiv(reduction, 2);
    }

}
