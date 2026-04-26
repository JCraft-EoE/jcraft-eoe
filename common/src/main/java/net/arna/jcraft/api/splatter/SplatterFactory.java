package net.arna.jcraft.api.splatter;

import net.arna.jcraft.common.splatter.Splatter;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface SplatterFactory {
    @NotNull
    Splatter create(Level world, Vec3 pos, Direction direction, float xRange, float zRange, int age,
                           @Nullable LivingEntity creator);
}
