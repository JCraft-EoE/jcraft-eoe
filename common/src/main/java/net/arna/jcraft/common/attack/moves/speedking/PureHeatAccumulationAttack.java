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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

@Getter
public final class PureHeatAccumulationAttack extends AbstractSimpleAttack<PureHeatAccumulationAttack, SpeedKingEntity> {
    private static final int EFFECT_RADIUS = 5;
    private static final int BLOCK_HEAT_DURATION = 200;

    public PureHeatAccumulationAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                      final float damage, final int stun, final float hitboxSize, final float knockback,
                                      final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull MoveType<PureHeatAccumulationAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected void processTarget(final SpeedKingEntity attacker, final LivingEntity target,
                                 final Vec3 kbVec, final DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);
        HeatTrapManager.addHeat(target, attacker.getUserOrThrow());
        target.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 200, 0, false, true));
    }

    @Override
    protected void performHook(final SpeedKingEntity attacker, final Set<LivingEntity> targets, final Set<AABB> boxes,
                               final DamageSource damageSource, final Vec3 forwardPos, final Vec3 rotationVector) {
        UUID attackerUUID = attacker.getUserOrThrow().getUUID();
        heatFloorBlocks(attacker.level(), BlockPos.containing(forwardPos), attackerUUID);
    }

    private void heatFloorBlocks(Level level, BlockPos center, UUID attackerUUID) {
        for (int x = -EFFECT_RADIUS; x <= EFFECT_RADIUS; x++) {
            for (int z = -EFFECT_RADIUS; z <= EFFECT_RADIUS; z++) {
                for (int dy = 0; dy >= -1; dy--) {
                    BlockPos pos = center.offset(x, dy, z);
                    if (!level.getBlockState(pos).isAir()) {
                        HeatTrapManager.heatBlock(level, pos, BLOCK_HEAT_DURATION, attackerUUID);
                        if (level instanceof ServerLevel serverLevel) {
                            double px = pos.getX() + 0.5 + (Math.random() - 0.5) * 0.6;
                            double py = pos.getY() + 1.0;
                            double pz = pos.getZ() + 0.5 + (Math.random() - 0.5) * 0.6;
                            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, px, py, pz,
                                    2, 0.2, 0.1, 0.2, 0.02);
                        }
                        break;
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
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<PureHeatAccumulationAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<PureHeatAccumulationAttack>, PureHeatAccumulationAttack> buildCodec(RecordCodecBuilder.Instance<PureHeatAccumulationAttack> instance) {
            return attackDefault(instance, (cd, wu, dur, md, dmg, st, hb, kb, off) ->
                    new PureHeatAccumulationAttack(cd, wu, dur, md, dmg, st, hb, kb, off));
        }
    }
}
