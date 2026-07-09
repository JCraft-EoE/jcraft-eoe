package net.arna.jcraft.common.attack.moves.magiciansred;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.LifeDetectorEntity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public final class LifeDetectorAttack<A extends IAttacker<? extends A, ?>> extends AbstractMove<LifeDetectorAttack<A>, A> {
    public LifeDetectorAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NonNull MoveType<LifeDetectorAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final LifeDetectorEntity lifeDetector = new LifeDetectorEntity(baseEntity.level());
        lifeDetector.setMaster(user);
        lifeDetector.moveTo(baseEntity.getX(), baseEntity.getY() + 1.5, baseEntity.getZ(), baseEntity.getYRot(), baseEntity.getXRot());
        baseEntity.level().addFreshEntity(lifeDetector);

        return Set.of();
    }

    @Override
    protected @NonNull LifeDetectorAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull LifeDetectorAttack<A> copy() {
        return copyExtras(new LifeDetectorAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<LifeDetectorAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<LifeDetectorAttack<?>>, LifeDetectorAttack<?>> buildCodec(RecordCodecBuilder.Instance<LifeDetectorAttack<?>> instance) {
            return baseDefault(instance, LifeDetectorAttack::new);
        }
    }
}
