package net.arna.jcraft.common.attack.moves.hamon;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.NonNull;
import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.component.living.CommonHamonComponent;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.api.registry.JParticleTypeRegistry;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.spec.HamonSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class SendoAttack extends AbstractSimpleAttack<SendoAttack, HamonSpec> {
    public static final float CHARGE_COST = 10.0F;
    private static final int AFTERSHOCK_DELAY = 30;
    private final Object2IntMap<LivingEntity> aftershockTimers = new Object2IntOpenHashMap<>(4);
    public SendoAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                       float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull MoveType<SendoAttack> getMoveType() {
        return SendoAttack.Type.INSTANCE;
    }

    @Override
    public void onInitiate(HamonSpec attacker) {
        super.onInitiate(attacker);

        attacker.drainCharge(CHARGE_COST);
        attacker.setUseHamonNext(false);
    }

    @Override
    public void tick(HamonSpec attacker) {
        super.tick(attacker);

        final var entrySet = aftershockTimers.object2IntEntrySet();

        for (var entry : entrySet) {
            final LivingEntity entity = entry.getKey();
            final int time = entry.getIntValue() - 1;
            aftershockTimers.replace(entity, time);

            if (time == 0) {
                final ServerLevel level = (ServerLevel) entity.level();

                entity.removeEffect(JStatusRegistry.KNOCKDOWN.get());

                Attacks.damageLogic(level, entity, new AttackData(
                        Vec3.ZERO, 10, StunType.BURSTABLE.ordinal(), false,
                        3f, true, 3, level.damageSources().indirectMagic(attacker.user, null),
                        attacker.user, CommonHitPropertyComponent.HitAnimation.CRUSH, null,
                        false, false
                ));

                var packet = new ClientboundLevelParticlesPacket(JParticleTypeRegistry.HAMON_SPARK.get(),
                        false,
                        entity.getX(), entity.getY(), entity.getZ(),
                        1, 1, 1,
                        0.2f, 10);

                for (ServerPlayer tracker : JUtils.tracking(entity))
                    tracker.connection.send(packet);
            }
        }

        entrySet.removeIf((entry -> entry.getIntValue() <= 0));
    }

    @Override
    public @NonNull Set<LivingEntity> perform(HamonSpec attacker, LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);

        for (LivingEntity target : targets) {
            aftershockTimers.put(target, AFTERSHOCK_DELAY);
            target.addEffect(new MobEffectInstance(JStatusRegistry.KNOCKDOWN.get(), 40, 0, true, false));
            target.playSound(JSoundRegistry.HAMON_CRACKLES.get(), 1.0f, 1.0f);
        }

        if (JUtils.getSpec(user) instanceof HamonSpec hamonSpec) {
            for (LivingEntity target : targets) {
                hamonSpec.processTarget(target);
                if (user instanceof final ServerPlayer player && target instanceof Enemy) {
                    final CommonHamonComponent hamon = JComponentPlatformUtils.getHamon(player);
                    if (!isAerialVariant() && !isCrouchingVariant()) {
                        hamon.setLastSendoed(target.getUUID());
                    }
                    else if (isAerialVariant()) {
                        hamon.setLastSendoAired(target.getUUID());
                    }
                }
            }
        }

        return targets;
    }

    @Override
    protected @NonNull SendoAttack getThis() {
        return this;
    }

    @Override
    public @NonNull SendoAttack copy() {
        return copyExtras(new SendoAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<SendoAttack> {
        public static final SendoAttack.Type INSTANCE = new SendoAttack.Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SendoAttack>, SendoAttack> buildCodec(RecordCodecBuilder.Instance<SendoAttack> instance) {
            return attackDefault(instance, SendoAttack::new);
        }
    }
}
