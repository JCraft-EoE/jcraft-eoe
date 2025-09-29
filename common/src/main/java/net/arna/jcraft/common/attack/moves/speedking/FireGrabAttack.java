package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.StateContainer;
import net.arna.jcraft.api.attack.moves.AbstractGrabAttack;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public final class FireGrabAttack extends AbstractGrabAttack<FireGrabAttack, SpeedKingEntity, SpeedKingEntity.State> {
    private static final int BOILING_INTERVAL = 10;

    public FireGrabAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                          final float damage, final int stun, final float hitboxSize, final float knockback,
                          final float offset, final AbstractMove<?, ? super SpeedKingEntity> hitMove,
                          final int grabDuration, final double grabOffset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMove,
                StateContainer.of(SpeedKingEntity.State.FIRE_GRAB), grabDuration, grabOffset);
        withHoldable(true);
    }

    @Override
    public void activeTick(SpeedKingEntity attacker, int moveStun) {
        super.activeTick(attacker, moveStun);

        // Apply boiling effect every BOILING_INTERVAL ticks to grabbed targets
        if (moveStun % BOILING_INTERVAL == 0 && attacker.hasUser()) {
            // Find entities currently being grabbed by this attacker
            for (LivingEntity entity : attacker.level().getEntitiesOfClass(LivingEntity.class,
                    attacker.getBoundingBox().inflate(3.0))) {

                if (entity != attacker.getUserOrThrow()) {
                    // Check if this entity is being grabbed (basic check if they're close and not moving much)
                    Vec3 grabberPos = attacker.position();
                    Vec3 entityPos = entity.position();
                    double distance = grabberPos.distanceTo(entityPos);

                    if (distance < 2.0) { // Close enough to be considered grabbed
                        // Add one level of boiling (up to level 4)
                        MobEffectInstance currentBoiling = entity.getEffect(JStatusRegistry.BOILING.get());
                        int newLevel = (currentBoiling != null) ? Math.min(currentBoiling.getAmplifier() + 1, 4) : 0;
                        entity.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 200, newLevel, false, true));
                    }
                }
            }
        }
    }

    @Override
    public @NonNull MoveType<FireGrabAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull FireGrabAttack getThis() {
        return this;
    }

    @Override
    public @NonNull FireGrabAttack copy() {
        return copyExtras(new FireGrabAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), getHitMove(),
                getGrabDuration(), getGrabOffset()));
    }

    public static class Type extends AbstractGrabAttack.Type<FireGrabAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FireGrabAttack>, FireGrabAttack> buildCodec(RecordCodecBuilder.Instance<FireGrabAttack> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), moveDistance(), damage(),
                            stun(), hitboxSize(), knockback(), offset(), this.<SpeedKingEntity>hitMove(), grabDuration(), grabOffset())
                    .apply(instance, applyAttackExtras(FireGrabAttack::new));
        }
    }
}