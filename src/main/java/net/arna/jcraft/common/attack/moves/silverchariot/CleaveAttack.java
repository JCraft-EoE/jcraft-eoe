package net.arna.jcraft.common.attack.moves.silverchariot;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractChargeAttack;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.entity.LivingEntity;
import org.joml.Vector3f;

public class CleaveAttack extends AbstractSimpleAttack<CleaveAttack, SilverChariotEntity> {
    public CleaveAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                        float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.HIT_SPARK_2;
    }

    @Override
    public void onInitiate(SilverChariotEntity attacker) {
        super.onInitiate(attacker);

        LivingEntity user = attacker.getUserOrThrow();
        AbstractChargeAttack.prepDetachmentMove(attacker, user);
        attacker.setFreePos((user.getPos().add(attacker.getUserOrThrow().getRotationVector().multiply(1.5)).toVector3f()));
        attacker.setFree(true);
    }

    @Override
    protected @NonNull CleaveAttack getThis() {
        return this;
    }

    @Override
    public @NonNull CleaveAttack copy() {
        return copyExtras(new CleaveAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }
}
