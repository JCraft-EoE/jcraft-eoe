package net.arna.jcraft.common.attack.moves.magiciansred;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.AnkhProjectile;
import net.minecraft.world.entity.LivingEntity;
import java.util.Set;

public final class CrossfireVariationAttack<A extends IAttacker<? extends A, ?>> extends AbstractMove<CrossfireVariationAttack<A>, A> {
    private static final int variationAnkhs = 6;

    public CrossfireVariationAttack(final int cooldown, final int windup, final int moveStunTicks, final float moveDistance) {
        super(cooldown, windup, moveStunTicks, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull MoveType<CrossfireVariationAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        int orbitRange = user.isShiftKeyDown() ? 7 : 5;
        for (int i = 0; i < variationAnkhs; i++) {
            final AnkhProjectile ankh = new AnkhProjectile(baseEntity.level(), user);
            ankh.setDeltaMovement(0.0, 1.0, 0.0);
            ankh.setPos(getOffsetHeightPos(attacker).add(0.0, 1.0, 0.0));
            ankh.setVariation(true);
            ankh.setOrbitRange(orbitRange);
            ankh.setOrbitOffset((360f / variationAnkhs) * i);
            baseEntity.level().addFreshEntity(ankh);
        }

        return Set.of();
    }

    @Override
    protected @NonNull CrossfireVariationAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull CrossfireVariationAttack<A> copy() {
        return copyExtras(new CrossfireVariationAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<CrossfireVariationAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<CrossfireVariationAttack<?>>, CrossfireVariationAttack<?>> buildCodec(RecordCodecBuilder.Instance<CrossfireVariationAttack<?>> instance) {
            return baseDefault(instance, CrossfireVariationAttack::new);
        }
    }
}
