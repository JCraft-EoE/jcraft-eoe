package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractHoldableMove;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import net.arna.jcraft.common.entity.stand.TuskAct2Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class PerfectGoldenRotationAttack extends AbstractHoldableMove<PerfectGoldenRotationAttack, TuskAct2Entity> {
    @Getter
    private final float baseSpeed;
    @Getter
    private final float maxRange;

    public PerfectGoldenRotationAttack(int cooldown, int windup, int duration, float moveDistance, float baseSpeed, float maxRange) {
        super(cooldown, windup, duration, moveDistance, 0); // Can fire immediately
        this.baseSpeed = baseSpeed;
        this.maxRange = maxRange;
        ranged = true;
    }

    @Override
    public @NotNull MoveType<PerfectGoldenRotationAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TuskAct2Entity attacker, LivingEntity user) {
        // Called by followup after charging
        int chargeTime = getChargeTime();

        // Drain nails based on charge (1-3 nails)
        float nailCost = 1.0f + Math.min(chargeTime / 50.0f, 2.0f);

        NailProjectile nail = NailProjectile.fromTuskAct2Perfect(attacker, nailCost, chargeTime, maxRange);
        if (nail == null) return Set.of();

        attacker.playSound(JSoundRegistry.TUSK_HEAVY_SHOT.get(), 1.0f, 1.0f);
        // Fire from player position at eye level
        nail.setPos(user.position().add(0, user.getBbHeight() * 0.75, 0));

        // Speed increases with charge: 1.0x to 2.0x
        float speedMultiplier = 1.0f + (chargeTime / 100.0f);
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
        PerfectGoldenRotationAttack copy = new PerfectGoldenRotationAttack(
                getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                baseSpeed, maxRange
        );
        if (setMoveStun) {
            copy.shouldSetMoveStun();
        }
        return copyExtras(copy);
    }

    public static class Type extends AbstractHoldableMove.Type<PerfectGoldenRotationAttack> {
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
                    minimumCharge(),
                    setMoveStun(),
                    Codec.FLOAT.fieldOf("base_speed").forGetter(PerfectGoldenRotationAttack::getBaseSpeed),
                    Codec.FLOAT.fieldOf("max_range").forGetter(PerfectGoldenRotationAttack::getMaxRange)
            ).apply(instance, applyExtras((cooldown, windup, duration, moveDistance, minimumCharge, setMoveStun, baseSpeed, maxRange) -> {
                PerfectGoldenRotationAttack attack = new PerfectGoldenRotationAttack(
                        cooldown, windup, duration, moveDistance, baseSpeed, maxRange
                );
                if (setMoveStun) {
                    attack.shouldSetMoveStun();
                }
                return attack;
            }));
        }
    }
}