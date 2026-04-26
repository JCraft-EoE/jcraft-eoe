package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.projectile.WindGustEntity;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public final class HeavyWindSlashAttack extends AbstractSimpleAttack<HeavyWindSlashAttack, WeatherReportEntity> {

    public HeavyWindSlashAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                final float damage, final int stun, final float hitboxSize, final float knockback,
                                final float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);
        if (!attacker.level().isClientSide) {
            final double lookYaw = Math.atan2(-user.getLookAngle().x, user.getLookAngle().z);
            for (int i = -1; i <= 1; i++) {
                final double spreadAngle = lookYaw + i * 0.25;
                final WindGustEntity gust = new WindGustEntity(attacker.level());
                gust.setMaster(user);
                gust.setVelocity(new net.minecraft.world.phys.Vec3(
                        -Math.sin(spreadAngle), user.getLookAngle().y, Math.cos(spreadAngle)));
                gust.setDamageValues(attacker.isElectrified() ? 3.5f : 2.5f, 14);
                gust.setLarge(true);
                gust.setPos(user.getEyePosition().subtract(0, 0.5, 0));
                attacker.level().addFreshEntity(gust);
            }
        }
        return targets;
    }

    @Override
    public @NonNull MoveType<HeavyWindSlashAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull HeavyWindSlashAttack getThis() {
        return this;
    }

    @Override
    public @NonNull HeavyWindSlashAttack copy() {
        return copyExtras(new HeavyWindSlashAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<HeavyWindSlashAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<HeavyWindSlashAttack>, HeavyWindSlashAttack> buildCodec(RecordCodecBuilder.Instance<HeavyWindSlashAttack> instance) {
            return attackDefault(instance, HeavyWindSlashAttack::new);
        }
    }
}
