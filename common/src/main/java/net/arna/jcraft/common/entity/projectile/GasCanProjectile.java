package net.arna.jcraft.common.entity.projectile;

import lombok.Getter;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JSplatterTypeRegistry;
import net.arna.jcraft.api.splatter.JSplatterManager;
import net.arna.jcraft.common.util.JExplosionModifier;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class GasCanProjectile extends ThrowableItemProjectile {
    private Vec3 lastSplatterPos;
    @Getter
    private Vec3 prevDeltaMovement = Vec3.ZERO;

    public GasCanProjectile(Level level) {
        super(JEntityTypeRegistry.GAS_CAN_PROJECTILE.get(), level);
    }

    public GasCanProjectile(LivingEntity shooter, Level level) {
        super(JEntityTypeRegistry.GAS_CAN_PROJECTILE.get(), shooter, level);
    }

    @Override
    public void baseTick() {
        prevDeltaMovement = getDeltaMovement();
        super.baseTick();

        if (!level().isClientSide() && checkHitFire(position()))
            discard();
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return JItemRegistry.GAS_CAN.get();
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            this.discard();

            if (checkHitFire(result.getLocation())) return;

            // Drop 5 splatters close to where the can hit.
            for (int i = 0; i < 5; i++) {
                float dx = random.nextFloat() - 0.5f;
                float dy = random.nextFloat() - 0.5f;
                float dz = random.nextFloat() - 0.5f;
                float size = random.nextFloat() * 0.25f + 0.75f;

                Vec3 splatterPos = result.getLocation().add(dx, dy, dz);
                splatter(splatterPos, size);
            }

            // Play sound
            playSound(JSoundRegistry.GAS_CAN_CRASH.get());
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) return;

        if (lastSplatterPos == null || lastSplatterPos.distanceToSqr(position()) >= 0.5) {
            dropSplatter();
            lastSplatterPos = position();
        }
    }

    // Gas can explodes when it hits fire
    private boolean checkHitFire(Vec3 pos) {
        if (!level().getBlockState(BlockPos.containing(pos)).is(BlockTags.FIRE))
            return false;

        // Only destroy blocks if we're allowed to.
        Explosion.BlockInteraction blockInteraction = getOwner() instanceof LivingEntity owner &&
                JUtils.mayAlter(level(), owner, BlockPos.containing(pos), null)
                ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.KEEP;

        // We hit fire, explode into a fireball
        JUtils.explode(level(), pos.x, pos.y, pos.z, 2f, JExplosionModifier.builder()
                .createFire(true)
                .blockInteraction(blockInteraction)
                .build());
        return true;
    }

    // Places a splatter on the ground directly below the projectile.
    private void dropSplatter() {
        Level level = level();

        BlockHitResult hit = level.clip(new ClipContext(position(), position().relative(Direction.DOWN, level.getHeight()),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, this));

        BlockPos pos = hit.getBlockPos().relative(hit.getDirection());
        splatter(position().with(Direction.Axis.Y, pos.getY()), 0.6f);
    }

    private void splatter(Vec3 pos, float sizeMult) {
        JSplatterManager splatterManager = JUtils.getSplatterManager(level());
        float xRange = (random.nextFloat() * 0.5f + 0.5f) * sizeMult;
        float zRange = (random.nextFloat() * 0.5f + 0.5f) * sizeMult;
        splatterManager.addSplatter(pos, JSplatterTypeRegistry.GASOLINE_SPLATTER_TYPE, xRange, zRange, 300,
                (LivingEntity) getOwner());

        level().playSound(this, BlockPos.containing(pos), JSoundRegistry.GAS_CAN_SPILL.get(), SoundSource.NEUTRAL,
                1f, 1f);
    }
}
