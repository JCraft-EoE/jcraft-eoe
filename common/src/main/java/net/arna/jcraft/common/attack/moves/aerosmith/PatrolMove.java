package net.arna.jcraft.common.attack.moves.aerosmith;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

@Getter
public class PatrolMove<A extends StandEntity<? extends A, ?>> extends AbstractMove<PatrolMove<A>, A> {

    private float range;
    private float radius;
    private float speed;

    public PatrolMove(final int cooldown, final int windup, final int duration, final float moveDistance, final float range, final float radius, final float speed) {
        super(cooldown, windup, duration, moveDistance);

        withRange(range);
        withRadius(radius);
        withSpeed(speed);
    }

    public PatrolMove<A> withRange(final float range) {
        this.range = range;
        return getThis();
    }

    public PatrolMove<A> withRadius(final float radius) {
        this.radius = radius;
        return getThis();
    }

    public PatrolMove<A> withSpeed(final float speed) {
        this.speed = speed;
        return getThis();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        // TODO implement
        attacker.setFree(true);
        return Set.of();
    }

    @Override
    public void tick(final A attacker) {
        // TODO implement
    }

    @Override
    public @NonNull MoveType<PatrolMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull PatrolMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull PatrolMove<A> copy() {
        return copyExtras(new PatrolMove<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getRange(), getRadius(), getSpeed()));
    }

    public static class Type extends AbstractMove.Type<PatrolMove<?>> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<PatrolMove<?>, Float> range() {
            return Codec.FLOAT.fieldOf("range").forGetter(PatrolMove::getRange);
        }
        protected RecordCodecBuilder<PatrolMove<?>, Float> radius() {
            return Codec.FLOAT.fieldOf("radius").forGetter(PatrolMove::getRadius);
        }
        protected RecordCodecBuilder<PatrolMove<?>, Float> speed() {
            return Codec.FLOAT.fieldOf("speed").forGetter(PatrolMove::getSpeed);
        }

        protected Products.P8<RecordCodecBuilder.Mu<PatrolMove<?>>, BaseMoveExtras, Integer, Integer, Integer, Float, Float, Float, Float>
        bombDefault(RecordCodecBuilder.Instance<PatrolMove<?>> instance) {
            return instance.group(extras(), cooldown(), windup(), duration(), moveDistance(), range(), radius(), speed());
        }

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<PatrolMove<?>>, PatrolMove<?>> buildCodec(final RecordCodecBuilder.Instance<PatrolMove<?>> instance) {
            return bombDefault(instance).apply(instance, applyExtras(PatrolMove::new));
        }
    }
}
