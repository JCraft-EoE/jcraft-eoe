package net.arna.jcraft.common.attack.moves.speedking;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import lombok.NonNull;
import net.arna.jcraft.api.AttackData;
import net.arna.jcraft.api.Attacks;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.enums.StunType;
import net.arna.jcraft.api.attack.moves.AbstractMultiHitAttack;
import net.arna.jcraft.api.component.living.CommonHitPropertyComponent;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity;
import net.arna.jcraft.common.entity.stand.SpeedKingEntity.State;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class FireGrabHitAttack extends AbstractMultiHitAttack<FireGrabHitAttack, SpeedKingEntity> {
    private final int finalStunMultiplier;
    private final float finalDamageMultiplier;

    public FireGrabHitAttack(final int cooldown, final int duration, final float moveDistance,
                              final float damage, final int stun, final float hitboxSize,
                              final float knockback, final float offset,
                              final @NonNull IntCollection hitMoments,
                              final int finalStunMultiplier, final float finalDamageMultiplier) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
        this.finalStunMultiplier = finalStunMultiplier;
        this.finalDamageMultiplier = finalDamageMultiplier;
    }

    @Override
    protected void processTarget(final SpeedKingEntity attacker, final LivingEntity target,
                                  final Vec3 kbVec, final DamageSource damageSource) {
        IntSortedSet moments = getHitMoments();
        boolean isFinalBlow = getBlow(attacker) == moments.size() - 1;

        if (isFinalBlow) {
            State.FIRE_GRAB.playAnimation(attacker);
            Attacks.damageLogic(attacker.getEntityWorld(), target, new AttackData(
                    kbVec,
                    getStun() * finalStunMultiplier, StunType.LAUNCH.ordinal(), true,
                    getDamage() * finalDamageMultiplier, true, getBlockStun(),
                    damageSource, attacker.getUserOrThrow(),
                    CommonHitPropertyComponent.HitAnimation.LAUNCH,
                    attacker.getMoveUsage(), isCanBackstab(), true
            ));
        } else {
            super.processTarget(attacker, target, kbVec, damageSource);
        }
        HeatTrapManager.addHeat(target, attacker.getUserOrThrow());
    }

    @Override
    public @NonNull MoveType<FireGrabHitAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull FireGrabHitAttack getThis() {
        return this;
    }

    @Override
    public @NonNull FireGrabHitAttack copy() {
        return copyExtras(new FireGrabHitAttack(getCooldown(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), getHitMoments(),
                finalStunMultiplier, finalDamageMultiplier));
    }

    public static class Type extends AbstractMultiHitAttack.Type<FireGrabHitAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<FireGrabHitAttack>, FireGrabHitAttack>
        buildCodec(RecordCodecBuilder.Instance<FireGrabHitAttack> instance) {
            return multiHitDefault(instance, (cd, dur, md, dmg, st, hb, kb, off, moments) ->
                    new FireGrabHitAttack(cd, dur, md, dmg, st, hb, kb, off, moments, 3, 2f));
        }
    }
}
