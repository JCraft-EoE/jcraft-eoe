package net.arna.jcraft.common.attack.moves.silverchariot;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.IntMoveVariable;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class CircleSlashAttack extends AbstractSimpleAttack<CircleSlashAttack, SilverChariotEntity> {
    public static final IntMoveVariable CHARGE_TIME = new IntMoveVariable(); // in half seconds
    private final float originalDamage;

    public CircleSlashAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                             float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        originalDamage = damage;
    }

    @Override
    public void onInitiate(SilverChariotEntity attacker) {
        super.onInitiate(attacker);
        withDamage(originalDamage);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(SilverChariotEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);

        withDamage(originalDamage + attacker.getMoveContext().getInt(CHARGE_TIME) * 0.75f);
        double launchMultiplier = getDamage() / 5; // damage [6.5 to 11]

        for (LivingEntity living : targets) {
            Vec3d launchVec = living.getPos().subtract(user.getPos()).normalize().multiply(launchMultiplier);
            JUtils.addVelocity(living, launchVec.x, launchVec.y + 0.2, launchVec.z);
        }

        return targets;
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(CHARGE_TIME);
    }

    @Override
    protected @NonNull CircleSlashAttack getThis() {
        return this;
    }

    @Override
    public @NonNull CircleSlashAttack copy() {
        return copyExtras(new CircleSlashAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }
}
