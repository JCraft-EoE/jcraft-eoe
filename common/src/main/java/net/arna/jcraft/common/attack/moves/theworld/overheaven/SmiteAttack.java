package net.arna.jcraft.common.attack.moves.theworld.overheaven;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Set;

@Getter
public final class SmiteAttack<A extends IAttacker<? extends A, ?>> extends AbstractSimpleAttack<SmiteAttack<A>, A> {
    private final boolean aerial;
    private final int levitationDuration, levitationAmplifier;

    private Vec3 lightningPos;
    private WeakReference<LightningBolt> bolt = new WeakReference<>(null);

    public SmiteAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                       final float damage, final int stun, final float hitboxSize, final float knockback,
                       final float offset, final boolean aerial, final int levitationDuration,
                       final int levitationAmplifier) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.aerial = ranged = aerial;
        this.levitationDuration = levitationDuration;
        this.levitationAmplifier = levitationAmplifier;
        this.withHitSpark(null);
    }

    @Override
    public @NonNull MoveType<SmiteAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void onInitiate(final A attacker) {
        super.onInitiate(attacker);

        final LivingEntity baseEntity = attacker.getBaseEntity();
        LivingEntity user = attacker.getUserOrThrow();
        if (!aerial) {
            lightningPos = user.position();
        } else {
            Vec3 eP = user.getEyePosition();
            Vec3 rangeMod = user.getLookAngle().scale(24);
            EntityHitResult eHit = ProjectileUtil.getEntityHitResult(user, eP, eP.add(rangeMod),
                    user.getBoundingBox().inflate(24),
                    EntitySelector.NO_CREATIVE_OR_SPECTATOR,
                    576 // Squared
            );

            lightningPos = Objects.requireNonNullElseGet(eHit, () -> baseEntity.level().clip(new ClipContext(eP, eP.add(rangeMod),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user))).getLocation();
        }

        AreaEffectCloud effectCloud = new AreaEffectCloud(baseEntity.level(),
                lightningPos.x, lightningPos.y, lightningPos.z);
        effectCloud.setOwner(user);
        effectCloud.setRadius(getDamage() / 2f);
        effectCloud.setWaitTime(10);
        effectCloud.setRadiusPerTick(-0.5f);

        baseEntity.level().addFreshEntity(effectCloud);

        baseEntity.level().playSound(null, lightningPos.x, lightningPos.y, lightningPos.z,
                JSoundRegistry.TWOH_CHARGE.get(), SoundSource.PLAYERS, 1, 1);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final LivingEntity baseEntity = attacker.getBaseEntity();
        LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, baseEntity.level());
        bolt.setVisualOnly(true);
        bolt.setPos(lightningPos);
        this.bolt = new WeakReference<>(bolt);

        Set<LivingEntity> targets = super.perform(attacker, user);

        baseEntity.level().addFreshEntity(bolt);
        return targets;
    }

    @Override
    protected Set<AABB> calculateBoxes(final A attacker, final LivingEntity user, final Vec3 rotVec, final Vec3 upVec, final Vec3 hPos, final Vec3 fPos) {
        return Set.of(createBox(lightningPos, getHitboxSize()));
    }

    @Override
    protected void processTarget(final A attacker, final LivingEntity target, final Vec3 kbVec, final DamageSource damageSource) {
        super.processTarget(attacker, target, kbVec, damageSource);

        LightningBolt bolt = this.bolt.get();

        if (bolt != null) {
            target.thunderHit((ServerLevel) attacker.getEntityWorld(), bolt);
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, levitationDuration, levitationAmplifier, true, false));
        }
    }

    @Override
    protected @NonNull SmiteAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull SmiteAttack<A> copy() {
        return copyExtras(new SmiteAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), isAerial(), levitationDuration, levitationAmplifier));
    }

    public static class Type extends AbstractSimpleAttack.Type<SmiteAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<SmiteAttack<?>>, SmiteAttack<?>> buildCodec(RecordCodecBuilder.Instance<SmiteAttack<?>> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(), damage(),
                    stun(), hitboxSize(), knockback(), offset(), Codec.BOOL.fieldOf("aerial").forGetter(SmiteAttack::isAerial),
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("levitation_duration").forGetter(SmiteAttack::getLevitationDuration),
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("levitation_amplifier").forGetter(SmiteAttack::getLevitationAmplifier))
                    .apply(instance, applyAttackExtras(SmiteAttack::new));
        }
    }
}
