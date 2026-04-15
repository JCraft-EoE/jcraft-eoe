package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.MoveUsage;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static net.arna.jcraft.api.Attacks.damageLogic;

public final class OverheatAttack extends AbstractMove<OverheatAttack, SpeedKingEntity> {
    private static final double SEARCH_RANGE = 32.0;
    private static final float BURST_DAMAGE = 0.25f;

    private static final int HIT_INTERVAL = 2;
    private static final float BLOCK_BURST_DAMAGE = 1.0f;
    private static final double BLOCK_BURST_RADIUS = 2.5;

    private record BurstEntry(WeakReference<LivingEntity> target, AtomicInteger hitsRemaining, MoveUsage moveUsage, AtomicInteger tickDelay) {}
    private static final Map<UUID, List<BurstEntry>> ACTIVE_BURSTS = new HashMap<>();

    public OverheatAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull MoveType<OverheatAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        if (attacker.level().isClientSide()) return Set.of();

        List<LivingEntity> heatedTargets = attacker.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(user.position().subtract(SEARCH_RANGE, SEARCH_RANGE, SEARCH_RANGE),
                        user.position().add(SEARCH_RANGE, SEARCH_RANGE, SEARCH_RANGE)),
                e -> e != user && e != attacker && e.isAlive()
                        && HeatTrapManager.getHeat(e) > 0
                        && user.getUUID().equals(HeatTrapManager.getAttackerUUID(e)));

        List<BurstEntry> entries = new ArrayList<>();
        for (LivingEntity target : heatedTargets) {
            int heat = HeatTrapManager.getHeat(target);
            entries.add(new BurstEntry(new WeakReference<>(target), new AtomicInteger(heat * 2), attacker.getMoveUsage(), new AtomicInteger(1)));
            HeatTrapManager.clearHeat(target);

            JCraft.createParticle((ServerLevel) attacker.level(),
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), JParticleType.BOOM);
        }
        if (!entries.isEmpty()) {
            ACTIVE_BURSTS.put(attacker.getUUID(), entries);
            JUtils.serverPlaySound(JSoundRegistry.KQ_EXPLODE.get(), (ServerLevel) attacker.level(), user.position(), 96);
        }

        // Detonate all heated blocks
        ServerLevel serverLevel = (ServerLevel) attacker.level();
        List<BlockPos> heatedBlocks = HeatTrapManager.detonateHeatedBlocks(serverLevel, user.getUUID());
        DamageSource blockDamageSource = JDamageSources.stand(attacker);
        for (BlockPos pos : heatedBlocks) {
            Vec3 center = Vec3.atCenterOf(pos);
            serverLevel.sendParticles(ParticleTypes.FLAME, center.x, center.y + 1.0, center.z, 12, 0.4, 0.4, 0.4, 0.05);
            serverLevel.sendParticles(ParticleTypes.LAVA, center.x, center.y + 1.0, center.z, 4, 0.3, 0.2, 0.3, 0.0);
            serverLevel.getEntitiesOfClass(LivingEntity.class,
                    new AABB(center.subtract(BLOCK_BURST_RADIUS, BLOCK_BURST_RADIUS, BLOCK_BURST_RADIUS),
                             center.add(BLOCK_BURST_RADIUS, BLOCK_BURST_RADIUS, BLOCK_BURST_RADIUS)),
                    e -> e != user && e != attacker && e.isAlive())
                .forEach(e -> {
                    e.invulnerableTime = 0;
                    damageLogic(serverLevel, e, new AttackData(
                            e.position().subtract(center).normalize().scale(0.3), 10,
                            StunType.UNBURSTABLE.ordinal(), true,
                            BLOCK_BURST_DAMAGE, false, 0, blockDamageSource, user,
                            CommonHitPropertyComponent.HitAnimation.MID, attacker.getMoveUsage(), false, true));
                });
        }
        if (!heatedBlocks.isEmpty()) {
            JUtils.serverPlaySound(JSoundRegistry.KQ_EXPLODE.get(), serverLevel, user.position(), 64);
        }

        if (heatedTargets.isEmpty() && heatedBlocks.isEmpty()) return Set.of();

        return Set.of();
    }

    public static void tickBursts(final ServerLevel level, final SpeedKingEntity stand) {
        List<BurstEntry> entries = ACTIVE_BURSTS.get(stand.getUUID());
        if (entries == null) return;

        LivingEntity user = stand.getUserOrThrow();
        DamageSource damageSource = JDamageSources.stand(stand);

        entries.removeIf(entry -> {
            LivingEntity target = entry.target().get();
            if (target == null || !target.isAlive() || target.level() != level) return true;
            if (entry.hitsRemaining().get() <= 0) return true;

            if (entry.tickDelay().decrementAndGet() > 0) return false;
            entry.tickDelay().set(HIT_INTERVAL);

            target.invulnerableTime = 0;
            Vec3 kbVec = target.position().subtract(user.position()).normalize().scale(0.1);
            damageLogic(level, target, new AttackData(
                    kbVec, 10, StunType.UNBURSTABLE.ordinal(), true,
                    BURST_DAMAGE, false, 0, damageSource, user,
                    CommonHitPropertyComponent.HitAnimation.MID, entry.moveUsage(), false, true));

            int remaining = entry.hitsRemaining().decrementAndGet();
            if (remaining <= 0) {
                target.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 80, 0, false, true));
                return true;
            }
            return false;
        });

        if (entries.isEmpty()) ACTIVE_BURSTS.remove(stand.getUUID());
    }

    @Override
    protected @NonNull OverheatAttack getThis() { return this; }

    @Override
    public @NonNull OverheatAttack copy() {
        return copyExtras(new OverheatAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<OverheatAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<OverheatAttack>, OverheatAttack> buildCodec(RecordCodecBuilder.Instance<OverheatAttack> instance) {
            return baseDefault(instance, OverheatAttack::new);
        }
    }
}
