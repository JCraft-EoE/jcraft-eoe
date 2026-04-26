package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Set;

public final class ThunderstormShockAttack extends AbstractSimpleAttack<ThunderstormShockAttack, WeatherReportEntity> {

    private final float range;

    private Vec3 lightningPos;
    private WeakReference<LightningBolt> bolt = new WeakReference<>(null);
    private int ringTicksLeft = 0;

    public ThunderstormShockAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                   final float damage, final int stun, final float hitboxSize, final float knockback,
                                   final float offset, final float range) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.range = range;
        withHitSpark(null);
    }

    @Override
    public void onInitiate(final WeatherReportEntity attacker) {
        super.onInitiate(attacker);
        ringTicksLeft = 0;
        final LivingEntity user = attacker.getUserOrThrow();
        final Vec3 eyePos = user.getEyePosition();
        final Vec3 lookVec = user.getLookAngle().scale(range);

        final EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(user, eyePos, eyePos.add(lookVec),
                user.getBoundingBox().inflate(range), EntitySelector.NO_CREATIVE_OR_SPECTATOR, range * range);

        lightningPos = Objects.requireNonNullElseGet(entityHit,
                () -> attacker.level().clip(new ClipContext(eyePos, eyePos.add(lookVec),
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user))
        ).getLocation();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        if (!(attacker.level() instanceof ServerLevel serverLevel)) return Set.of();

        final LightningBolt newBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
        newBolt.setPos(lightningPos);
        newBolt.setVisualOnly(false);
        bolt = new WeakReference<>(newBolt);

        final Set<LivingEntity> targets = super.perform(attacker, user);

        serverLevel.addFreshEntity(newBolt);

        final AABB strikeBox = AABB.ofSize(lightningPos, getHitboxSize(), getHitboxSize() * 2, getHitboxSize());
        serverLevel.getEntitiesOfClass(LivingEntity.class, strikeBox,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != user)).forEach(e -> e.thunderHit(serverLevel, newBolt));

        final float blastRadius = getHitboxSize() * 2.5f;
        serverLevel.getEntitiesOfClass(LivingEntity.class,
                AABB.ofSize(lightningPos, blastRadius * 2, blastRadius, blastRadius * 2),
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != user))
                .forEach(e -> e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1, false, false)));

        ringTicksLeft = 10;

        return targets;
    }

    @Override
    public void activeTick(final WeatherReportEntity attacker, final int moveStun) {
        super.activeTick(attacker, moveStun);
        if (ringTicksLeft <= 0 || lightningPos == null) return;
        ringTicksLeft--;
        if (!(attacker.level() instanceof ServerLevel serverLevel)) return;

        final double radius = (10 - ringTicksLeft) * 0.45;
        final int points = 20;
        for (int i = 0; i < points; i++) {
            final double angle = i * (Math.PI * 2 / points);
            final double px = lightningPos.x + Math.cos(angle) * radius;
            final double pz = lightningPos.z + Math.sin(angle) * radius;
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, lightningPos.y + 0.1, pz, 1, 0.05, 0.05, 0.05, 0.02);
        }
    }

    @Override
    protected Set<AABB> calculateBoxes(final WeatherReportEntity attacker, final LivingEntity user,
                                       final Vec3 rotVec, final Vec3 upVec, final Vec3 hPos, final Vec3 fPos) {
        return lightningPos == null ? Set.of() : Set.of(createBox(lightningPos, getHitboxSize()));
    }

    @Override
    public @NonNull MoveType<ThunderstormShockAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull ThunderstormShockAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ThunderstormShockAttack copy() {
        return copyExtras(new ThunderstormShockAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), range));
    }

    public static class Type extends AbstractSimpleAttack.Type<ThunderstormShockAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ThunderstormShockAttack>, ThunderstormShockAttack> buildCodec(RecordCodecBuilder.Instance<ThunderstormShockAttack> instance) {
            return attackDefault(instance, (cd, wu, dur, md, dmg, st, hs, kb, off) ->
                    new ThunderstormShockAttack(cd, wu, dur, md, dmg, st, hs, kb, off, 24f));
        }
    }
}
