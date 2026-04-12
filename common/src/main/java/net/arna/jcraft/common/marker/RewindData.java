package net.arna.jcraft.common.marker;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Used by rewind moves for the visuals.
 * @param originalPos
 * @param entity
 */
public record RewindData(Vec3 originalPos, Entity entity) {
}
