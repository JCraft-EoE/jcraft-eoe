package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractSimpleAttack;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

import static net.arna.jcraft.api.Attacks.damageLogic;

public final class BoilingPointAttack extends AbstractSimpleAttack<BoilingPointAttack, SpeedKingEntity> {
    private static final int BOILING_DURATION = 400;

    public BoilingPointAttack(final int cooldown, final int windup, final int duration, final float moveDistance,
                               final float damage, final int stun, final float hitboxSize) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, 0f, 0f);
    }

    @Override
    public @NonNull MoveType<BoilingPointAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final SpeedKingEntity attacker, final LivingEntity user) {
        if (attacker.level().isClientSide()) return Set.of();

        Vec3 look = user.getLookAngle();
        Vec3 target = user.position().add(look.x * getMoveDistance(), 0, look.z * getMoveDistance());

        final DamageSource damageSource = JDamageSources.stand(attacker);
        final double r = getHitboxSize() / 2.0;
        final Set<LivingEntity> targets = AbstractSimpleAttack.findHits(attacker,
                new net.minecraft.world.phys.AABB(
                        target.add(-r, -1, -r),
                        target.add(r, 3, r)),
                damageSource);

        for (LivingEntity entity : targets) {
            final Vec3 kbVec = entity.getEyePosition().subtract(target).normalize();
            damageLogic(attacker.level(), entity, kbVec, getStun(), 1, true, getDamage(), false, 4, damageSource, user, null);
            entity.addEffect(new MobEffectInstance(JStatusRegistry.BOILING.get(), BOILING_DURATION, 0, false, true));
            HeatTrapManager.addHeat(entity, user);
            HeatTrapManager.addHeat(entity, user);
        }

        return targets;
    }

    @Override
    protected @NonNull BoilingPointAttack getThis() { return this; }

    @Override
    public @NonNull BoilingPointAttack copy() {
        return copyExtras(new BoilingPointAttack(getCooldown(), getWindup(), getDuration(),
                getMoveDistance(), getDamage(), getStun(), getHitboxSize()));
    }

    public static class Type extends AbstractSimpleAttack.Type<BoilingPointAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<BoilingPointAttack>, BoilingPointAttack> buildCodec(RecordCodecBuilder.Instance<BoilingPointAttack> instance) {
            return attackDefault(instance, (cd, wu, dur, md, dmg, st, hb, kb, off) ->
                    new BoilingPointAttack(cd, wu, dur, md, dmg, st, hb));
        }
    }
}
