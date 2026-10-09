package net.arna.jcraft.common.attack.moves.goldexperience;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class OverclockAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<OverclockAttack<A>, A> {
    @Getter
    int effectDuration;

    public OverclockAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                           final float damage, final int stun, final float hitboxSize, final float knockback,
                           final float offset, final int effectDuration) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.effectDuration = effectDuration;
    }

    @Override
    public @NotNull MoveType<OverclockAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        final var level = attacker.getEntityWorld();

        for (LivingEntity target : targets) {
            if (!getBlockableType().isNonBlockableEffects())
                if (JUtils.isBlocking(target)) continue;

            target.addEffect(new MobEffectInstance(JStatusRegistry.OUT_OF_BODY.get(), 60, 0, false, true));

            if (getDamage() <= 0) {
                final var aabb = target.getBoundingBox();
                final var pos = aabb.getCenter();

                final var packet = new ClientboundLevelParticlesPacket(
                        ParticleTypes.HAPPY_VILLAGER,
                        false,
                        pos.x,
                        pos.y,
                        pos.z,
                        (float)aabb.getXsize(),
                        (float)aabb.getYsize(),
                        (float)aabb.getZsize(),
                        0.1f,
                        32
                );

                for (var p : JUtils.around((ServerLevel) level, target.position(), 256)) {
                    p.connection.send(packet);
                }
            }
        }

        return targets;
    }

    @Override
    protected @NonNull OverclockAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull OverclockAttack<A> copy() {
        return copyExtras(new OverclockAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getEffectDuration()));
    }

    public static class Type extends AbstractSimpleAttack.Type<OverclockAttack<?>> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<OverclockAttack<?>, Integer> effectDuration() {
            return Codec.INT.fieldOf("effectDuration").forGetter(OverclockAttack::getEffectDuration);
        }

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<OverclockAttack<?>>, OverclockAttack<?>> buildCodec(RecordCodecBuilder.Instance<OverclockAttack<?>> instance) {
            return instance.group(extras(), attackExtras(),
                    cooldown(), windup(), duration(), moveDistance(),
                    damage(), stun(), hitboxSize(), knockback(),
                    offset(), effectDuration()
            ).apply(instance, applyAttackExtras(OverclockAttack::new));
        }
    }
}
