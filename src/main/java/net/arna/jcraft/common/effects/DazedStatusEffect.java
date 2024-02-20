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
    private static final UUID slowUUID = UUID.fromString("778B48FC-485B-5BA7-58C7-E0D755CE354D");

    public DazedStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x444444);
    }

    // Should the status effect be applied and under what condition?
    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    public static boolean canBeComboBroken(int amplifier) {
        return switch (amplifier) {
            case (1), (3), (4) -> true;
            default -> false;
        };
    }

    // Stun heavily reduces horizontal speed and prevents mobs from attacking
    // Amplifier = Source ID
    // 0 - Hitstun, not combo breakable
    // 1 - Hitstun, combo breakable
    // 2 - Blocking, not combo breakable
    // 3 - Launch, not combo breakable
    // 4 - Winded, small movement penalty, combo breakable
    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        Vec3d eVel = entity.getVelocity();
        double yVel = eVel.y;
        double horizontalMult = 0.4;

        if (amplifier < 2) { // Immobilizing stun
            yVel = MathHelper.clamp(yVel, -0.5, 0.5);
            horizontalMult = 0.8;
        } else if (amplifier == 3) horizontalMult = 1;

        entity.setVelocity(eVel.x * horizontalMult, yVel, eVel.z * horizontalMult);

        if (amplifier == 2) return; // Blockstun should not disable targetting
        if (!(entity instanceof MobEntity mob)) return;
        mob.setTarget(null);
        mob.setAttacking(false);
    }

    @Override
    public double adjustModifierAmount(int amplifier, EntityAttributeModifier modifier) {
        if (Objects.equals(modifier.getId(), slowUUID)) return switch (amplifier) {
            case 3, 1, 0 -> -1;
            case 4 -> -0.25;
            default -> 0;
        };

        return super.adjustModifierAmount(amplifier, modifier);
    }
}
