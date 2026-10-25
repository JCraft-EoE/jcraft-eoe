package net.arna.jcraft.common.attack.moves.purplehaze;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.moves.AbstractUppercutAttack;
import net.arna.jcraft.common.entity.stand.AbstractPurpleHazeEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class BackhandAttack<A extends IAttacker<? extends A, ?>> extends AbstractUppercutAttack<BackhandAttack<A>, A> {
    public BackhandAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                          float hitboxSize, float knockback, float offset, float strength) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, strength);
    }

    @Override
    public @NonNull MoveType<BackhandAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected void processTarget(A attacker, LivingEntity target, Vec3 kbVec, DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);
        boolean stillBlocking = JUtils.isBlocking(target);
        if (!stillBlocking) {
            AbstractPurpleHazeEntity.infect(target, 3 * 20, attacker.getUser());
        }
    }

    @Override
    protected @NonNull BackhandAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull BackhandAttack<A> copy() {
        return copyExtras(new BackhandAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), getStrength()));
    }

    public static class Type extends AbstractUppercutAttack.Type<BackhandAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<BackhandAttack<?>>, BackhandAttack<?>> buildCodec(RecordCodecBuilder.Instance<BackhandAttack<?>> instance) {
            return uppercutDefault(instance, BackhandAttack::new);
        }
    }
}
