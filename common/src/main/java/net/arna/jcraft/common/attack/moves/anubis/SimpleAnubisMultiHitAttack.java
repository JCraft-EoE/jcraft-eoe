package net.arna.jcraft.common.attack.moves.anubis;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMultiHitAttack;
import net.arna.jcraft.common.spec.AnubisSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

@Getter
public class SimpleAnubisMultiHitAttack extends AbstractMultiHitAttack<SimpleAnubisMultiHitAttack, AnubisSpec> {
    private final boolean unsheatheSweep;

    public SimpleAnubisMultiHitAttack(int cooldown, int duration, float moveDistance, float damage, int stun,
                                      float hitboxSize, float knockback, float offset, @NonNull IntCollection hitMoments,
                                      boolean unsheatheSweep) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
        this.unsheatheSweep = unsheatheSweep;
    }

    @Override
    public @NonNull MoveType<SimpleAnubisMultiHitAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean conditionsMet(AnubisSpec attacker) {
        return super.conditionsMet(attacker) && (unsheatheSweep ? attacker.isHoldingSheathedAnubis() : attacker.isHoldingAnubis());
    }

    @Override
    public @NonNull Set<LivingEntity> perform(AnubisSpec attacker, LivingEntity user) {
        Set<LivingEntity> targets = super.perform(attacker, user);
        attacker.tryIncrementBloodlust(targets);

        if (!unsheatheSweep || getBlow(attacker) != 1) return targets;

        attacker.unsheatheAttack(targets);
        targets.forEach(target -> {
            if (!JUtils.isBlocking(target)) {
                target.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), 35, 0));
            }
        });

        return targets;
    }

    @Override
    protected @NonNull SimpleAnubisMultiHitAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SimpleAnubisMultiHitAttack copy() {
        return copyExtras(new SimpleAnubisMultiHitAttack(getCooldown(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), getHitMoments(), unsheatheSweep));
    }

    public static class Type extends AbstractMultiHitAttack.Type<SimpleAnubisMultiHitAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SimpleAnubisMultiHitAttack>, SimpleAnubisMultiHitAttack> buildCodec(RecordCodecBuilder.Instance<SimpleAnubisMultiHitAttack> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), duration(), moveDistance(), damage(), stun(),
                    hitboxSize(), knockback(), offset(), hitMoments(),
                    Codec.BOOL.fieldOf("unsheathe_sweep").forGetter(SimpleAnubisMultiHitAttack::isUnsheatheSweep))
                    .apply(instance, applyAttackExtras(SimpleAnubisMultiHitAttack::new));
        }
    }
}
