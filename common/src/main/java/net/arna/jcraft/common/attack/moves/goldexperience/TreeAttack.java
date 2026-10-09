package net.arna.jcraft.common.attack.moves.goldexperience;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.GETreeEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class TreeAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<TreeAttack<A>, A> {
    public TreeAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun, final float hitboxSize,
                      final float knockback, final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        hitSpark = JParticleType.HIT_SPARK_2;
    }

    @Override
    public @NotNull MoveType<TreeAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        final Vec3 direction = user.getLookAngle();
        LivingEntity baseEntity = attacker.getBaseEntity();

        final var tree = new GETreeEntity(baseEntity.level(), user, direction.scale(1.2));

        final Direction gravity = GravityChangerAPI.getGravityDirection(baseEntity);
        GravityChangerAPI.setDefaultGravityDirection(tree, gravity);

        final Vec3 midPos = RotationUtil.vecPlayerToWorld(0.0, baseEntity.getBbHeight() * 0.25, 0.0, gravity);
        final double e = direction.x, f = direction.y, g = direction.z;
        final double l = direction.horizontalDistance();
        tree.moveTo(baseEntity.getX() + midPos.x, baseEntity.getY() + midPos.y, baseEntity.getZ() + midPos.z,
                (float) (Mth.atan2(e, g) * 57.2957763671875),
                (float) (Mth.atan2(f, l) * 57.2957763671875)
        );

        baseEntity.level().addFreshEntity(tree);

        return targets;
    }

    @Override
    protected @NonNull TreeAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull TreeAttack<A> copy() {
        return copyExtras(new TreeAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<TreeAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<TreeAttack<?>>, TreeAttack<?>> buildCodec(RecordCodecBuilder.Instance<TreeAttack<?>> instance) {
            return attackDefault(instance, TreeAttack::new);
        }
    }
}
