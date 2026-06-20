package net.arna.jcraft.common.attack.moves.hierophantgreen;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMultiHitAttack;
import net.arna.jcraft.common.entity.projectile.EmeraldProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Getter
public final class EmeraldSplashAttack<A extends IAttacker<? extends A, ?>> extends AbstractMultiHitAttack<EmeraldSplashAttack<A>, A> {
    private final float speed;
    private final boolean isSuper;
    private boolean reflect = false;

    public EmeraldSplashAttack(final int cooldown, final int duration, final float moveDistance, final float damage,
                               final int stun, final float knockback, final float offset, final IntSet hitMoments,
                               final float speed, final boolean isSuper) {
        super(cooldown, duration, moveDistance, damage, stun, 0, knockback, offset, hitMoments);
        this.speed = speed;
        this.isSuper = isSuper;
        ranged = true;
    }

    public EmeraldSplashAttack<A> withReflect() {
        return withReflect(true);
    }

    public EmeraldSplashAttack<A> withReflect(boolean reflect) {
        this.reflect = reflect;
        return this;
    }

    @Override
    public @NotNull MoveType<EmeraldSplashAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        final int emeraldCount = 3 + getChargeTime() / 10;

        for (int i = 0; i < emeraldCount; i++) {
            final EmeraldProjectile emerald = new EmeraldProjectile(baseEntity.level(), user);
            emerald.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, speed, 5F);

            final Vec3 heightOffset = GravityChangerAPI.getEyeOffset(user).scale(0.75);
            emerald.setPos(baseEntity.position().add(heightOffset));

            if (reflect) {
                emerald.withReflect();
            }

            baseEntity.level().addFreshEntity(emerald);
        }

        return Set.of();
    }

    @Override
    protected @NonNull EmeraldSplashAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull EmeraldSplashAttack<A> copy() {
        final EmeraldSplashAttack<A> emeraldSplashAttack = copyExtras(new EmeraldSplashAttack<>(getCooldown(), getDuration(),
                getMoveDistance(), getDamage(), getStun(), getKnockback(), getOffset(), getHitMoments(), speed, isSuper));
        if (reflect) {
            emeraldSplashAttack.withReflect();
        }
        return emeraldSplashAttack;
    }

    public static class Type extends AbstractMultiHitAttack.Type<EmeraldSplashAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<EmeraldSplashAttack<?>>, EmeraldSplashAttack<?>>
        buildCodec(RecordCodecBuilder.Instance<EmeraldSplashAttack<?>> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), duration(), moveDistance(), damage(), stun(),
                            knockback(), offset(), hitMoments(),
                            Codec.FLOAT.fieldOf("speed").forGetter(EmeraldSplashAttack::getSpeed),
                            Codec.BOOL.fieldOf("is_super").forGetter(EmeraldSplashAttack::isSuper),
                            Codec.BOOL.optionalFieldOf("reflect", false).forGetter(EmeraldSplashAttack::isReflect))
                    .apply(instance, applyAttackExtras((cooldown, duration, moveDistance, damage,
                                                        stun,knockback, offset, hitMoments,
                                                        speed, isSuper, reflect) ->
                            new EmeraldSplashAttack<>(cooldown, duration, moveDistance, damage, stun, knockback, offset,
                                    hitMoments, speed, isSuper).withReflect(reflect)));
        }
    }
}
