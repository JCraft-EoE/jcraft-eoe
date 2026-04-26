package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class ElectrifiedGroundRippleAttack extends AbstractSimpleAttack<ElectrifiedGroundRippleAttack, WeatherReportEntity> {

    private final int maxRipples;
    private final float rippleRange;

    public ElectrifiedGroundRippleAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                         final float damage, final int stun, final float hitboxSize, final float knockback,
                                         final float offset, final int maxRipples, final float rippleRange) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        this.maxRipples = maxRipples;
        this.rippleRange = rippleRange;
        withHitSpark(null);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        if (level(attacker).isClientSide) return Set.of();

        final Vec3 groundPos = attacker.getBaseEntity().position();
        spawnLightningVisual(attacker, groundPos);

        final AABB searchBox = AABB.ofSize(groundPos, rippleRange * 2, rippleRange, rippleRange * 2);
        final List<LivingEntity> nearby = new ArrayList<>(level(attacker).getEntitiesOfClass(LivingEntity.class, searchBox,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e != user && e != attacker.getBaseEntity())));
        nearby.sort(Comparator.comparingDouble(e -> e.distanceToSqr(groundPos)));

        final int rippleCount = Math.min(nearby.size(), maxRipples);
        for (int i = 0; i < rippleCount; i++) {
            final LivingEntity target = nearby.get(i);
            final Vec3 kbVec = target.position().subtract(groundPos).normalize().scale(0.3);
            spawnLightningVisual(attacker, target.position());
            processTarget(attacker, target, kbVec, attacker.getDamageSource());
        }

        return Set.of();
    }

    @Override
    protected Set<AABB> calculateBoxes(final WeatherReportEntity attacker, final LivingEntity user,
                                       final Vec3 rotVec, final Vec3 upVec, final Vec3 hPos, final Vec3 fPos) {
        return Set.of();
    }

    private void spawnLightningVisual(final WeatherReportEntity attacker, final Vec3 pos) {
        if (!(level(attacker) instanceof ServerLevel serverLevel)) return;
        final LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, serverLevel);
        bolt.setVisualOnly(true);
        bolt.setPos(pos);
        serverLevel.addFreshEntity(bolt);

        for (int i = 0; i < 6; i++) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    pos.x + attacker.getRandom().nextGaussian() * 0.5,
                    pos.y,
                    pos.z + attacker.getRandom().nextGaussian() * 0.5,
                    1, 0, 0, 0, 0.05);
        }
    }

    private net.minecraft.world.level.Level level(final WeatherReportEntity attacker) {
        return attacker.getBaseEntity().level();
    }

    @Override
    public @NonNull MoveType<ElectrifiedGroundRippleAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull ElectrifiedGroundRippleAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ElectrifiedGroundRippleAttack copy() {
        return copyExtras(new ElectrifiedGroundRippleAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), maxRipples, rippleRange));
    }

    public static class Type extends AbstractSimpleAttack.Type<ElectrifiedGroundRippleAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ElectrifiedGroundRippleAttack>, ElectrifiedGroundRippleAttack> buildCodec(RecordCodecBuilder.Instance<ElectrifiedGroundRippleAttack> instance) {
            return attackDefault(instance, (cd, wu, dur, md, dmg, st, hs, kb, off) ->
                    new ElectrifiedGroundRippleAttack(cd, wu, dur, md, dmg, st, hs, kb, off, 5, 10f));
        }
    }
}
