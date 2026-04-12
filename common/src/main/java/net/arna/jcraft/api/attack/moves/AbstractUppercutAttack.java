package net.arna.jcraft.api.attack.moves;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Function10;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.data.AttackMoveExtras;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

@Getter
public abstract class AbstractUppercutAttack<T extends AbstractUppercutAttack<T, A>, A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<T, A> {
    private final float strength;

    protected AbstractUppercutAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                                  final float hitboxSize, final float knockback, final float offset, final float strength) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.strength = strength;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);
        final Vec3 upDir = new Vec3(GravityChangerAPI.getGravityDirection(user).step()).scale(-strength);

        for (LivingEntity target : targets) {
            JUtils.addVelocity(target, upDir);
        }

        return targets;
    }

    public abstract static class Type<M extends AbstractUppercutAttack<? extends M, ?>> extends AbstractSimpleAttack.Type<M> {
        protected RecordCodecBuilder<M, Float> strength() {
            return Codec.FLOAT.fieldOf("strength").forGetter(AbstractUppercutAttack::getStrength);
        }

        /**
         * Creates the default uppercut codec base.
         * Can be used as a base to extend upon or as a standalone codec.
         * (see {@link #uppercutDefault(RecordCodecBuilder.Instance, Function10)}).
         *
         * @param instance The instance to create the codec with
         * @return The default uppercut codec base
         */
        protected Products.P12<RecordCodecBuilder.Mu<M>, BaseMoveExtras, AttackMoveExtras, Integer, Integer, Integer, Float, Float, Integer, Float, Float, Float, Float>
        uppercutDefault(RecordCodecBuilder.Instance<M> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(), damage(),
                    stun(), hitboxSize(), knockback(), offset(), strength());
        }

        /**
         * Creates the default uppercut codec.
         * Can be used directly as a return value of {@link #buildCodec(RecordCodecBuilder.Instance)}
         * @param instance The instance to create the codec with
         * @param function The constructor function used to create a new instance of the move
         * @return The default uppercut codec
         */
        protected App<RecordCodecBuilder.Mu<M>, M> uppercutDefault(RecordCodecBuilder.Instance<M> instance, Function10<Integer,
                Integer, Integer, Float, Float, Integer, Float, Float, Float, Float, M> function) {
            return uppercutDefault(instance).apply(instance, applyAttackExtras(function));
        }
    }
}
