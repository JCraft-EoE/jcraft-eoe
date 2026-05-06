package net.arna.jcraft.common.attack.moves.killerqueen;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.component.living.CommonBombTrackerComponent;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.KillerQueenEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;

@Getter
public final class KQGrabHitAttack extends AbstractMove<KQGrabHitAttack, KillerQueenEntity> {
    private final int stun;

    public KQGrabHitAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final int stun) {
        super(cooldown, windup, duration, moveDistance);
        this.stun = stun;
    }

    @Override
    public @NotNull MoveType<KQGrabHitAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final KillerQueenEntity attacker, final LivingEntity user) {
        attacker.playSound(JSoundRegistry.KQ_DETONATE.get(), 1, 1);

        final CommonBombTrackerComponent.BombData bombData = JComponentPlatformUtils.getBombTracker(user).getMainBomb();

        if (bombData.bombEntity instanceof LivingEntity livingEntity) {
            final ServerLevel world = (ServerLevel) attacker.level();

            final Vec3 pos = livingEntity.position();
            JCraft.createParticle(world, pos.x, pos.y, pos.z, JParticleType.BOOM);
            JUtils.serverPlaySound(JSoundRegistry.KQ_NUKE.get(), world, pos, 96);

            final DamageSource damageSource = JDamageSources.stand(attacker);

            damageLogic(world, livingEntity, new Vec3(0, 1, 0), stun, 3, true,
                    11f, false, 4, damageSource, user, null);
            livingEntity.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), 35, 0, true, false));
        }

        bombData.reset();

        return Set.of();
    }

    @Override
    protected @NonNull KQGrabHitAttack getThis() {
        return this;
    }

    @Override
    public @NonNull KQGrabHitAttack copy() {
        return copyExtras(new KQGrabHitAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getStun()));
    }

    public static class Type extends AbstractMove.Type<KQGrabHitAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<KQGrabHitAttack>, KQGrabHitAttack> buildCodec(RecordCodecBuilder.Instance<KQGrabHitAttack> instance) {
            return baseDefault(instance).and(Codec.INT.fieldOf("stun").forGetter(KQGrabHitAttack::getStun))
                    .apply(instance, applyExtras(KQGrabHitAttack::new));
        }
    }
}
