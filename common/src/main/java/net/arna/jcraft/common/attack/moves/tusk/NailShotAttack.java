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
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.sounds.SoundEvent;
import net.arna.jcraft.common.entity.stand.TuskAct1Entity;
import net.arna.jcraft.common.entity.stand.TuskAct2Entity;
import net.arna.jcraft.common.entity.stand.TuskAct3Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
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

        boolean isToenail = maxRange < 20.0f;

        if (attacker instanceof TuskAct1Entity tusk1) {
            nail = isToenail ? NailProjectile.fromTuskAct1Toenail(tusk1, maxRange)
                             : NailProjectile.fromTuskAct1(tusk1, maxRange);
        } else if (attacker instanceof TuskAct2Entity tusk2) {
            // Toenail for Act 2 is free (no nail cost), same as Act 1
            nail = isToenail ? NailProjectile.fromTuskAct1Toenail2(tusk2, maxRange)
                             : NailProjectile.fromTuskAct2(tusk2, maxRange, 0f);
        } else if (attacker instanceof TuskAct3Entity tusk3) {
            nail = NailProjectile.fromTuskAct3(tusk3, maxRange, 0f);
        }

        if (nail == null) return Set.of();

        SoundEvent shotSound = isToenail ? JSoundRegistry.TUSK_CHU.get() : JSoundRegistry.TUSK_SHOT.get();
        attacker.getBaseEntity().playSound(shotSound, 1.0f, 1.0f);

        // Toenail fires from lower position (hip/knee level); regular nail fires from chest
        float heightMult = isToenail ? 0.15f : 0.55f;
        Vec3 spawnPos = user.position().add(0, user.getBbHeight() * heightMult, 0);
        nail.setPos(spawnPos);
        Vec3 target = JUtils.getCrosshairTarget(user, 50.0);
        nail.setDeltaMovement(target.subtract(spawnPos).normalize().scale(baseSpeed));

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