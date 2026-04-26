package net.arna.jcraft.common.attack.moves.weatherreport;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.ElectricBoltProjectile;
import net.arna.jcraft.common.entity.stand.WeatherReportEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class ElectrifiedShotAttack extends AbstractMove<ElectrifiedShotAttack, WeatherReportEntity> {

    private final float boltSpeed;

    public ElectrifiedShotAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                                 final float boltSpeed) {
        super(cooldown, windup, duration, moveDistance);
        this.boltSpeed = boltSpeed;
        ranged = true;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final WeatherReportEntity attacker, final LivingEntity user) {
        final ElectricBoltProjectile bolt = new ElectricBoltProjectile(attacker.level(), user);

        final Vec3 heightOffset = GravityChangerAPI.getEyeOffset(user).scale(0.75);
        bolt.setPos(attacker.getBaseEntity().position().add(heightOffset));
        bolt.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, boltSpeed, 0.05F);

        attacker.level().addFreshEntity(bolt);
        return Set.of();
    }

    @Override
    public @NonNull MoveType<ElectrifiedShotAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull ElectrifiedShotAttack getThis() {
        return this;
    }

    @Override
    public @NonNull ElectrifiedShotAttack copy() {
        return copyExtras(new ElectrifiedShotAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), boltSpeed));
    }

    public static class Type extends AbstractMove.Type<ElectrifiedShotAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<ElectrifiedShotAttack>, ElectrifiedShotAttack> buildCodec(RecordCodecBuilder.Instance<ElectrifiedShotAttack> instance) {
            return baseDefault(instance, (cd, wu, dur, md) -> new ElectrifiedShotAttack(cd, wu, dur, md, 0.6f));
        }
    }
}
