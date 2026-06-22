package net.arna.jcraft.common.attack.moves.purplehaze;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.AbstractPurpleHazeEntity;
import net.arna.jcraft.common.util.JUtils;

public class PHRekkaAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<PHRekkaAttack<A>, A> {
    public PHRekkaAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun, float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull MoveType<PHRekkaAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void onInitiate(A attacker) {
        super.onInitiate(attacker);

        if (attacker.isRemote()) {
            return;
        }
        JUtils.addVelocity(attacker.getUser(), attacker.getBaseEntity().getLookAngle().scale(0.6));
    }

    @Override
    protected @NonNull PHRekkaAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull PHRekkaAttack<A> copy() {
        return copyExtras(new PHRekkaAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<PHRekkaAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<PHRekkaAttack<?>>, PHRekkaAttack<?>> buildCodec(RecordCodecBuilder.Instance<PHRekkaAttack<?>> instance) {
            return attackDefault(instance, PHRekkaAttack::new);
        }
    }
}
