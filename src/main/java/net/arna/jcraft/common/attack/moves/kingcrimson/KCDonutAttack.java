package net.arna.jcraft.common.attack.moves.kingcrimson;

import lombok.NonNull;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.KingCrimsonEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;

public class KCDonutAttack extends AbstractSimpleAttack<KCDonutAttack, KingCrimsonEntity> {
    public KCDonutAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                         float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    protected void processTarget(KingCrimsonEntity attacker, LivingEntity target, Vec3d kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);
        Vec3d pos = attacker.getPos().add(attacker.getRotationVector().multiply(1.5));
        target.teleport(pos.x, target.getY(), pos.z);
    }

    @Override
    protected @NonNull KCDonutAttack getThis() {
        return this;
    }

    @Override
    public @NonNull KCDonutAttack copy() {
        return copyExtras(new KCDonutAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }
}
