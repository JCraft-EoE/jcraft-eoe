package net.arna.jcraft.common.attack.moves.thesun;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractBarrageAttack;
import net.arna.jcraft.common.entity.stand.TheSunEntity;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public class MeteorShowerAttack extends AbstractBarrageAttack<MeteorShowerAttack, TheSunEntity> {
    public MeteorShowerAttack(int cooldown, int windup, int duration, int interval) {
        super(cooldown, windup, duration, 0, 0, 0, 0, 0, 0, interval);
        ranged = true;
    }

    @Override
    public @NonNull MoveType<MeteorShowerAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TheSunEntity attacker, LivingEntity user) {
        Set<LivingEntity> targets = super.perform(attacker, user);

        for (int i = 0; i < attacker.getRawScale() * 1.5; i++) {
            FireMeteorAttack.fireMeteor(attacker, user, attacker.randomPos(),
                    JUtils.randUnitVec(attacker.getRandom()), 1.25f, 0f);
        }

        return targets;
    }

    @Override
    protected @NonNull MeteorShowerAttack getThis() {
        return this;
    }

    @Override
    public @NonNull MeteorShowerAttack copy() {
        return copyExtras(new MeteorShowerAttack(getCooldown(), getWindup(), getDuration(), getInterval()));
    }

    public static class Type extends AbstractBarrageAttack.Type<MeteorShowerAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<MeteorShowerAttack>, MeteorShowerAttack> buildCodec(RecordCodecBuilder.Instance<MeteorShowerAttack> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), windup(), duration(), interval())
                    .apply(instance, applyAttackExtras(MeteorShowerAttack::new));
        }
    }
}
