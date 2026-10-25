package net.arna.jcraft.common.attack.moves.anubis;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.NonNull;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMultiHitAttack;
import net.arna.jcraft.common.spec.AnubisSpec;
import net.minecraft.world.entity.LivingEntity;
import java.util.Set;

public class Rekka3Attack extends AbstractMultiHitAttack<Rekka3Attack, AnubisSpec> {
    public Rekka3Attack(final int cooldown, final int duration, final float moveDistance, final float damage, final int stun, final float hitboxSize,
                        final float knockback, final float offset, final @NonNull IntCollection hitMoments) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
    }

    @Override
    public @NonNull MoveType<Rekka3Attack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final AnubisSpec attacker, final LivingEntity user) {
        if (attacker.getAttackSpeedMult() == 1 && getBlow(attacker) == 1) {
            attacker.curMove = getFollowup();
        }

        return super.perform(attacker, user);
    }

    @Override
    protected @NonNull Rekka3Attack getThis() {
        return this;
    }

    @Override
    public @NonNull Rekka3Attack copy() {
        return copyExtras(new Rekka3Attack(getCooldown(), getDuration(), getMoveDistance(), getDamage(), getStun(), getHitboxSize(),
                getKnockback(), getOffset(), getHitMoments()));
    }

    public static class Type extends AbstractMultiHitAttack.Type<Rekka3Attack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<Rekka3Attack>, Rekka3Attack> buildCodec(RecordCodecBuilder.Instance<Rekka3Attack> instance) {
            return multiHitDefault(instance, Rekka3Attack::new);
        }
    }
}
