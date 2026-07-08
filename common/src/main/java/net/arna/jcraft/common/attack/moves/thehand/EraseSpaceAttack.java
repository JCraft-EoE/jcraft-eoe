package net.arna.jcraft.common.attack.moves.thehand;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.enums.MobilityType;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class EraseSpaceAttack<A extends StandEntity<? extends A, ?>> extends AbstractEraseAttack<EraseSpaceAttack<A>, A> {
    public EraseSpaceAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                            float hitboxSize, float knockback, float offset) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset);
        mobilityType = MobilityType.DASH;
    }

    @Override
    public @NonNull MoveType<EraseSpaceAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public @NonNull Set<LivingEntity> perform(A attacker, LivingEntity user) {
        Set<LivingEntity> targets = super.perform(attacker, user);

        final Vec3 rotVec = user.getLookAngle();
        final Vec3 eyePos = user.position().add(GravityChangerAPI.getEyeOffset(user));
        final HitResult hitResult = JUtils.raycastAll(attacker,
                eyePos,
                eyePos.add(rotVec.scale(16.0)),
                ClipContext.Fluid.NONE);

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            final Entity hitEntity = JUtils.getUserIfStand(((EntityHitResult) hitResult).getEntity());
            JUtils.addVelocity(hitEntity, rotVec.scale(-1.25));
            hitEntity.setOnGround(false);

            if (hitEntity instanceof LivingEntity living) {
                living.addEffect(
                        new MobEffectInstance(MobEffects.LEVITATION, 5, 0, true, false)
                );
            }
        } else {
            JUtils.addVelocity(user, rotVec.scale(1.25));
        }

        return targets;
    }

    @Override
    protected @NonNull EraseSpaceAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull EraseSpaceAttack<A> copy() {
        return copyExtras(new EraseSpaceAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset()));
    }

    public static class Type extends AbstractSimpleAttack.Type<EraseSpaceAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NotNull App<RecordCodecBuilder.Mu<EraseSpaceAttack<?>>, EraseSpaceAttack<?>> buildCodec(RecordCodecBuilder.Instance<EraseSpaceAttack<?>> instance) {
            return attackDefault(instance, EraseSpaceAttack::new);
        }
    }
}
