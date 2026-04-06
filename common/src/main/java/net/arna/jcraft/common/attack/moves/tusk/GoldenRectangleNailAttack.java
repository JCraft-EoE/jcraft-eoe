package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import net.arna.jcraft.common.entity.stand.TuskAct2Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class GoldenRectangleNailAttack extends AbstractMove<GoldenRectangleNailAttack, TuskAct2Entity> {
    @Getter
    private final float baseSpeed;
    @Getter
    private final float maxRange;
    @Getter
    private final float creepDistance;

    public GoldenRectangleNailAttack(int cooldown, int windup, int duration, float moveDistance, float baseSpeed, float maxRange, float creepDistance) {
        super(cooldown, windup, duration, moveDistance);
        this.baseSpeed = baseSpeed;
        this.maxRange = maxRange;
        this.creepDistance = creepDistance;
        ranged = true;
    }

    @Override
    public @NotNull MoveType<GoldenRectangleNailAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TuskAct2Entity attacker, LivingEntity user) {
        NailProjectile nail = NailProjectile.fromTuskAct2(attacker, maxRange, creepDistance);
        if (nail == null) return Set.of();

        // Position at stand height
        nail.setPos(attacker.position().add(0, attacker.getBbHeight() * 0.75, 0));
        nail.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, baseSpeed, 1.0F);

        attacker.level().addFreshEntity(nail);
        return Set.of();
    }

    @Override
    protected @NonNull GoldenRectangleNailAttack getThis() {
        return this;
    }

    @Override
    public @NonNull GoldenRectangleNailAttack copy() {
        return copyExtras(new GoldenRectangleNailAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), baseSpeed, maxRange, creepDistance));
    }

    public static class Type extends AbstractMove.Type<GoldenRectangleNailAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<GoldenRectangleNailAttack>, GoldenRectangleNailAttack>
        buildCodec(RecordCodecBuilder.Instance<GoldenRectangleNailAttack> instance) {
            return instance.group(
                    extras(),
                    cooldown(),
                    windup(),
                    duration(),
                    moveDistance(),
                    Codec.FLOAT.fieldOf("base_speed").forGetter(GoldenRectangleNailAttack::getBaseSpeed),
                    Codec.FLOAT.fieldOf("max_range").forGetter(GoldenRectangleNailAttack::getMaxRange),
                    Codec.FLOAT.fieldOf("creep_distance").forGetter(GoldenRectangleNailAttack::getCreepDistance)
            ).apply(instance, applyExtras((cooldown, windup, duration, moveDistance, baseSpeed, maxRange, creepDistance) ->
                    new GoldenRectangleNailAttack(cooldown, windup, duration, moveDistance, baseSpeed, maxRange, creepDistance)));
        }
    }
}