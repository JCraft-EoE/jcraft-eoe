package net.arna.jcraft.common.food;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;

public class MobFoodData extends FoodData {
    protected static boolean isHurt(LivingEntity livingEntity) {
        return livingEntity.getHealth() > 0.0F && livingEntity.getHealth() < livingEntity.getMaxHealth();
    }

    public void tick(LivingEntity entity) {
        Difficulty difficulty = entity.level().getDifficulty();
        this.lastFoodLevel = this.foodLevel;
        if (this.exhaustionLevel > 4.0F) {
            this.exhaustionLevel -= 4.0F;
            if (this.saturationLevel > 0.0F) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);
            } else if (difficulty != Difficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }

        boolean bl = entity.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
        if (bl && this.saturationLevel > 0.0F && isHurt(entity) && this.foodLevel >= 20) {
            ++this.tickTimer;
            if (this.tickTimer >= 10) {
                float f = Math.min(this.saturationLevel, 6.0F);
                entity.heal(f / 6.0F);
                this.addExhaustion(f);
                this.tickTimer = 0;
            }
        } else if (bl && this.foodLevel >= 18 && isHurt(entity)) {
            ++this.tickTimer;
            if (this.tickTimer >= 80) {
                entity.heal(1.0F);
                this.addExhaustion(6.0F);
                this.tickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.tickTimer;
            if (this.tickTimer >= 80) {
                if (entity.getHealth() > 10.0F || difficulty == Difficulty.HARD || entity.getHealth() > 1.0F && difficulty == Difficulty.NORMAL) {
                    entity.hurt(entity.damageSources().starve(), 1.0F);
                }

                this.tickTimer = 0;
            }
        } else {
            this.tickTimer = 0;
        }

    }
}
