package net.arna.jcraft.common.attack.moves.spin;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class ObserveNatureMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<ObserveNatureMove<A>, A> {

    public ObserveNatureMove(final int cooldown, final int windup, final int duration) {
        super(cooldown, windup, duration, 0);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final Vec3 eyePos = baseEntity.position().add(GravityChangerAPI.getEyeOffset(baseEntity));
        final HitResult hitResult = JUtils.raycastAll(baseEntity,
                eyePos, eyePos.add(baseEntity.getLookAngle().scale(20 /* blocks */)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_SPECTATORS));
        if (user != null) {
            JComponentPlatformUtils.getSpin(user).observeNature(hitResult, user.level());
        }
        return Set.of();
    }

    @Override
    protected @NonNull ObserveNatureMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull ObserveNatureMove<A> copy() {
        return copyExtras(new ObserveNatureMove<>(getCooldown(), getWindup(), getDuration()));
    }

    @Override
    public @NonNull MoveType<ObserveNatureMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    public static class Type extends AbstractMove.Type<ObserveNatureMove<?>> {

        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ObserveNatureMove<?>>, ObserveNatureMove<?>> buildCodec(final RecordCodecBuilder.Instance<ObserveNatureMove<?>> instance) {
            return instance.group(extras(), cooldown(), windup(), duration()).apply(instance, applyExtras(ObserveNatureMove::new));
        }

    }

}
