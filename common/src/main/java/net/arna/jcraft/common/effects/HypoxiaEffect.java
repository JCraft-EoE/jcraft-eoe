package net.arna.jcraft.common.effects;

import lombok.NonNull;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HypoxiaEffect extends MobEffect {
    private final HashMap<Integer, Tuple<Attribute, AttributeModifier>> attributeModifiersBySeverity = new HashMap<>();
    public HypoxiaEffect() {
        super(MobEffectCategory.HARMFUL, 0x858c30);
        attributeModifiersBySeverity.put(
                0,
                new Tuple<>(
                        Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                UUID.randomUUID(),
                                this::getDescriptionId,
                                -0.2,
                                AttributeModifier.Operation.MULTIPLY_BASE
                        )
                )
        );

        attributeModifiersBySeverity.put(
                1,
                new Tuple<>(
                        Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(
                                UUID.randomUUID(),
                                this::getDescriptionId,
                                -0.2,
                                AttributeModifier.Operation.MULTIPLY_BASE
                        )
                )
        );

        attributeModifiersBySeverity.put(
                2,
                new Tuple<>(
                        Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                UUID.randomUUID(),
                                this::getDescriptionId,
                                -0.2,
                                AttributeModifier.Operation.MULTIPLY_BASE
                        )
                )
        );
    }

    @Override
    public boolean isDurationEffectTick(final int duration, final int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(final @NonNull LivingEntity livingEntity, final int amplifier) {
        super.applyEffectTick(livingEntity, amplifier);

        if (amplifier < 2) return;
        final int airSupply = livingEntity.getAirSupply();
        if (airSupply <= 0) livingEntity.hurt(livingEntity.damageSources().drown(), 2.0F);
        livingEntity.setAirSupply(airSupply - 20);
    }

    @Override
    public void addAttributeModifiers(@NonNull final LivingEntity livingEntity, @NonNull final AttributeMap attributeMap, final int amplifier) {
        for (Map.Entry<Integer, Tuple<Attribute, AttributeModifier>> entry : attributeModifiersBySeverity.entrySet()) {
            // Only apply attribute modifiers appropriate to the amplifier
            if (amplifier < entry.getKey()) continue;

            final Tuple<Attribute, AttributeModifier> modifier = entry.getValue();
            final Attribute attribute = modifier.getA();
            final AttributeInstance attributeInstance = attributeMap.getInstance(attribute);
            if (attributeInstance != null) {
                int multiplier = amplifier;

                if (attribute == Attributes.MOVEMENT_SPEED && multiplier > 2) {
                    multiplier = 2;
                }

                AttributeModifier attributeModifier = modifier.getB();
                attributeInstance.removeModifier(attributeModifier);
                attributeInstance.addPermanentModifier(
                        new AttributeModifier(attributeModifier.getId(),
                                this.getDescriptionId() + " " + multiplier,
                                this.getAttributeModifierValue(multiplier, attributeModifier),
                                attributeModifier.getOperation()
                        )
                );
            }
        }
    }

    @Override
    public void removeAttributeModifiers(@NonNull final LivingEntity livingEntity, @NonNull final AttributeMap attributeMap, int amplifier) {
        for (Map.Entry<Integer, Tuple<Attribute, AttributeModifier>> entry : attributeModifiersBySeverity.entrySet()) {
            // Clear all attribute modifiers irrespective of the amplifier
            Tuple<Attribute, AttributeModifier> modifier = entry.getValue();
            AttributeInstance attributeInstance = attributeMap.getInstance(modifier.getA());
            if (attributeInstance != null) {
                attributeInstance.removeModifier(modifier.getB());
            }
        }
    }
}
