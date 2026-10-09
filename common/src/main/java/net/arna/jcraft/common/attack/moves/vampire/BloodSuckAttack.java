package net.arna.jcraft.common.attack.moves.vampire;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.attack.moves.AbstractSpecGrabAttack;
import net.arna.jcraft.api.registry.JStatRegistry;
import net.arna.jcraft.common.spec.VampireSpec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.Set;

public final class BloodSuckAttack extends AbstractSpecGrabAttack<BloodSuckAttack, VampireSpec, VampireSpec.State> {
    @Getter
    private final boolean alternative;

    public BloodSuckAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                           final float damage, final int stun, final float hitboxSize, final float knockback,
                           final float offset, final AbstractMove<?, ? super VampireSpec> hitMove,
                           final int grabDuration, final double grabOffset, final boolean alternative) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback,
                offset, hitMove, alternative ? VampireSpec.State.FEEDING_BLOODSUCK_HIT : VampireSpec.State.BLOODSUCK_HIT,
                grabDuration, grabOffset);
        this.alternative = alternative;
    }

    @Override
    public @NotNull MoveType<BloodSuckAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public void performHook(final VampireSpec attacker, final Set<LivingEntity> targets, final Set<AABB> boxes,
                            final DamageSource damageSource, final Vec3 forwardPos, final Vec3 rotationVector) {
        if (!targets.isEmpty()) {
            if (getHitMove() instanceof BloodSuckHitsAttack bloodSuckHitsAttack) {
                bloodSuckHitsAttack.setTarget(new WeakReference<>(targets.stream().findFirst().get()));
            }

            if (attacker != null && attacker.getUser() instanceof final Player player && !player.level().isClientSide()) {
                player.awardStat(JStatRegistry.BLOOD_SUCKED.get());
            }
        }
    }

    @Override
    protected @NonNull BloodSuckAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BloodSuckAttack copy() {
        return copyExtras(new BloodSuckAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getHitMove(), getGrabDuration(), getGrabOffset(), isAlternative()));
    }

    public static class Type extends AbstractSpecGrabAttack.Type<BloodSuckAttack> {
        public static final Type INSTANCE = new Type();

        @SuppressWarnings("unchecked")
        protected RecordCodecBuilder<BloodSuckAttack, AbstractMove<?, VampireSpec>> hitMove() {
            return JRegistries.MOVE_CODEC
                    .fieldOf("hit_move")
                    .<AbstractMove<?, VampireSpec>>xmap(m -> (AbstractMove<?, VampireSpec>) m, m -> m)
                    .forGetter(m -> (AbstractMove<?, VampireSpec>) m.getHitMove());
        }

        protected RecordCodecBuilder<BloodSuckAttack, Boolean> alternative() {
            return Codec.BOOL
                    .fieldOf("is_alternative")
                    .forGetter(BloodSuckAttack::isAlternative);
        }

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<BloodSuckAttack>, BloodSuckAttack>
        buildCodec(RecordCodecBuilder.Instance<BloodSuckAttack> instance) {
            return instance.group(
                    extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(),
                    damage(), stun(), hitboxSize(), knockback(),
                    offset(), hitMove(),
                    grabDuration(), grabOffset(), alternative()
            ).apply(instance, applyAttackExtras(BloodSuckAttack::new));
        }
    }
}
