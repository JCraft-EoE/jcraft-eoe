package net.arna.jcraft.common.attack.moves.aerosmith;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractHitscanAttack;
import net.arna.jcraft.common.util.JParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class MuzzleHitscanAttack<A extends IAttacker<? extends A, ?>> extends AbstractHitscanAttack<MuzzleHitscanAttack<A>, A> {

    public MuzzleHitscanAttack(final int cooldown, final int windup, final int duration, final float moveDistance, final float damage, final int stun, final float knockback, final float range, final float hardness, final float breakChance, final float spread) {
        super(cooldown, windup, duration, moveDistance, damage, stun, knockback, range, hardness, breakChance, spread);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        final Set<LivingEntity> targets = super.perform(attacker, user);
        if (user == null) {
            return targets;
        }
        final Vec3 eyes = attacker.getBaseEntity().getEyePosition();
        JCraft.createParticle((ServerLevel)user.level(),
                eyes.x(),
                eyes.y(),
                eyes.z(),
                JParticleType.HIT_SPARK_1);
        return targets;
    }

    @Override
    public @NonNull MoveType<MuzzleHitscanAttack<A>> getMoveType() {
        return MuzzleHitscanAttack.Type.INSTANCE.cast();
    }

    @Override
    protected @NonNull MuzzleHitscanAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull MuzzleHitscanAttack<A> copy() {
        return copyExtras(new MuzzleHitscanAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(), getStun(),
                getKnockback(), getRange(), getHardness(), getBreakChance(), getSpread()));
    }

    public static class Type extends AbstractHitscanAttack.Type<MuzzleHitscanAttack<?>> {
        public static final MuzzleHitscanAttack.Type INSTANCE = new MuzzleHitscanAttack.Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<MuzzleHitscanAttack<?>>, MuzzleHitscanAttack<?>> buildCodec(RecordCodecBuilder.Instance<MuzzleHitscanAttack<?>> instance) {
            return hitscanDefault(instance, MuzzleHitscanAttack::new);
        }
    }
}
