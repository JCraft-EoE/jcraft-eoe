package net.arna.jcraft.common.effects;

import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.Gravity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleTypes;

import java.util.Random;

public class WeightlessStatusEffect extends StatusEffect {
    private boolean previouslyNoGravved = false;
    private final Random random = new Random();

    public WeightlessStatusEffect() {
        super(StatusEffectCategory.NEUTRAL, 0x000011);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return amplifier == 1;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        //entity.addVelocity(gravDir.x, gravDir.y, gravDir.z);
        entity.world.addParticle(
                ParticleTypes.REVERSE_PORTAL,
                entity.getX() + random.nextDouble() - 0.5,
                entity.getY() + random.nextDouble() * 1.8,
                entity.getZ() + random.nextDouble() - 0.5,
                0, 0, 0
        );
    }

    @Override
    public void onApplied(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onApplied(entity, attributes, amplifier);

        if (entity.getWorld().isClient) return;
        this.previouslyNoGravved = entity.hasNoGravity();
        if (amplifier == 1)
            GravityChangerAPI.addGravity(entity, new Gravity(entity.getMovementDirection(), 1, 60, "command"));
        else entity.setNoGravity(true);
    }

    @Override
    public void onRemoved(LivingEntity entity, AttributeContainer attributes, int amplifier) {
        super.onRemoved(entity, attributes, amplifier);

        if (entity.getWorld().isClient) return;
        if (!previouslyNoGravved && amplifier != 1)
            entity.setNoGravity(false);
    }
}
