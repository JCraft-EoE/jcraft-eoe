package net.arna.jcraft.common.attack.moves.shared;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.LivingEntity;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Getter
public class HealMove extends AbstractSimpleAttack<HealMove, StandEntity<?, ?>> {
    private final float health;
    private final HealTarget target;
    private final Consumer<LivingEntity> consumer;

    public HealMove(int cooldown, int windup, int duration, float moveDistance, float hitBoxSize, float offset,
                    float health, HealTarget target) {
        this(cooldown, windup, duration, moveDistance, hitBoxSize, offset, health, target, e -> {});
    }

    public HealMove(int cooldown, int windup, int duration, float moveDistance, float hitBoxSize, float offset,
                    float health, HealTarget target, Consumer<LivingEntity> consumer) {
        super(cooldown, windup, duration, moveDistance, 0f, 0, hitBoxSize, 0f, offset);
        this.health = health;
        this.target = target;
        this.consumer = consumer;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(StandEntity<?, ?> stand, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = target.pickTargets(super.perform(stand, user, ctx), user);
        targets.forEach(e -> {
            e.heal(health);
            consumer.accept(e);
        });
        return targets;
    }

    @Override
    protected HealMove getThis() {
        return this;
    }

    @Override
    public HealMove copy() {
        return new HealMove(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getHitBoxSize(), getOffset(),
                health, target, consumer);
    }

    public enum HealTarget {
        TARGETS((targets, user) -> targets),
        USER((targets, user) -> Set.of(user));

        private final BiFunction<Set<LivingEntity>, LivingEntity, Set<LivingEntity>> targetPicker;

        HealTarget(BiFunction<Set<LivingEntity>, LivingEntity, Set<LivingEntity>> targetPicker) {
            this.targetPicker = targetPicker;
        }

        public Set<LivingEntity> pickTargets(Set<LivingEntity> targets, LivingEntity user) {
            return targetPicker.apply(targets, user);
        }
    }
}
