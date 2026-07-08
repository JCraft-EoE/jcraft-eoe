package net.arna.jcraft.common.attack.moves.starplatinum.theworld;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.attack.moves.shared.TimeSkipMove;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TimeStrikeAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<TimeStrikeAttack<A>, A> {
    private boolean turnAround = false;

    public TimeStrikeAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                            float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull MoveType<TimeStrikeAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void onInitiate(A attacker) {
        super.onInitiate(attacker);

        LivingEntity user = attacker.getUser();
        if (user == null) return;

        turnAround = user.isShiftKeyDown();
    }

    @Override
    public void activeTick(A attacker, int moveStun) {
        super.activeTick(attacker, moveStun);

        if (moveStun == 7) {
            final LivingEntity user = attacker.getUserOrThrow();
            final Vec3 prevPos = user.getEyePosition();

            TimeSkipMove.doTimeSkip(attacker, user, 2.5, List.of(JSoundRegistry.STAR_PLATINUM_TIMESKIP.get()));
            if (turnAround) {
                user.lookAt(EntityAnchorArgument.Anchor.EYES, prevPos);
            }
        }
    }

    @Override
    protected @NonNull TimeStrikeAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TimeStrikeAttack<A> copy() {
        return copyExtras(new TimeStrikeAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<TimeStrikeAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<TimeStrikeAttack<?>>, TimeStrikeAttack<?>> buildCodec(RecordCodecBuilder.Instance<TimeStrikeAttack<?>> instance) {
            return attackDefault(instance, TimeStrikeAttack::new);
        }
    }
}
