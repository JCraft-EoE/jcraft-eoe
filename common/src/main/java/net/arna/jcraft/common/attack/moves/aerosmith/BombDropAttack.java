package net.arna.jcraft.common.attack.moves.aerosmith;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.attack.core.data.BaseMoveExtras;
import net.arna.jcraft.common.entity.projectile.AerobombProjectile;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

@Getter
public class BombDropAttack<A extends StandEntity<? extends A, ?>> extends AbstractMove<BombDropAttack<A>, A> {

    private float range;
    @Setter
    private Vec3 dropLocation;
    private int underway;
    private boolean returning;

    public BombDropAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float range) {
        super(cooldown, windup, duration, moveDistance);

        withRange(range);
    }

    public BombDropAttack<A> withRange(final float range) {
        this.range = range;
        return getThis();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        if (user == null) {
            return Set.of();
        }
        final Vec3 userEyePos = user.position().add(GravityChangerAPI.getEyeOffset(user));
        final Vec3 rotVec = user.getLookAngle();
        final HitResult goal = JUtils.raycastAll(user, userEyePos, userEyePos.add(rotVec.scale(getRange())), ClipContext.Fluid.NONE, EntitySelector.LIVING_ENTITY_STILL_ALIVE.and(EntitySelector.NO_SPECTATORS));
        setDropLocation(goal.getLocation().add(0d, 10d, 0d));
        attacker.setRemote(true);
        attacker.setDeltaMovement(dropLocation.subtract(attacker.position()).scale(2d / getDuration()));
        return Set.of();
    }

    @Override
    public void tick(final A attacker) {
        if (dropLocation != null) {
            if (attacker.position().distanceTo(dropLocation) <= 0.5) {
                // TODO play the animation
                dropBomb(attacker);
                returning = true;
                underway = 0;
                dropLocation = null;
            }
            else {
                underway++;
                if (underway < getDuration()) {
                    attacker.setDeltaMovement(dropLocation.subtract(attacker.position()).scale(2d / (getDuration() - underway)));
                }
            }
        }
        if (returning && attacker.hasUser()) {
            final LivingEntity user = attacker.getUserOrThrow();
            if (attacker.position().distanceTo(user.position()) <= 0.5) {
                attacker.setDeltaMovement(Vec3.ZERO);
                attacker.setRemote(false);
                returning = false;
                underway = 0;
            }
            else {
                underway++;
                if (underway < getDuration()) {
                    attacker.setDeltaMovement(user.position().subtract(attacker.position()).scale(2d / (getDuration() - underway)));
                }
            }
        }
    }

    private void dropBomb(A attacker) {
        AerobombProjectile bomb = new AerobombProjectile(attacker.level());
        bomb.setOwner(attacker.hasUser() ? attacker.getUserOrThrow() : attacker);
        bomb.setPos(attacker.position().subtract(0d, 1d, 0d));
        attacker.level().addFreshEntity(bomb);
    }

    @Override
    public @NonNull MoveType<BombDropAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull BombDropAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull BombDropAttack<A> copy() {
        return copyExtras(new BombDropAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getRange()));
    }

    public static class Type extends AbstractMove.Type<BombDropAttack<?>> {
        public static final Type INSTANCE = new Type();

        protected RecordCodecBuilder<BombDropAttack<?>, Float> range() {
            return Codec.FLOAT.fieldOf("range").forGetter(BombDropAttack::getRange);
        }

        protected Products.P6<RecordCodecBuilder.Mu<BombDropAttack<?>>, BaseMoveExtras, Integer, Integer, Integer, Float, Float>
        bombDefault(RecordCodecBuilder.Instance<BombDropAttack<?>> instance) {
            return instance.group(extras(), cooldown(), windup(), duration(), moveDistance(), range());
        }

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<BombDropAttack<?>>, BombDropAttack<?>> buildCodec(final RecordCodecBuilder.Instance<BombDropAttack<?>> instance) {
            return bombDefault(instance).apply(instance, applyExtras(BombDropAttack::new));
        }
    }
}
