package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class HeatBurstAttack extends AbstractMove<HeatBurstAttack, SpeedKingEntity> {
    private static final double BURST_RANGE = 8.0;
    private static final int DAMAGE_INTERVAL = 2;

    private final List<UUID> burstTargets = new ArrayList<>();

    public HeatBurstAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull MoveType<HeatBurstAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        burstTargets.clear();
        if (attacker.level().isClientSide()) return Set.of();

        List<LivingEntity> targets = attacker.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(user.position().add(-BURST_RANGE, -BURST_RANGE, -BURST_RANGE),
                        user.position().add(BURST_RANGE, BURST_RANGE, BURST_RANGE)),
                e -> e != user && e != attacker && e.isAlive()
                        && HeatTrapManager.getHeat(e) > 0
                        && user.getUUID().equals(HeatTrapManager.getAttackerUUID(e)));

        for (LivingEntity t : targets) burstTargets.add(t.getUUID());
        return new HashSet<>(targets);
    }

    @Override
    public void activeTick(SpeedKingEntity attacker, int moveStun) {
        super.activeTick(attacker, moveStun);
        if (attacker.level().isClientSide() || burstTargets.isEmpty()) return;

        if (moveStun == 0) {
            for (UUID id : burstTargets) {
                int heat = HeatTrapManager.getHeat(id);
                if (heat == 0) continue;
                attacker.level().getEntitiesOfClass(LivingEntity.class,
                        attacker.getBoundingBox().inflate(BURST_RANGE + 4),
                        e -> e.getUUID().equals(id)).stream().findFirst().ifPresent(target -> {
                    target.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), heat * 80, 0, false, true));
                    HeatTrapManager.clearHeat(target);
                });
            }
            burstTargets.clear();
            return;
        }

        if (moveStun % DAMAGE_INTERVAL != 0) return;

        for (UUID id : burstTargets) {
            int heat = HeatTrapManager.getHeat(id);
            if (heat == 0) continue;
            attacker.level().getEntitiesOfClass(LivingEntity.class,
                    attacker.getBoundingBox().inflate(BURST_RANGE + 4),
                    e -> e.getUUID().equals(id)).stream().findFirst().ifPresent(target -> {
                if (target.isAlive()) {
                    target.hurt(target.damageSources().magic(), 0.5f * (heat / 5.0f));
                }
            });
        }
    }

    @Override
    protected @NonNull HeatBurstAttack getThis() { return this; }

    @Override
    public @NonNull HeatBurstAttack copy() {
        return copyExtras(new HeatBurstAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<HeatBurstAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<HeatBurstAttack>, HeatBurstAttack> buildCodec(RecordCodecBuilder.Instance<HeatBurstAttack> instance) {
            return baseDefault(instance, HeatBurstAttack::new);
        }
    }
}
