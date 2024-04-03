package net.arna.jcraft.common.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DimensionData {
    public final LivingEntity user;
    public @Nullable Vec3d pos = null;
    public final RegistryKey<World> worldKey;
    public int timer = 300;

    public DimensionData(LivingEntity user, RegistryKey<World> worldKey, int timer) {
        this.user = user;
        this.worldKey = worldKey;
        this.timer = timer;
    }

    public DimensionData(LivingEntity user, @Nullable Vec3d pos, RegistryKey<World> worldKey) {
        this.user = user;
        this.pos = pos;
        this.worldKey = worldKey;
    }

    public DimensionData(LivingEntity user, @Nullable Vec3d pos, RegistryKey<World> worldKey, int timer) {
        this.user = user;
        this.pos = pos;
        this.worldKey = worldKey;
        this.timer = timer;
    }
}
