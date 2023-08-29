package net.arna.jcraft.common.attack.moves.silverchariot;

import net.arna.jcraft.common.attack.core.ctx.FloatMoveVariable;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractChargeAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.NotNull;

public class SCChargeAttack extends AbstractChargeAttack<SCChargeAttack, SilverChariotEntity, SilverChariotEntity.State> {
    public static final FloatMoveVariable LOOK_DIR_Y = new FloatMoveVariable();

    public SCChargeAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                          float hitboxSize, float knockback, float offset, SilverChariotEntity.State hitAnimState) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitAnimState);
    }

    @Override
    public void onInitialize(SilverChariotEntity attacker) {
        super.onInitialize(attacker);

        LivingEntity user = attacker.getUserOrThrow();
        float lookDirY = (float) user.getRotationVector().y;
        lookDirY *= MathHelper.abs(lookDirY);
        attacker.getMoveContext().setFloat(LOOK_DIR_Y, lookDirY);
    }

    @Override
    public void tick(SilverChariotEntity attacker) {
        super.tick(attacker);

        Vec3f chargePos = attacker.getFreePos();
        chargePos.add(0, attacker.getMoveContext().getFloat(LOOK_DIR_Y), 0);
        attacker.setFreePos(chargePos);
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(LOOK_DIR_Y);
    }

    @NotNull
    @Override
    protected SCChargeAttack getThis() {
        return this;
    }

    @NotNull
    @Override
    public SCChargeAttack copy() {
        return copyExtras(new SCChargeAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getHitAnimState()));
    }
}
