package net.arna.jcraft.common.attack.moves.base;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.core.StunType;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.spec.JSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.SpecAnimationState;
import net.minecraft.entity.LivingEntity;

import java.util.Set;

@Getter
public abstract class AbstractSpecGrabAttack<
        T extends AbstractSpecGrabAttack<T, A, S>,
        A extends JSpec<A, S>,
        S extends Enum<S> & SpecAnimationState<A>
        >
        extends AbstractSimpleAttack<T, A> {
    private final AbstractMove<?, ? super A> hitMove;
    private final S hitState;
    private final int grabDuration;
    private final double grabOffset;

    protected AbstractSpecGrabAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun, float hitboxSize,
                                 float knockback, float offset, AbstractMove<?, ? super A> hitMove, S hitState) {
        this(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset, hitMove, hitState, hitMove.getWindup() - 1, 1);
    }

    protected AbstractSpecGrabAttack(int cooldown, int windup, int duration, float attackDistance, float damage, int stun, float hitboxSize,
                                 float knockback, float offset, AbstractMove<?, ? super A> hitMove, S hitState, int grabDuration, double grabOffset) {
        super(cooldown, windup, duration, attackDistance, damage, stun, hitboxSize, knockback, offset);

        grab = true;

        this.hitMove = hitMove;
        this.hitState = hitState;
        this.grabDuration = grabDuration;
        this.grabOffset = grabOffset;
        withHitAnimation(null);

        // Spec grabs cannot be burst out, but CAN be blocked
        withStunType(StunType.UNBURSTABLE);
        withOverrideStun();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        if (targets.isEmpty()) return targets;
        boolean anyHit = false;

        for (LivingEntity target : targets) {
            StandEntity<?, ?> stand = JUtils.getStand(target);
            if (stand != null && stand.blocking) continue;

            anyHit = true;
            JUtils.cancelMoves(target);
            JComponents.getGrab(target).startGrab(attacker.getBaseEntity(), grabDuration, grabOffset);
            JUtils.setVelocity(target, 0, 0, 0);
        }

        if (anyHit) {
            attacker.setMove(hitMove, hitState);
            attacker.setMoveStun(grabDuration);
            attacker.setPlayerAnimation(hitState.getKey(attacker), grabDuration, 1f);
        }

        return targets;
    }
}