package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.splatter.JSplatterManager;
import net.arna.jcraft.api.splatter.Splatter;
import net.arna.jcraft.common.splatter.GasolineSplatter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MatchProjectile extends ThrowableItemProjectile {
    public MatchProjectile(Level level) {
        super(JEntityTypeRegistry.MATCH_PROJECTILE.get(), level);
    }

    public MatchProjectile(LivingEntity shooter, Level level) {
        super(JEntityTypeRegistry.MATCH_PROJECTILE.get(), shooter, level);
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);

        if (level().isClientSide()) return;
        discard();

        // Check if we hit a gasoline splatter
        JSplatterManager splatterManager = JSplatterManager.get(level());
        List<Splatter> gasSplatters = splatterManager.getHit(result.getLocation(), s -> s instanceof GasolineSplatter);

        // Light them on fire
        gasSplatters.forEach(s -> ((GasolineSplatter) s).lightOnFire());
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return JItemRegistry.MATCHBOX.get();
    }
}
