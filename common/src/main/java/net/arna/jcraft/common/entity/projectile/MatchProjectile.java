package net.arna.jcraft.common.entity.projectile;

import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.splatter.JSplatterManager;
import net.arna.jcraft.api.splatter.Splatter;
import net.arna.jcraft.common.effects.FlammableEffect;
import net.arna.jcraft.common.splatter.GasolineSplatter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
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

        if (!level().isClientSide()) {
            checkGas(position());
            checkBlocks(blockPosition());
        }
    }

    private void checkGas(Vec3 position) {
        JSplatterManager splatterManager = JSplatterManager.get(level());
        List<Splatter> gasSplatters = splatterManager.getHit(position, s -> s instanceof GasolineSplatter);
        gasSplatters.forEach(s -> ((GasolineSplatter) s).lightOnFire());
    }

    private void checkBlocks(BlockPos pos) {
        Level level = level();

        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        BlockState state = level.getBlockState(pos);

        // TNT minecart — only prime once, guard with isPrimed()
        level.getEntitiesOfClass(MinecartTNT.class, new AABB(pos).inflate(1))
                .stream()
                .filter(minecart -> !minecart.isPrimed())
                .forEach(MinecartTNT::primeFuse);

        // TNT block — check in a small radius around the match
        BlockPos.betweenClosedStream(new AABB(pos).inflate(0.075))
                .map(BlockPos::immutable)
                .filter(p -> level.getBlockState(p).is(Blocks.TNT))
                .findFirst()
                .ifPresent(p -> {
                    TntBlock.explode(level, p);
                    level.removeBlock(p, false);
                    discard();
                });

        // Unlit campfire
        for (BlockPos target : new BlockPos[]{pos, below}) {
            BlockState s = level.getBlockState(target);
            if ((s.is(Blocks.CAMPFIRE) || s.is(Blocks.SOUL_CAMPFIRE))
                    && !s.getValue(CampfireBlock.LIT)
                    && !s.getValue(BlockStateProperties.WATERLOGGED)) {
                level.setBlock(target, s.setValue(CampfireBlock.LIT, true), 11);
                discard();
                return;
            }
        }

        // Netherrack / soul sand / soul soil
        if (isFlammableNetherBlock(belowState)) {
            level.setBlock(pos, BaseFireBlock.getState(level, pos), 11);
            discard();
            return;
        }

        // Portal
        if (belowState.is(Blocks.OBSIDIAN)) {
            if (state.isAir()) {
                level.setBlock(pos, BaseFireBlock.getState(level, pos), 11);
                discard();
                return;
            }
        }

        if (!level().isClientSide()) {
            checkGas(position());
            // Light flammable players on fire
            level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.25),
                            e -> FlammableEffect.isFlammable(e) && !e.isOnFire())
                    .forEach(e -> e.setSecondsOnFire(5));
        }

        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = pos.relative(dir);
            if (level.getBlockState(neighbor).is(Blocks.OBSIDIAN)) {
                if (state.isAir()) {
                    level.setBlock(pos, BaseFireBlock.getState(level, pos), 11);
                    discard();
                    return;
                }
            }
        }
    }

    private boolean isFlammableNetherBlock(BlockState state) {
        return state.is(Blocks.NETHERRACK)
                || state.is(Blocks.SOUL_SAND)
                || state.is(Blocks.SOUL_SOIL)
                || state.is(Blocks.CRIMSON_NYLIUM)
                || state.is(Blocks.WARPED_NYLIUM);
    }
}