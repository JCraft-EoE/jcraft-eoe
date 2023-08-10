package net.arna.jcraft.common.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class DimValues {
    public final LivingEntity user;
    public final Vec3d pos;
    public final RegistryKey<World> worldKey;
    public int timer = 300;

    public DimValues(LivingEntity user, Vec3d pos, RegistryKey<World> worldKey) {
        this.user = user;
        this.pos = pos;
        this.worldKey = worldKey;
    }

    public DimValues(LivingEntity user, Vec3d pos, RegistryKey<World> worldKey, int timer) {
        this.user = user;
        this.pos = pos;
        this.worldKey = worldKey;
        this.timer = timer;
    }
}
