package net.arna.jcraft.common.attack.moves.killerqueen;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.arna.jcraft.common.entity.stand.KillerQueenEntity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class SheerHeartAttackAttack<A extends IAttacker<? extends A, ?>> extends AbstractMove<SheerHeartAttackAttack<A>, A> {
    public SheerHeartAttackAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NotNull MoveType<SheerHeartAttackAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final SheerHeartAttackEntity sha = new SheerHeartAttackEntity(baseEntity.level());
        sha.setMaster(user);
        sha.moveTo(baseEntity.getX(), baseEntity.getY() + 0.5, baseEntity.getZ(), baseEntity.getYRot(), baseEntity.getXRot());
        baseEntity.level().addFreshEntity(sha);

        return Set.of();
    }

    @Override
    protected @NonNull SheerHeartAttackAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull SheerHeartAttackAttack<A> copy() {
        return copyExtras(new SheerHeartAttackAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<SheerHeartAttackAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SheerHeartAttackAttack<?>>, SheerHeartAttackAttack<?>> buildCodec(RecordCodecBuilder.Instance<SheerHeartAttackAttack<?>> instance) {
            return baseDefault(instance, SheerHeartAttackAttack::new);
        }
    }
}
