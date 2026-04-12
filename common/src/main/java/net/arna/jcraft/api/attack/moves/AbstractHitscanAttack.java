package net.arna.jcraft.api.attack.moves;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Function11;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.common.attack.core.data.AttackMoveExtras;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.common.compat.FtbChunksCompat;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/**
 * A simple attack that uses ray-cast to hitscan.
 *
 * @param <T>
 * @param <A>
 */
@SuppressWarnings("UnusedReturnValue")
@Getter
public abstract class AbstractHitscanAttack<T extends AbstractHitscanAttack<T, A>, A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<T, A> {
    private float range;
    private float hardness;
    private float breakChance;
    private float spread;
    private @NonNull JParticleType shootSpark = JParticleType.LEMON;

    protected AbstractHitscanAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage,
                                    final int stun, final float knockback,
                                    final float range, final float hardness, final float breakChance, final float spread) {
        super(cooldown, windup, duration, moveDistance, damage, stun, 0f, knockback, 0f);

        withRange(range);
        withHardness(hardness);
        withBreakChance(breakChance);
        withSpread(spread);
    }

    public T withRange(final float range) {
        this.range = range;
        return getThis();
    }

    public T withHardness(final float hardness) {
        this.hardness = hardness;
        return getThis();
    }

    public T withBreakChance(final float breakChance) {
        this.breakChance = breakChance;
        return getThis();
    }

    public T withSpread(final float spread) {
        this.spread = spread;
        return getThis();
    }

    public T withShootSpark(final JParticleType shootSpark) {
        this.shootSpark = shootSpark;
        return getThis();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        if (user == null) {
            return Set.of();
        }
        final RandomSource random = user.getRandom();
        // finding target
        final Vec3 userEyePos = user.position().add(GravityChangerAPI.getEyeOffset(user));
        final Vec3 rotVec = user.getLookAngle();
        final HitResult goal = JUtils.raycastAll(user, userEyePos, userEyePos.add(rotVec.scale(getRange())), ClipContext.Fluid.NONE, EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_SPECTATORS));
        final Vec3 goalLocation = goal.getLocation().add(rotVec);
        final Vec3 attackerEyePos = attacker.getBaseEntity().position().add(GravityChangerAPI.getEyeOffset(attacker.getBaseEntity()));
        final Vec3 attackVector = goalLocation.subtract(attackerEyePos)
                .xRot((float)random.nextGaussian() * spread)
                .yRot((float)random.nextGaussian() * spread)
                .zRot((float)random.nextGaussian() * spread);
        final HitResult hitResult = JUtils.raycastAll(attacker.getBaseEntity(), attackerEyePos, attackerEyePos.add(attackVector), ClipContext.Fluid.ANY, EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_SPECTATORS));
        // entity hit
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            final Entity hitEntity = ((EntityHitResult)hitResult).getEntity();
            if (hitEntity instanceof LivingEntity living) { // should always happen
                final Vec3 kbVec = rotVec.scale(getKnockback()).add(new Vec3(0.0, Math.abs(getKnockback()) / 4, 0.0));
                processTarget(attacker, living, kbVec, attacker.getDamageSource());
                return Set.of(living);
            }
        }
        // block mining
        else if (hitResult.getType() == HitResult.Type.BLOCK && user.level().getGameRules().getBoolean(JCraft.STAND_GRIEFING) && getBreakChance() > 0f) {
            final BlockPos pos = ((BlockHitResult)hitResult).getBlockPos();
            final BlockState state = user.level().getBlockState(pos);
            if (state.getFluidState().isEmpty()) {
                double hardness = state.getBlock().defaultDestroyTime();
                if (hardness < 0) {
                    hardness = Double.POSITIVE_INFINITY;
                }
                boolean chunkAccess = !(user instanceof ServerPlayer player) || FtbChunksCompat.get().mayEdit(player, (ServerLevel) player.level(), pos);
                if (getHardness() >= hardness && chunkAccess && random.nextDouble() >= getBreakChance()) {
                    user.level().destroyBlock(pos, true, user);
                }
            }
        }
        // create particles
        JCraft.createHitscanTraceParticle((ServerLevel)user.level(), hitscanTraceParticleOrigin(attacker), hitscanTraceParticleVelocity(attacker, hitResult.getLocation()), shootSpark);
        // TODO Arna add hit/block particles?
        if (hitResult.getType() != HitResult.Type.MISS) {
            JCraft.createParticle((ServerLevel)user.level(),
                    hitResult.getLocation().x() + random.nextGaussian() * 0.25,
                    hitResult.getLocation().y() + random.nextGaussian() * 0.25,
                    hitResult.getLocation().z() + random.nextGaussian() * 0.25,
                    hitSpark);
        }
        return Set.of();
    }

    protected Vec3 hitscanTraceParticleOrigin(final A attacker) {
        return attacker.getBaseEntity().getEyePosition();
    }

    protected Vec3 hitscanTraceParticleVelocity(final A attacker, final Vec3 goal) {
        return goal.subtract(attacker.getBaseEntity().getEyePosition());
    }

    @Override
    protected @NonNull T copyExtras(@NonNull final T base) {
        T copy = super.copyExtras(base);
        copy.withShootSpark(shootSpark);
        return copy;
    }

    protected abstract static class Type<M extends AbstractHitscanAttack<? extends M, ?>> extends AbstractSimpleAttack.Type<M> {
        protected RecordCodecBuilder<M, Float> range() {
            return Codec.FLOAT.fieldOf("range").forGetter(AbstractHitscanAttack::getRange);
        }

        protected RecordCodecBuilder<M, Float> hardness() {
            return Codec.FLOAT.fieldOf("hardness").forGetter(AbstractHitscanAttack::getHardness);
        }

        protected RecordCodecBuilder<M, Float> breakChance() {
            return Codec.FLOAT.fieldOf("breakChance").forGetter(AbstractHitscanAttack::getBreakChance);
        }

        protected RecordCodecBuilder<M, Float> spread() {
            return Codec.FLOAT.fieldOf("spread").forGetter(AbstractHitscanAttack::getSpread);
        }

        protected RecordCodecBuilder<M, JParticleType> shootSpark() {
            return JParticleType.CODEC.optionalFieldOf("shootSpark", JParticleType.FLASH)
                    .forGetter(AbstractHitscanAttack::getShootSpark);
        }

        protected Products.P14<RecordCodecBuilder.Mu<M>, BaseMoveExtras, AttackMoveExtras, Integer, Integer, Integer, Float,
                Float, Integer, Float, Float, Float, Float, Float, JParticleType>
        hitscanDefault(RecordCodecBuilder.Instance<M> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(), damage(), stun(),
                    knockback(), range(), hardness(), breakChance(), spread(), shootSpark());
        }

        protected App<RecordCodecBuilder.Mu<M>, M> hitscanDefault(RecordCodecBuilder.Instance<M> instance, Function11<Integer, Integer, Integer, Float,
                                                                Float, Integer, Float, Float, Float, Float, Float, M> function) {
            return hitscanDefault(instance).apply(instance, applyAttackExtras((cooldown, windup, duration,
                                                             moveDistance, damage, stun, knockback, range, hardness,
                                                             breakChance, spread, shootSpark) -> {
                M move = function.apply(cooldown, windup, duration, moveDistance, damage, stun, knockback, range, hardness, breakChance, spread);
                move.withShootSpark(shootSpark);
                return move;
            }));
        }
    }
}
