package net.arna.jcraft.common.attack.moves.thehand;

import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.enums.BlockableType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import static net.arna.jcraft.api.Attacks.damageLogic;
import static net.arna.jcraft.api.Attacks.trueDamage;

@Getter
public abstract class AbstractEraseAttack<T extends AbstractEraseAttack<T, A>, A extends StandEntity<? extends A, ?>> extends AbstractSimpleAttack<T, A> {
    public AbstractEraseAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                               final float damage, final int stun, final float hitboxSize, final float knockback,
                               final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        withBlockableType(BlockableType.NON_BLOCKABLE);
        withHitSpark(JParticleType.INVERTED_HIT_SPARK_3);
    }

    @Override
    protected void processTarget(final A attacker, final LivingEntity target, final Vec3 kbVec, final DamageSource damageSource) {
        damageLogic(attacker.getEntityWorld(), target, kbVec, getStun(), getStunType().ordinal(), true,
                0, isLift(), getBlockStun(), damageSource, attacker.getUserOrThrow(), getHitAnimation(), true, true);

        target.removeEffect(JStatusRegistry.DAZED.get());
        StandEntity<?, ?> stand = JUtils.getStand(target);
        if (stand != null) stand.blocking = false;
        JCraft.stun(target, getStun(), 0, attacker);
        trueDamage(getDamage(), JDamageSources.stand(attacker), target);
    }
}
