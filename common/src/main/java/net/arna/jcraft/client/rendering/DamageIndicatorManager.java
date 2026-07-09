package net.arna.jcraft.client.rendering;

import lombok.experimental.UtilityClass;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.common.entity.TrainingDummyEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.WeakHashMap;

@UtilityClass
public class DamageIndicatorManager {
    private static final Map<TrainingDummyEntity, Integer> dummyPositionCounter = new WeakHashMap<>();

    /**
     * Spawns a damage number particle at the entity's location
     */
    public static void spawnDamageNumber(Entity entity, float damageAmount) {
        if (!entity.level().isClientSide) {
            return;
        }

        Level level = entity.level();
        int animationPos = 0;

        // gets the next position index
        if (entity instanceof TrainingDummyEntity dummy) {
            animationPos = getNextNumberPos(dummy);
        }

        // Spawn the particle
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() * 0.4; //height of the thing
        double z = entity.getZ();

        // Pass the animation position as the Z speed parameter
        // The particle will use this to determine its horizontal offset
        level.addParticle(JParticleTypeRegistry.DAMAGE_NUMBER.get(),
                x, y, z,
                damageAmount, 0, animationPos);
    }

    /**
     * Gets the next position index for a training dummy to prevent overlapping damage numbers
     */
    private static int getNextNumberPos(TrainingDummyEntity dummy) {
        Integer current = dummyPositionCounter.get(dummy);
        if (current == null) {
            current = 0;
        }
        current = (current + 1) % 5; // Cycle through 5 positions
        dummyPositionCounter.put(dummy, current);
        return current;
    }
}