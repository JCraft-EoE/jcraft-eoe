package net.arna.jcraft.common.effects;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.UUID;

public class DazedStatusEffect extends StatusEffect {

    public DazedStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x444444);
    }

    // Should the status effect be applied and under what condition?
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    // Stun heavily reduces horizontal speed and prevents mobs from attacking
    // Amplifier = Source ID
    // 0 - Soft stun, un combo breakable
    // 1 - Regular stun, combo breakable
    // 2 - Blocking, un combo breakable
    // 3 - Launch, un combo breakable
    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        Vec3d eVel = entity.getVelocity();
        double yVel = eVel.y;
        double horizontalMult = 0.4;
        if (amplifier < 2) { // Hitstun
            yVel = MathHelper.clamp(yVel, -0.5, 0.5);
            horizontalMult = 0.2;
        }
        if (amplifier == 3) {
            horizontalMult = 1;
        }
        entity.setVelocity(eVel.x * horizontalMult, yVel, eVel.z * horizontalMult);

        if (entity instanceof MobEntity mob) {
            mob.setTarget(null);
            mob.setAttacking(false);
        }
    }

    private static final UUID slowUUID = UUID.fromString("778B48FC-485B-5BA7-58C7-E0D755CE354D");

    @Override
    public double adjustModifierAmount(int amplifier, EntityAttributeModifier modifier) {
        if (Objects.equals(modifier.getId(), slowUUID)) {
            return switch (amplifier) {
                case 3, 1, 0 -> -1;
                default -> 0;
            };
        }

        return super.adjustModifierAmount(amplifier, modifier);
    }
}