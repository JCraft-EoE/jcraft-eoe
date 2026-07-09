package net.arna.jcraft.common.attack.moves.cream;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMultiHitAttack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class CreamComboAttack<A extends IAttacker<? extends A, ?>> extends AbstractMultiHitAttack<CreamComboAttack<A>, A> {
    public CreamComboAttack(final int cooldown, final int duration, final float moveDistance, final float damage, final int stun,
                            final float hitboxSize, final float knockback, final float offset,
                            final @NonNull IntCollection hitMoments) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
    }

    @Override
    public @NotNull MoveType<CreamComboAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        if (getBlow(attacker) == 2) {
            final Vec3 rV = getRotVec(attacker);

            for (LivingEntity target : targets) {
                target.knockback(1, rV.x, rV.z);
                target.hurtMarked = true;
            }
        }

        return targets;
    }

    @Override
    protected @NonNull CreamComboAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull CreamComboAttack<A> copy() {
        return copyExtras(new CreamComboAttack<>(getCooldown(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), getHitMoments()));
    }

    public static class Type extends AbstractMultiHitAttack.Type<CreamComboAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<CreamComboAttack<?>>, CreamComboAttack<?>> buildCodec(RecordCodecBuilder.Instance<CreamComboAttack<?>> instance) {
            return multiHitDefault(instance, CreamComboAttack::new);
        }
    }
}
