package net.arna.jcraft.common.attack.moves.tusk;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.entity.projectile.NailProjectile;
import net.minecraft.sounds.SoundEvent;
import net.arna.jcraft.common.entity.stand.TuskAct1Entity;
import net.arna.jcraft.common.entity.stand.TuskAct3Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class NailShotAttack<A extends IAttacker<A, ?>> extends AbstractMove<NailShotAttack<A>, A> {
    @Getter
    private final float baseSpeed;
    @Getter
    private final float maxRange;

    public NailShotAttack(int cooldown, int windup, int duration, float moveDistance, float baseSpeed, float maxRange) {
        super(cooldown, windup, duration, moveDistance);
        this.baseSpeed = baseSpeed;
        this.maxRange = maxRange;
        ranged = true;
    }

    @Override
    public @NotNull MoveType<NailShotAttack<A>> getMoveType() {
        return (MoveType<NailShotAttack<A>>) (MoveType<?>) Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        NailProjectile nail = null;

        if (attacker instanceof TuskAct1Entity tusk1) {
            // Check if this is toenail shot based on maxRange (5 blocks or less)
            if (maxRange <= 5.0f) {
                nail = NailProjectile.fromTuskAct1Toenail(tusk1, maxRange);
            } else {
                nail = NailProjectile.fromTuskAct1(tusk1, maxRange);
            }
        } else if (attacker instanceof TuskAct3Entity tusk3) {
            nail = NailProjectile.fromTuskAct3(tusk3, maxRange, 0f);
        }

        if (nail == null) return Set.of();

        SoundEvent shotSound = maxRange <= 5.0f ? JSoundRegistry.TUSK_CHU.get() : JSoundRegistry.TUSK_SHOT.get();
        attacker.getBaseEntity().playSound(shotSound, 1.0f, 1.0f);

        // Fire from player position at eye level
        nail.setPos(user.position().add(0, user.getBbHeight() * 0.75, 0));
        nail.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, baseSpeed, 1.0F);

        attacker.getBaseEntity().level().addFreshEntity(nail);
        return Set.of();
    }

    @Override
    protected @NonNull NailShotAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull NailShotAttack<A> copy() {
        return copyExtras(new NailShotAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), baseSpeed, maxRange));
    }

    public static class Type extends AbstractMove.Type<NailShotAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<NailShotAttack<?>>, NailShotAttack<?>>
        buildCodec(RecordCodecBuilder.Instance<NailShotAttack<?>> instance) {
            return instance.group(
                    extras(),
                    cooldown(),
                    windup(),
                    duration(),
                    moveDistance(),
                    Codec.FLOAT.fieldOf("base_speed").forGetter(NailShotAttack::getBaseSpeed),
                    Codec.FLOAT.fieldOf("max_range").forGetter(NailShotAttack::getMaxRange)
            ).apply(instance, applyExtras((cooldown, windup, duration, moveDistance, baseSpeed, maxRange) ->
                    new NailShotAttack<>(cooldown, windup, duration, moveDistance, baseSpeed, maxRange)));
        }
    }
}