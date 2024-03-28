package net.arna.jcraft.common.attack.moves.silverchariot;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.common.util.MobilityType;
import net.minecraft.entity.LivingEntity;

public class RayDartAttack extends AbstractSimpleAttack<RayDartAttack, SilverChariotEntity> {
    public RayDartAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                         float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        ranged = true;
        mobilityType = MobilityType.DASH;
    }

    @Override
    public void onInitiate(SilverChariotEntity attacker) {
        super.onInitiate(attacker);

        LivingEntity user = attacker.getUser();
        if (user != null && user.isOnGround()) {
            user.setVelocity(user.getVelocity().add(getRotVec(attacker).multiply(1)));
            user.velocityModified = true;
        }
    }

    @Override
    protected @NonNull RayDartAttack getThis() {
        return this;
    }

    @Override
    public @NonNull RayDartAttack copy() {
        return copyExtras(new RayDartAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
