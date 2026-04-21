package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import net.arna.jcraft.common.entity.stand.TuskAct2Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class PerfectGoldenRotationAttack extends AbstractMove<PerfectGoldenRotationAttack, TuskAct2Entity> {
    @Getter
    private final float baseSpeed;
    @Getter
    private final float maxRange;

    public PerfectGoldenRotationAttack(int cooldown, int windup, int duration, float moveDistance, float baseSpeed, float maxRange) {
        super(cooldown, windup, duration, moveDistance);
        this.baseSpeed = baseSpeed;
        this.maxRange = maxRange;
        this.copyOnUse = true;
        ranged = true;
    }

    @Override
    public @NotNull MoveType<PerfectGoldenRotationAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TuskAct2Entity attacker, LivingEntity user) {
        int chargeTime = Math.min(getChargeTime(), DrillShotChargeMove.MAX_CHARGE);
        NailProjectile nail = NailProjectile.fromTuskAct2Perfect(attacker, chargeTime, maxRange);
        if (nail == null) return Set.of();

        attacker.playSound(JSoundRegistry.TUSK_HEAVY_SHOT.get(), 1.0f, 1.0f);
        nail.setPos(user.position().add(0, user.getBbHeight() * 0.55, 0));

        float speedMultiplier = 1.0f + (Math.min(chargeTime, 100) / 100.0f);
        nail.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, baseSpeed * speedMultiplier, 0.5F);

        attacker.level().addFreshEntity(nail);
        return Set.of();
    }

    @Override
    protected @NonNull PerfectGoldenRotationAttack getThis() {
        return this;
    }

    @Override
    public @NonNull PerfectGoldenRotationAttack copy() {
        return copyExtras(new PerfectGoldenRotationAttack(
                getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                baseSpeed, maxRange
        ));
    }

    public static class Type extends AbstractMove.Type<PerfectGoldenRotationAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<PerfectGoldenRotationAttack>, PerfectGoldenRotationAttack>
        buildCodec(RecordCodecBuilder.Instance<PerfectGoldenRotationAttack> instance) {
            return instance.group(
                    extras(),
                    cooldown(),
                    windup(),
                    duration(),
                    moveDistance(),
                    Codec.FLOAT.fieldOf("base_speed").forGetter(PerfectGoldenRotationAttack::getBaseSpeed),
                    Codec.FLOAT.fieldOf("max_range").forGetter(PerfectGoldenRotationAttack::getMaxRange)
            ).apply(instance, applyExtras((cooldown, windup, duration, moveDistance, baseSpeed, maxRange) ->
                    new PerfectGoldenRotationAttack(cooldown, windup, duration, moveDistance, baseSpeed, maxRange)
            ));
        }
    }
}
