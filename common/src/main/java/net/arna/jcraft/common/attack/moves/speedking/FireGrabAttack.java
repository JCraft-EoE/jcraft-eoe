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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class FireGrabAttack extends AbstractGrabAttack<FireGrabAttack, SpeedKingEntity, SpeedKingEntity.State> {
    private static final int BOILING_INTERVAL = 10;

    public FireGrabAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                          final float damage, final int stun, final float hitboxSize, final float knockback,
                          final float offset, final AbstractMove<?, ? super SpeedKingEntity> hitMove,
                          final StateContainer<SpeedKingEntity.State> hitState) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMove, hitState, 40, 1.0);
    }

    @Override
    public void activeTick(SpeedKingEntity attacker, int moveStun) {
        super.activeTick(attacker, moveStun);

        // Apply boiling effect every BOILING_INTERVAL ticks to grabbed targets
        if (moveStun % BOILING_INTERVAL == 0 && attacker.hasUser()) {
            LivingEntity user = attacker.getUserOrThrow();

            // Find entities currently being grabbed by this attacker
            for (LivingEntity entity : attacker.level().getEntitiesOfClass(LivingEntity.class,
                    attacker.getBoundingBox().inflate(getHitboxSize()))) {

                // Skip the user and the stand itself
                if (entity == user || entity == attacker) {
                    continue;
                }

                // Check if this entity is being grabbed
                Vec3 grabberPos = attacker.position();
                Vec3 entityPos = entity.position();
                double distance = grabberPos.distanceTo(entityPos);

                if (distance < getGrabOffset() + 1.0) {
                    // Apply Boiling Level 1 (amplifier 0)
                    entity.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), 200, 0, false, true));
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
                getHitState()));
    }

    public static class Type extends AbstractGrabAttack.Type<FireGrabAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FireGrabAttack>, FireGrabAttack> buildCodec(RecordCodecBuilder.Instance<FireGrabAttack> instance) {
            return this.<SpeedKingEntity, SpeedKingEntity.State>grabDefault(instance, FireGrabAttack::new);
        }
    }
}