package net.arna.jcraft.common.attack.moves.whitesnake;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.WSAcidProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import java.util.Set;

public final class ChargedSpewAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<ChargedSpewAttack<A>, A> {
    public ChargedSpewAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                             final float hitboxSize, final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.ranged = true;
    }

    @Override
    public @NonNull MoveType<ChargedSpewAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        final LivingEntity baseEntity = attacker.getBaseEntity();
        final Direction gravity = GravityChangerAPI.getGravityDirection(user);
        for (int i = 0; i < 5; i++) {
            final WSAcidProjectile acidProjectile = new WSAcidProjectile(baseEntity.level(), user);

            final Vec2 corrected = RotationUtil.rotPlayerToWorld(user.getYRot() - 75F + i * 37.5F, user.getXRot(), gravity);
            JUtils.shoot(acidProjectile, user, corrected.y, corrected.x, 0, 0.66F, 0);

            acidProjectile.setPos(baseEntity.getEyePosition());
            baseEntity.level().addFreshEntity(acidProjectile);
        }

        return targets;
    }

    @Override
    protected @NonNull ChargedSpewAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull ChargedSpewAttack<A> copy() {
        return copyExtras(new ChargedSpewAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<ChargedSpewAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ChargedSpewAttack<?>>, ChargedSpewAttack<?>> buildCodec(RecordCodecBuilder.Instance<ChargedSpewAttack<?>> instance) {
            return attackDefault(instance, ChargedSpewAttack::new);
        }
    }
}
