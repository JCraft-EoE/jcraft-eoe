package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

@Getter
public final class PureHeatAccumulationAttack extends AbstractSimpleAttack<PureHeatAccumulationAttack, SpeedKingEntity> {
    private static final double SPAWN_DISTANCE = 9.0;
    private static final double VERTICAL_OFFSET = 2.0;
    private static final int EFFECT_RADIUS = 5;
    private static final int BLOCK_HEAT_DURATION = 100;


    private final int boilingDuration;
    private Vec3 attackCenter = Vec3.ZERO;

    public PureHeatAccumulationAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                      final float damage, final int stun, final float hitboxSize, final float knockback,
                                      final float offset, final int boilingDuration) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.boilingDuration = boilingDuration;
    }

    @Override
    public @NonNull MoveType<PureHeatAccumulationAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        attackCenter = user.position().add(user.getLookAngle().scale(SPAWN_DISTANCE));

        BlockPos blockCenter = new BlockPos((int) attackCenter.x, (int) attackCenter.y, (int) attackCenter.z);

        attacker.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(attackCenter.add(-EFFECT_RADIUS, -2, -EFFECT_RADIUS), attackCenter.add(EFFECT_RADIUS, 2, EFFECT_RADIUS)),
                entity -> entity != user && entity != attacker)
                .forEach(entity -> {
                    entity.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), boilingDuration, 0, false, true));
                    HeatTrapManager.addHeat(entity, user);
                });

        heatBlocks(attacker.level(), blockCenter, user.getUUID());
        removeVegetation(attacker.level(), blockCenter);
        spawnHitboxParticles(attacker);

        return Set.of();
    }

    private void spawnHitboxParticles(SpeedKingEntity attacker) {
        if (!(attacker.level() instanceof ServerLevel serverLevel)) return;
        float r = getHitboxSize() * 0.5f;
        Vec3 center = attackCenter.add(0, VERTICAL_OFFSET, 0);
        for (int i = 0; i < 40; i++) {
            double angle = (2 * Math.PI * i) / 40;
            double x = center.x + r * Math.cos(angle);
            double z = center.z + r * Math.sin(angle);
            for (double y = center.y - r; y <= center.y + r; y += r * 0.4) {
                serverLevel.sendParticles(ParticleTypes.FLAME, x, y, z, 1, 0.05, 0.05, 0.05, 0.02);
            }
        }
    }

    private void heatBlocks(Level level, BlockPos center, UUID attackerUUID) {
        for (int x = -EFFECT_RADIUS; x <= EFFECT_RADIUS; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -EFFECT_RADIUS; z <= EFFECT_RADIUS; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!level.getBlockState(pos).isAir()) {
                        HeatTrapManager.heatBlock(level, pos, BLOCK_HEAT_DURATION, attackerUUID);
                    }
                }
            }
        }
    }

    private void removeVegetation(Level level, BlockPos center) {
        for (int x = -EFFECT_RADIUS; x <= EFFECT_RADIUS; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -EFFECT_RADIUS; z <= EFFECT_RADIUS; z++) {
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

    @Override
    protected @NonNull PureHeatAccumulationAttack getThis() {
        return this;
    }

    @Override
    public @NonNull PureHeatAccumulationAttack copy() {
        return copyExtras(new PureHeatAccumulationAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), boilingDuration));
    }

    public static class Type extends AbstractSimpleAttack.Type<PureHeatAccumulationAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<PureHeatAccumulationAttack>, PureHeatAccumulationAttack> buildCodec(RecordCodecBuilder.Instance<PureHeatAccumulationAttack> instance) {
            return attackDefault(instance, (cd, wu, dur, md, dmg, st, hb, kb, off) ->
                    new PureHeatAccumulationAttack(cd, wu, dur, md, dmg, st, hb, kb, off, 200));
        }
    }
}
