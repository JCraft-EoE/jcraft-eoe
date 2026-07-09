package net.arna.jcraft.common.attack.moves.vampire;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMultiHitAttack;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.common.spec.VampireSpec;
import net.arna.jcraft.common.util.JUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.lang.ref.WeakReference;
import java.util.Set;

public class BloodSuckHitsAttack extends AbstractMultiHitAttack<BloodSuckHitsAttack, VampireSpec> {
    @Setter
    private WeakReference<LivingEntity> target;

    @Getter
    private final float bloodGainMult;

    public BloodSuckHitsAttack(int cooldown, int duration, float moveDistance, float damage, int stun, float hitboxSize,
                               float knockback, float offset, @NonNull IntCollection hitMoments, float bloodGainMult) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
        this.bloodGainMult = bloodGainMult;
    }

    @Override
    public @NonNull MoveType<BloodSuckHitsAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(VampireSpec attacker, LivingEntity user) {
        Set<LivingEntity> targets = super.perform(attacker, user);
        LivingEntity target = this.target.get();
        float bloodMult = target == null ? 0 : JUtils.getBloodMult(target);
        if (bloodMult <= 0) return targets;

        user.heal(1);
        attacker.getVampireComponent().setBlood(attacker.getVampireComponent().getBlood() + 2 * bloodMult);
        JUtils.serverPlaySound(JSoundRegistry.VAMPIRE_SUCK.get(), (ServerLevel) user.level(), user.position(), 32);
        return targets;
    }

    @Override
    protected @NonNull BloodSuckHitsAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BloodSuckHitsAttack copy() {
        return copyExtras(new BloodSuckHitsAttack(getCooldown(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getHitboxSize(), getKnockback(), getOffset(), getHitMoments(), getBloodGainMult()));
    }

    public static class Type extends AbstractMultiHitAttack.Type<BloodSuckHitsAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<BloodSuckHitsAttack>, BloodSuckHitsAttack>
        buildCodec(RecordCodecBuilder.Instance<BloodSuckHitsAttack> instance) {
            return instance.group(extras(), attackExtras(), cooldown(), duration(), moveDistance(), damage(),
                            stun(), hitboxSize(), knockback(), offset(), hitMoments(),
                            Codec.FLOAT.fieldOf("blood_gain_mult").forGetter(BloodSuckHitsAttack::getBloodGainMult))
                    .apply(instance, applyAttackExtras(BloodSuckHitsAttack::new));
        }
    }
}
