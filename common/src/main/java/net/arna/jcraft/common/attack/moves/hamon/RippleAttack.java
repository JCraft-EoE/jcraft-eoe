package net.arna.jcraft.common.attack.moves.hamon;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.util.Function10;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.attack.core.data.AttackMoveExtras;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.common.entity.projectile.HamonWaveEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.arna.jcraft.common.util.JUtils.syncVelocityUpdate;

public final class RippleAttack extends AbstractSimpleAttack<RippleAttack, HamonSpec> {
    public static final float CHARGE_COST = 15.0F;
    @Getter
    private boolean dive;
    private boolean diving = false;
    public RippleAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                        final float hitboxSize, final float knockback, final float offset, final boolean doDive) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        ranged = true;
        hitSpark = JParticleType.HIT_SPARK_2;
        dive = doDive;
    }

    public RippleAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun,
                        final float hitboxSize, final float knockback, final float offset) {
        this(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, true);
    }

    public RippleAttack withNoDive() {
        dive = false;
        return this;
    }

    @Override
    public void onInitiate(HamonSpec attacker) {
        super.onInitiate(attacker);

        attacker.drainCharge(CHARGE_COST);
        attacker.setUseHamonNext(false);

        diving = dive;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final HamonSpec attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        if (user.onGround()) {
            final Level level = user.level();

            final HamonWaveEntity wave = new HamonWaveEntity(level);
            wave.copyPosition(user);
            wave.setMaster(user);
            level.addFreshEntity(wave);
            GravityChangerAPI.setDefaultGravityDirection(wave, GravityChangerAPI.getGravityDirection(user));

            JUtils.serverPlaySound(JSoundRegistry.IMPACT_2.get(), (ServerLevel) user.level(), user.position());
        }

        if (JUtils.getSpec(user) instanceof HamonSpec hamonSpec)
            for (LivingEntity target : targets)
                hamonSpec.processTarget(target);

        return targets;
    }

    @Override
    public void tick(HamonSpec attacker) {
        super.tick(attacker);

        if (attacker.getCurrentMove() instanceof RippleAttack rippleAttack && rippleAttack.diving) {
            if (attacker.user.onGround()) {
                rippleAttack.diving = false;
                return;
            }

            attacker.user.push(0, -0.3, 0);
            syncVelocityUpdate(attacker.user);
        }
    }

    @Override
    public @NotNull MoveType<RippleAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull RippleAttack getThis() {
        return this;
    }

    @Override
    public @NonNull RippleAttack copy() {
        return copyExtras(new RippleAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<RippleAttack> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<RippleAttack, Boolean> dive() {
            return Codec.BOOL.fieldOf("dive").forGetter(RippleAttack::isDive);
        }

        protected Products.P12<RecordCodecBuilder.Mu<RippleAttack>, BaseMoveExtras, AttackMoveExtras, Integer, Integer, Integer, Float, Float, Integer, Float, Float, Float, Boolean>
        rippleDefault(RecordCodecBuilder.Instance<RippleAttack> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(), damage(),
                    stun(), hitboxSize(), knockback(), offset(), dive());
        }

        protected App<RecordCodecBuilder.Mu<RippleAttack>, RippleAttack> rippleDefault(RecordCodecBuilder.Instance<RippleAttack> instance, Function10<Integer,
                Integer, Integer, Float, Float, Integer, Float, Float, Float, Boolean, RippleAttack> function) {
            return rippleDefault(instance).apply(instance, applyAttackExtras(function));
        }

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<RippleAttack>, RippleAttack> buildCodec(RecordCodecBuilder.Instance<RippleAttack> instance) {
            return rippleDefault(instance, RippleAttack::new);
        }
    }
}
