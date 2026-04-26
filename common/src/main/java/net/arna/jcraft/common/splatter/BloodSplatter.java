package net.arna.jcraft.common.splatter;

import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.registry.JSplatterTypeRegistry;
import net.arna.jcraft.api.splatter.SplatterFactory;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BloodSplatter extends Splatter {
    private static final ResourceLocation texture = JCraft.id("textures/effect/splatter/blood.png");

    public BloodSplatter(Level world, Vec3 pos, Direction direction, float xRange, float zRange, int maxAge, @Nullable LivingEntity creator) {
        super(world, pos, direction, xRange, zRange, maxAge, creator);
    }

    @Override
    public ResourceLocation getTexture() {
        return texture;
    }

    @Override
    public RegistrySupplier<SplatterFactory> getType() {
        return JSplatterTypeRegistry.BLOOD_SPLATTER_TYPE;
    }
}
