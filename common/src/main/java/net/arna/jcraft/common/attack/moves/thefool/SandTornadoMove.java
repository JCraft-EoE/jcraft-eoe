package net.arna.jcraft.common.attack.moves.thefool;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.SandTornadoEntity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public final class SandTornadoMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<SandTornadoMove<A>, A> {
    public SandTornadoMove(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull MoveType<SandTornadoMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final SandTornadoEntity sandTornado = new SandTornadoEntity(baseEntity.level());
        sandTornado.setMaster(user);
        sandTornado.moveTo(baseEntity.getX(), baseEntity.getY() + 1.5, baseEntity.getZ(), baseEntity.getYRot(), baseEntity.getXRot());
        baseEntity.level().addFreshEntity(sandTornado);

        return Set.of();
    }

    @Override
    protected @NonNull SandTornadoMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull SandTornadoMove<A> copy() {
        return copyExtras(new SandTornadoMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<SandTornadoMove<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SandTornadoMove<?>>, SandTornadoMove<?>> buildCodec(RecordCodecBuilder.Instance<SandTornadoMove<?>> instance) {
            return baseDefault(instance, SandTornadoMove::new);
        }
    }
}
