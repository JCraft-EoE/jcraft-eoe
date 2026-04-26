package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.splatter.JSplatterManager;
import net.arna.jcraft.api.splatter.Splatter;
import net.arna.jcraft.common.splatter.GasolineSplatter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public class MatchProjectile extends Projectile {
    private static final float GRAVITY = 0.03F;

    private int landedAt = -1;

    public MatchProjectile(Level level) {
        super(JEntityTypeRegistry.MATCH_PROJECTILE.get(), level);
    }

    public MatchProjectile(LivingEntity shooter, Level level) {
        this(level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();

        if (!isNoGravity())
            setDeltaMovement(getDeltaMovement().add(0, -GRAVITY, 0));

        move(MoverType.SELF, getDeltaMovement());

        if (onGround()) {
            if (landedAt < 0)
                landedAt = tickCount;

            // Friction — damp horizontal movement, kill vertical so it doesn't
            // accumulate against the floor each tick.
            setDeltaMovement(getDeltaMovement().multiply(0.7, 0.0, 0.7));

            // Disappear after two and a half seconds on the ground.
            if (!level().isClientSide() && tickCount - landedAt > 50)
                discard();
        } else {
            setDeltaMovement(getDeltaMovement().multiply(0.99, 0.98, 0.99));
        }

        if (!level().isClientSide())
            checkGas(position());
    }

    private void checkGas(Vec3 position) {
        JSplatterManager splatterManager = JSplatterManager.get(level());
        List<Splatter> gasSplatters = splatterManager.getHit(position, s -> s instanceof GasolineSplatter);
        gasSplatters.forEach(s -> ((GasolineSplatter) s).lightOnFire());
    }
}
