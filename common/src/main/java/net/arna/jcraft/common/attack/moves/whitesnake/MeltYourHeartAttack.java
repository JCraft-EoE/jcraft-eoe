package net.arna.jcraft.common.attack.moves.whitesnake;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.WSAcidProjectile;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.LivingEntity;
import java.util.Set;

public final class MeltYourHeartAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<MeltYourHeartAttack<A>, A> {
    public MeltYourHeartAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                               final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull MoveType<MeltYourHeartAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        final LivingEntity baseEntity = attacker.getBaseEntity();
        for (int i = 0; i < 10; i++) {
            final float yaw = i * 36F - 180F + i * 3.6F;
            for (int j = 0; j < 10; j++) {
                WSAcidProjectile acidProjectile = new WSAcidProjectile(baseEntity.level(), user);
                acidProjectile.markMeltYourHeart();
                JUtils.shoot(acidProjectile, user, j * 36F - 180F, yaw, 0, 0.66F, 0);
                acidProjectile.setPos(baseEntity.getEyePosition());
                baseEntity.level().addFreshEntity(acidProjectile);
            }
        }

        return targets;
    }

    @Override
    protected @NonNull MeltYourHeartAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull MeltYourHeartAttack<A> copy() {
        return copyExtras(new MeltYourHeartAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<MeltYourHeartAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<MeltYourHeartAttack<?>>, MeltYourHeartAttack<?>> buildCodec(RecordCodecBuilder.Instance<MeltYourHeartAttack<?>> instance) {
            return attackDefault(instance, MeltYourHeartAttack::new);
        }
    }
}
