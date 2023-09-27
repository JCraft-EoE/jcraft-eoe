package net.arna.jcraft.common.attack.moves.kingcrimson;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractCounterAttack;
import net.arna.jcraft.common.attack.moves.shared.CounterMissMove;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.registry.JSoundRegistry;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

public class EpitaphAttack extends AbstractCounterAttack<EpitaphAttack, KingCrimsonEntity> {
    private final CounterMissMove<KingCrimsonEntity> counterMiss = new CounterMissMove<>(20);

    public EpitaphAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public void whiff(@NonNull KingCrimsonEntity attacker, @NonNull LivingEntity user) {
        attacker.setMove(counterMiss, KingCrimsonEntity.State.COUNTER_MISS);
        StandEntity.stun(user, counterMiss.getDuration(), 0);
        attacker.playSound(JSoundRegistry.KC_RAGE, 1, 1);
    }

    @Override
    public void counter(@NonNull KingCrimsonEntity attacker, Entity countered, DamageSource counteredDamageSource) {
        super.counter(attacker, countered, counteredDamageSource);

        if (countered == null) return;
        LivingEntity user = attacker.getUserOrThrow();
        Vec3d ePos = countered.getPos();
        if (!countered.isInsideWall()) {
            Vec3d uPos = user.getPos();

            countered.teleport(uPos.x, uPos.y, uPos.z);
            user.teleport(ePos.x, ePos.y, ePos.z);
        }

        user.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, countered.getEyePos());

        if (countered instanceof LivingEntity livingEntity) {
            StandEntity.stun(livingEntity, 20, 0);
            JUtils.cancelMoves(livingEntity);
        }

        attacker.getWorld().playSound(null, ePos.x, ePos.y, ePos.z, JSoundRegistry.TE_TP, SoundCategory.PLAYERS, 1f, 1f);
    }

    @Override
    protected @NonNull EpitaphAttack getThis() {
        return this;
    }

    @Override
    public @NonNull EpitaphAttack copy() {
        return copyExtras(new EpitaphAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
