package net.arna.jcraft.common.attack.moves.metallica;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.MoveSelectionResult;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.MetallicaEntity;
import net.arna.jcraft.common.tickable.MagneticFields;
import net.arna.jcraft.common.tickable.RazorCoughs;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class RazorCoughAttack extends AbstractMove<RazorCoughAttack, MetallicaEntity> {
    public RazorCoughAttack(int cooldown, int windup, int duration) {
        super(cooldown, windup, duration, 0);
    }

    @Override
    public @NonNull MoveType<RazorCoughAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(MetallicaEntity attacker, LivingEntity user) {
        final Set<Entity> filter = new HashSet<>(2);
        filter.add(user);
        filter.add(attacker);
        if (user.isVehicle()) {
            filter.addAll(user.getPassengers());
        }

        MagneticFields.forAllOfOwner(user, (field) -> {
            Set<LivingEntity> hit = JUtils.generateHitbox(attacker.level(), field.pos, field.getStrength(), filter);
            for (LivingEntity target : hit) {
                if (target instanceof StandEntity<?, ?> stand) {
                    final LivingEntity targetUser = stand.getUser();

                    if (targetUser == null) {
                        continue;
                    } else if (hit.contains(targetUser)) { // handled later in the iteration
                        continue;
                    } else {
                        target = targetUser;
                    }
                }

                RazorCoughs.add(user, target);
                target.playSound(JSoundRegistry.METALLICA_RAZOR_VOMIT_PREPARE.get());

                int amplifier = 0;
                MobEffectInstance effect = target.getEffect(JStatusRegistry.HYPOXIA.get());
                if (effect != null) {
                    amplifier = effect.getAmplifier() + 1;
                }

                target.addEffect(new MobEffectInstance(
                        JStatusRegistry.HYPOXIA.get(),
                        20 * 20,
                        amplifier
                ));

                if (target instanceof Player) field.time = 0;
                else field.time -= MagneticFields.MagneticField.TICKS_TO_LIVE / 3;
            }
        });

        return Set.of();
    }

    @Override
    public MoveSelectionResult specificMoveSelectionCriterion(
            MetallicaEntity attacker, LivingEntity mob, LivingEntity target,
            int stunTicks, int enemyMoveStun, double distance,
            StandEntity<?, ?> enemyStand, AbstractMove<?, ?> enemyAttack) {
        final LivingEntity user = attacker.getUserOrThrow();
        final AtomicBoolean hitFound = new AtomicBoolean(false);
        final Set<Entity> filter = Set.of(attacker, user);

        MagneticFields.forAllOfOwner(user, (field) -> {
            final Set<LivingEntity> hit = JUtils.generateHitboxNoDisplay(attacker.level(), field.pos, field.getStrength(), e -> !filter.contains(e));

            if (!hit.isEmpty()) {
                hitFound.set(true);
                return false;
            }

            return true;
        });

        return hitFound.get() ? MoveSelectionResult.USE : MoveSelectionResult.STOP;
    }

    @Override
    protected @NonNull RazorCoughAttack getThis() {
        return this;
    }

    @Override
    public @NonNull RazorCoughAttack copy() {
        return copyExtras(new RazorCoughAttack(getCooldown(), getWindup(), getDuration()));
    }

    public static class Type extends AbstractMove.Type<RazorCoughAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<RazorCoughAttack>, RazorCoughAttack> buildCodec(RecordCodecBuilder.Instance<RazorCoughAttack> instance) {
            return instance.group(extras(), cooldown(), windup(), duration()).apply(instance, applyExtras(RazorCoughAttack::new));
        }
    }
}
