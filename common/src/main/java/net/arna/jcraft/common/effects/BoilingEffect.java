package net.arna.jcraft.common.effects;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class BoilingEffect extends MobEffect {
    private static final AttributeModifier SPEED_MODIFIER = new AttributeModifier(
            UUID.fromString("778B48FC-485B-5BA7-58C7-E0D755CE354D"), "Boiling slowness", -0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);

    public BoilingEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF4500); // Orange-red color for heat
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        Level level = entity.level();

        // Deal damage over time (similar to poison)
        entity.hurt(entity.damageSources().inFire(), 1.0f + amplifier * 0.5f);

        // Screen flash effect every 30 ticks (1.5 seconds) - blindness for 1 second
        if (entity.tickCount % 30 == 0) {
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20, 10, true, false));
        }

        // Destroy nearby plants and water every 20 ticks
        if (entity.tickCount % 20 == 0) {
            destroyNearbyPlantsAndWater(entity, level);
        }
    }

    private void destroyNearbyPlantsAndWater(LivingEntity entity, Level level) {
        Vec3 pos = entity.position();
        int radius = 2; // Small radius around the entity

        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos blockPos = new BlockPos((int) pos.x + x, (int) pos.y + y, (int) pos.z + z);
                    BlockState state = level.getBlockState(blockPos);

                    if (state.is(BlockTags.FLOWERS) ||
                            state.is(BlockTags.CROPS) ||
                            state.is(BlockTags.SAPLINGS) ||
                            state.is(Blocks.GRASS) ||
                            state.is(Blocks.TALL_GRASS) ||
                            state.is(Blocks.FERN) ||
                            state.is(Blocks.LARGE_FERN) ||
                            state.is(Blocks.DEAD_BUSH) ||
                            state.is(Blocks.SEAGRASS) ||
                            state.is(Blocks.TALL_SEAGRASS) ||
                            state.is(Blocks.VINE) ||
                            state.is(Blocks.LILY_PAD) ||
                            state.is(Blocks.KELP) ||
                            state.is(Blocks.KELP_PLANT)) {
                        level.destroyBlock(blockPos, false);
                    }

                    // Destroy water
                    if (state.is(Blocks.WATER)) {
                        level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // Apply effect every tick for continuous damage and environmental destruction
        return true;
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);

        // Apply movement speed reduction - check if modifier isn't already applied
        if (entity.getAttribute(Attributes.MOVEMENT_SPEED) != null &&
                !entity.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(SPEED_MODIFIER)) {
            entity.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(SPEED_MODIFIER);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);

        // Remove movement speed reduction
        if (entity.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            entity.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(SPEED_MODIFIER);
        }
    }
}