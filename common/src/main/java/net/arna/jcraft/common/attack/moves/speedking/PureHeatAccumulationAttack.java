package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PureHeatAccumulationAttack extends AbstractSimpleAttack<PureHeatAccumulationAttack, SpeedKingEntity> {
    private static final Map<String, Long> HEATED_BLOCKS = new HashMap<>();
    private static final Map<String, Long> ACTIVE_GEYSER = new HashMap<>();

    public PureHeatAccumulationAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                      final float damage, final int stun, final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull MoveType<PureHeatAccumulationAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected void processTarget(SpeedKingEntity attacker, LivingEntity target, Vec3 kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);
        target.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 200, 0, false, true));
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        Vec3 userPosition = user.position();
        Vec3 userLookDirection = user.getLookAngle();
        Vec3 attackCenter = userPosition.add(userLookDirection.scale(8.0));

        Vec3 originalPos = attacker.position();
        attacker.setPos(attackCenter.x, attackCenter.y + 2, attackCenter.z);
        Set<LivingEntity> hitTargets = super.perform(attacker, user);
        attacker.setPos(originalPos.x, originalPos.y, originalPos.z);

        BlockPos blockCenter = new BlockPos((int) attackCenter.x, (int) attackCenter.y, (int) attackCenter.z);
        int effectRadius = 5;

        List<LivingEntity> areaEntities = attacker.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(attackCenter.add(-effectRadius, -2, -effectRadius), attackCenter.add(effectRadius, 2, effectRadius)),
                entity -> entity != user && entity != attacker);

        for (LivingEntity entity : areaEntities) {
            entity.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 200, 0, false, true));
        }

        heatBlocks(attacker.level(), blockCenter, effectRadius);
        spawnGeyser(attacker.level(), blockCenter);
        removeVegetation(attacker.level(), blockCenter, effectRadius);

        Set<LivingEntity> allTargets = new HashSet<>(hitTargets);
        allTargets.addAll(areaEntities);
        return allTargets;
    }

    private void heatBlocks(Level level, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!level.getBlockState(pos).isAir()) {
                        String key = level.dimension().location() + "_" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
                        HEATED_BLOCKS.put(key, level.getGameTime() + 100);
                    }
                }
            }
        }
    }

    private void spawnGeyser(Level level, BlockPos center) {
        if (!(level instanceof ServerLevel)) return;

        String key = level.dimension().location() + "_" + center.getX() + "_" + center.getY() + "_" + center.getZ();
        ACTIVE_GEYSER.put(key, level.getGameTime() + 100);
    }

    private void removeVegetation(Level level, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (state.is(BlockTags.FLOWERS) || state.is(BlockTags.CROPS) ||
                            state.is(Blocks.GRASS) || state.is(Blocks.WATER)) {
                        level.destroyBlock(pos, false);
                    }
                }
            }
        }
    }

    public static void tickGeyser(ServerLevel level) {
        ACTIVE_GEYSER.entrySet().removeIf(entry -> {
            long expireTime = entry.getValue();
            long startTime = expireTime - 100;
            long currentTime = level.getGameTime();

            if (currentTime >= expireTime) {
                return true;
            }

            String[] parts = entry.getKey().split("_");
            if (parts.length >= 4) {
                try {
                    int centerX = Integer.parseInt(parts[1]);
                    int centerY = Integer.parseInt(parts[2]);
                    int centerZ = Integer.parseInt(parts[3]);

                    int ticksActive = (int)(currentTime - startTime);
                    double maxHeight = Math.min(6.0, ticksActive / 3.0);

                    int radius = 5;
                    int numPoints = 20;

                    for (int i = 0; i < numPoints; i++) {
                        double angle = (2 * Math.PI * i) / numPoints;
                        double x = radius * Math.cos(angle);
                        double z = radius * Math.sin(angle);

                        for (double h = 0; h <= maxHeight; h += 0.5) {
                            if (level.random.nextFloat() < 0.25f) {
                                level.sendParticles(ParticleTypes.FLAME,
                                        centerX + x + 0.5, centerY + h, centerZ + z + 0.5,
                                        1, 0, 0, 0, 0.05);
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    protected @NonNull PureHeatAccumulationAttack getThis() {
        return this;
    }

    @Override
    public @NonNull PureHeatAccumulationAttack copy() {
        return copyExtras(new PureHeatAccumulationAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<PureHeatAccumulationAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<PureHeatAccumulationAttack>, PureHeatAccumulationAttack> buildCodec(RecordCodecBuilder.Instance<PureHeatAccumulationAttack> instance) {
            return attackDefault(instance, PureHeatAccumulationAttack::new);
        }
    }
}