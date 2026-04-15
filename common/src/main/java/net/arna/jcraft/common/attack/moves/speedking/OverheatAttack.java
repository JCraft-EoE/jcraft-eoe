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
    private static final float BURST_DAMAGE = 0.5f;

    private record BurstEntry(WeakReference<LivingEntity> target, AtomicInteger hitsRemaining, MoveUsage moveUsage) {}
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

        if (heatedTargets.isEmpty()) return Set.of();

        List<BurstEntry> entries = new ArrayList<>();
        for (LivingEntity target : heatedTargets) {
            int heat = HeatTrapManager.getHeat(target);
            entries.add(new BurstEntry(new WeakReference<>(target), new AtomicInteger(heat * 2), attacker.getMoveUsage()));
            HeatTrapManager.clearHeat(target);

            JCraft.createParticle((ServerLevel) attacker.level(),
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), JParticleType.BOOM);
        }
        ACTIVE_BURSTS.put(attacker.getUUID(), entries);
        JUtils.serverPlaySound(JSoundRegistry.KQ_EXPLODE.get(), (ServerLevel) attacker.level(), user.position(), 96);

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

            int hits = entry.hitsRemaining().getAndSet(0);
            if (hits <= 0) return true;

            Vec3 kbVec = target.position().subtract(user.position()).normalize().scale(0.1);
            for (int i = 0; i < hits; i++) {
                target.invulnerableTime = 0;
                damageLogic(level, target, new AttackData(
                        kbVec, 10, StunType.UNBURSTABLE.ordinal(), true,
                        BURST_DAMAGE, false, 0, damageSource, user,
                        CommonHitPropertyComponent.HitAnimation.MID, entry.moveUsage(), false, true));
            }
            target.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 80, 0, false, true));
            return true;
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
