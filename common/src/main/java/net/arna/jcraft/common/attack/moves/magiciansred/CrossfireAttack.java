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

public final class CrossfireAttack<A extends IAttacker<? extends A, ?>> extends AbstractMove<CrossfireAttack<A>, A> {
    public CrossfireAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull MoveType<CrossfireAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        for (int i = 0; i < 3; i++) {
            final AnkhProjectile ankh = new AnkhProjectile(baseEntity.level(), user);
            ankh.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1F, 5F);
            ankh.setPos(getOffsetHeightPos(attacker));
            baseEntity.level().addFreshEntity(ankh);
        }

        return Set.of();
    }

    @Override
    protected @NonNull CrossfireAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull CrossfireAttack<A> copy() {
        return copyExtras(new CrossfireAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<CrossfireAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<CrossfireAttack<?>>, CrossfireAttack<?>> buildCodec(RecordCodecBuilder.Instance<CrossfireAttack<?>> instance) {
            return baseDefault(instance, CrossfireAttack::new);
        }
    }
}
