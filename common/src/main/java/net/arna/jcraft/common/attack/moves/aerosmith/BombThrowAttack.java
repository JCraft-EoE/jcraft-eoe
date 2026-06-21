package net.arna.jcraft.common.attack.moves.aerosmith;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.entity.projectile.AerobombProjectile;
import net.arna.jcraft.common.entity.stand.AerosmithEntity;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.common.util.math.V3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class BombThrowAttack extends AbstractMove<BombThrowAttack, AerosmithEntity> {
    public BombThrowAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(AerosmithEntity attacker, LivingEntity user) {
        final var forward = attacker.getLookAngle();
        final var up = JUtils.getLocalUp(attacker);

        final var location = new V3(attacker.position());
        final var launchVel = new V3();

        location.add(forward, 2.5).add(up, 1.5);
        launchVel.add(forward, 0.2).add(up, 0.1);

        final var level = attacker.level();
        final var bomb = new AerobombProjectile(level);

        bomb.setPos(new Vec3(location.x, location.y, location.z));
        bomb.setDeltaMovement(new Vec3(launchVel.x, launchVel.y, launchVel.z));

        level.addFreshEntity(bomb);

        return Set.of();
    }

    @Override
    public @NonNull MoveType<BombThrowAttack> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull BombThrowAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BombThrowAttack copy() {
        return copyExtras(new BombThrowAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<BombThrowAttack> {
        public static final BombThrowAttack.Type INSTANCE = new BombThrowAttack.Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<BombThrowAttack>, BombThrowAttack> buildCodec(final RecordCodecBuilder.Instance<BombThrowAttack> instance) {
            return baseDefault(instance).apply(instance, applyExtras(BombThrowAttack::new));
        }
    }
}
