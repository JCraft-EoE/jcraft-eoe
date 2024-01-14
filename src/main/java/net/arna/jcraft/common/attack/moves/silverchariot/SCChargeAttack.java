package net.arna.jcraft.common.attack.moves.silverchariot;

import net.arna.jcraft.common.attack.core.ctx.FloatMoveVariable;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractChargeAttack;
import net.arna.jcraft.common.entity.stand.SilverChariotEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class SCChargeAttack extends AbstractChargeAttack<SCChargeAttack, SilverChariotEntity, SilverChariotEntity.State> {
    public static final FloatMoveVariable LOOK_DIR_Y = new FloatMoveVariable();

    public SCChargeAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                          float hitboxSize, float knockback, float offset, SilverChariotEntity.State hitAnimState) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitAnimState);
    }

    @Override
    public void onInitiate(SilverChariotEntity attacker) {
        super.onInitiate(attacker);
        attacker.getMoveContext().setFloat(LOOK_DIR_Y, (float) attacker.getUserOrThrow().getRotationVector().y);
    }

    @Override
    protected Vec3d advanceChargePos(StandEntity<?, ?> attacker, float moveDistance, int windupPoint) {
        return attacker.getPos().add(
                getRotVec(attacker).add(0, attacker.getMoveContext().getFloat(LOOK_DIR_Y), 0)
                        .multiply(moveDistance / windupPoint)
        );
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
