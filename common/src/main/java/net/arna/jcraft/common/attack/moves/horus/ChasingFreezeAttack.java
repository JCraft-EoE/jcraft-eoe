package net.arna.jcraft.common.attack.moves.horus;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.IceBranchProjectile;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public class ChasingFreezeAttack<A extends IAttacker<? extends A, ?>> extends AbstractMove<ChasingFreezeAttack<A>, A> {
    public ChasingFreezeAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull MoveType<ChasingFreezeAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final IceBranchProjectile iceBranchProjectile = new IceBranchProjectile(baseEntity.level(), user, 0);
        iceBranchProjectile.moveTo(baseEntity.getX(), baseEntity.getY(), baseEntity.getZ(), -baseEntity.getYRot() + 180, -baseEntity.getXRot());
        baseEntity.level().addFreshEntity(iceBranchProjectile);

        return Set.of();
    }

    @Override
    protected @NonNull ChasingFreezeAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull ChasingFreezeAttack<A> copy() {
        return copyExtras(new ChasingFreezeAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<ChasingFreezeAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ChasingFreezeAttack<?>>, ChasingFreezeAttack<?>> buildCodec(RecordCodecBuilder.Instance<ChasingFreezeAttack<?>> instance) {
            return baseDefault(instance, ChasingFreezeAttack::new);
        }
    }
}
