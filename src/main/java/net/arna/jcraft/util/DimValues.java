package net.arna.jcraft.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class DimValues {
    public Entity user;
    public Vec3d pos;
    public RegistryKey<World> worldKey;
    public int timer = 300;

    public DimValues(Entity user, Vec3d pos, RegistryKey<World> worldKey) { this.user = user; this.pos = pos; this.worldKey = worldKey; }
}
