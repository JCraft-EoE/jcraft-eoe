package net.arna.jcraft.common.attack.moves.goldexperience.requiem;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.GERScorpionEntity;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
public final class LifeBeamAttack<A extends IAttacker<? extends A, ?>> extends AbstractMove<LifeBeamAttack<A>, A> {
    public LifeBeamAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
        ranged = true;
    }

    @Override
    public @NotNull MoveType<LifeBeamAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final GERScorpionEntity scorpion = new GERScorpionEntity(JEntityTypeRegistry.GER_SCORPION.get(), baseEntity.level());
        if (getChargeTime() >= 18) {
            scorpion.charge();
        }
        scorpion.setInitialVel(user.getLookAngle().scale(2));
        final Vec3 ePos = baseEntity.getEyePosition();
        scorpion.moveTo(ePos.x, ePos.y, ePos.z, -user.getYRot() - 90f, baseEntity.getXRot());
        scorpion.setMaster(user);
        baseEntity.level().addFreshEntity(scorpion);

        return Set.of();
    }

    @Override
    protected @NonNull LifeBeamAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull LifeBeamAttack<A> copy() {
        return copyExtras(new LifeBeamAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<LifeBeamAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<LifeBeamAttack<?>>, LifeBeamAttack<?>> buildCodec(RecordCodecBuilder.Instance<LifeBeamAttack<?>> instance) {
            return baseDefault(instance, LifeBeamAttack::new);
        }
    }
}
