package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ChainLightningAttack extends AbstractSimpleAttack<ChainLightningAttack, WeatherReportEntity> {

    private final int maxBounces;
    private final float bounceRange;
    private final float damageDecay;

    public ChainLightningAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                final float damage, final int stun, final float hitboxSize, final float knockback,
                                final float offset, final int maxBounces, final float bounceRange, final float damageDecay) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.maxBounces = maxBounces;
        this.bounceRange = bounceRange;
        this.damageDecay = damageDecay;
        withHitSpark(null);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        final Set<LivingEntity> initialTargets = super.perform(attacker, user);
        if (initialTargets.isEmpty() || attacker.level().isClientSide) return initialTargets;

        final Set<LivingEntity> allTargets = new HashSet<>(initialTargets);

        LivingEntity lastTarget = initialTargets.iterator().next();
        float chainDamage = getDamage() * damageDecay;
        int bouncesLeft = maxBounces - 1;

        while (bouncesLeft > 0 && chainDamage >= 0.5f) {
            final LivingEntity current = lastTarget;
            final List<LivingEntity> candidates = new ArrayList<>(
                    attacker.level().getEntitiesOfClass(LivingEntity.class,
                            AABB.ofSize(current.position(), bounceRange * 2, bounceRange * 2, bounceRange * 2),
                            EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> !allTargets.contains(e) && e != user && e != attacker.getBaseEntity()))
            );
            if (candidates.isEmpty()) break;

            candidates.sort(Comparator.comparingDouble(e -> e.distanceToSqr(current)));
            final LivingEntity nextTarget = candidates.get(0);

            spawnChainParticles(attacker, current.getEyePosition(), nextTarget.getEyePosition());
            spawnLightningVisual(attacker, nextTarget.position());
            playBounceSound(attacker, nextTarget.position());

            final Vec3 kbVec = nextTarget.position().subtract(current.position()).normalize().scale(getKnockback() * 0.5);
            withDamage(chainDamage);
            processTarget(attacker, nextTarget, kbVec, attacker.getDamageSource());

            allTargets.add(nextTarget);
            lastTarget = nextTarget;
            chainDamage *= damageDecay;
            bouncesLeft--;
        }

        withDamage(getDamage());

        return allTargets;
    }

    private void spawnLightningVisual(final WeatherReportEntity attacker, final Vec3 pos) {
        if (!(attacker.level() instanceof ServerLevel serverLevel)) return;
        final LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
        bolt.setVisualOnly(true);
        bolt.setPos(pos);
        serverLevel.addFreshEntity(bolt);
    }

    private void spawnChainParticles(final WeatherReportEntity attacker, final Vec3 from, final Vec3 to) {
        if (!(attacker.level() instanceof ServerLevel serverLevel)) return;
        final var rng = attacker.getRandom();
        final Vec3 dir = to.subtract(from);
        final double len = dir.length();
        if (len < 1e-4) return;

        final Vec3 up = Math.abs(dir.y / len) < 0.9 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        final Vec3 perp = dir.cross(up).normalize();

        final int segments = 7;
        Vec3 prev = from;
        for (int i = 1; i <= segments; i++) {
            final double t = (double) i / segments;
            final Vec3 base = from.add(dir.scale(t));
            final double jag = (i < segments) ? (rng.nextDouble() - 0.5) * len * 0.3 : 0;
            final Vec3 wp = base.add(perp.scale(jag));

            final Vec3 seg = wp.subtract(prev);
            final int steps = Math.max(2, (int) (seg.length() / 0.25));
            for (int j = 0; j <= steps; j++) {
                final Vec3 p = prev.add(seg.scale((double) j / steps));
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0.04, 0.04, 0.04, 0.01);
            }
            prev = wp;
        }
    }

    private void playBounceSound(final WeatherReportEntity attacker, final Vec3 pos) {
        if (!(attacker.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER,
                0.4f, 1.2f + attacker.getRandom().nextFloat() * 0.3f);
    }

    @Override
    public @NonNull MoveType<ChainLightningAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull ChainLightningAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ChainLightningAttack copy() {
        return copyExtras(new ChainLightningAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), maxBounces, bounceRange, damageDecay));
    }

    public static class Type extends AbstractSimpleAttack.Type<ChainLightningAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ChainLightningAttack>, ChainLightningAttack> buildCodec(RecordCodecBuilder.Instance<ChainLightningAttack> instance) {
            return attackDefault(instance, (cd, wu, dur, md, dmg, st, hs, kb, off) ->
                    new ChainLightningAttack(cd, wu, dur, md, dmg, st, hs, kb, off, 8, 6f, 0.7f));
        }
    }
}
