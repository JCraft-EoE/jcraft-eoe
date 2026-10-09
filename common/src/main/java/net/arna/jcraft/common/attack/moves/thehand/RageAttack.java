package net.arna.jcraft.common.attack.moves.thehand;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntSet;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.enums.BlockableType;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMultiHitAttack;
import net.arna.jcraft.common.attack.moves.shared.BarrageAttack;
import net.arna.jcraft.common.entity.damage.JDamageSources;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.TheHandEntity;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

import static net.arna.jcraft.api.Attacks.*;

public final class RageAttack extends AbstractMultiHitAttack<RageAttack, TheHandEntity> {
    public static final SimpleEraseAttack<TheHandEntity> RAGE_FINISHER = new SimpleEraseAttack<TheHandEntity>(0,
            41, 50, 0.74f, 6.0f, 12, 2.0f, 2.0f, 0.0f)
            .withSound(JSoundRegistry.D4C_LIGHT)
            .withImpactSound(JSoundRegistry.IMPACT_12)
            .withImpactSound(JSoundRegistry.TW_KICK_HIT)
            .withExtraHitBox(1.5)
            .withInfo(
                    Component.literal("Rage (Finisher)"),
                    Component.empty()
            )
            .withLaunch();

    public static final BarrageAttack<TheHandEntity> RAGE_FOLLOWUP = new BarrageAttack<TheHandEntity>(0,
            9, 50, 0.75f, 1.0f, 13, 1.75f, 0.2f, 0.0f, 2)
            .withImpactSound(JSoundRegistry.IMPACT_1)
            .withInfo(
                    Component.literal("Rage (Barrage)"),
                    Component.empty()
            )
            .withFinisher(29, RAGE_FINISHER);

    public RageAttack(final int cooldown, final int duration, final float moveDistance, final float damage, final int stun,
                      final float hitboxSize, final float knockback, final float offset, final IntSet hitMoments) {
        super(cooldown, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitMoments);
        withBlockableType(BlockableType.NON_BLOCKABLE);
        withHitSpark(JParticleType.INVERTED_HIT_SPARK_3);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TheHandEntity attacker, LivingEntity user) {
        Set<LivingEntity> targets = super.perform(attacker, user);

        if (targets.isEmpty() || getBlow(attacker) != 1) return targets;

        attacker.setMove(RAGE_FOLLOWUP, TheHandEntity.State.RAGE_FOLLOWUP);

        return targets;
    }

    @Override
    protected void processTarget(final TheHandEntity attacker, final LivingEntity target, final Vec3 kbVec, final DamageSource damageSource) {
        damageLogic(attacker.getEntityWorld(), target, kbVec, getStun(), getStunType().ordinal(), true,
                0, isLift(), getBlockStun(), damageSource, attacker.getUserOrThrow(), getHitAnimation(), true, false);

        target.removeEffect(JStatusRegistry.DAZED.get());
        StandEntity<?, ?> stand = JUtils.getStand(target);
        if (stand != null) stand.blocking = false;
        JCraft.stun(target, getStun(), 0, attacker);
        trueDamage(getDamage(), JDamageSources.stand(attacker), target);
    }

    @Override
    public @NonNull MoveType<RageAttack> getMoveType() {
        return Type.INSTANCE;
    }

    @Override
    protected @NonNull RageAttack getThis() {
        return this;
    }

    @Override
    public @NonNull RageAttack copy() {
        return copyExtras(new RageAttack(getCooldown(), getDuration(), getMoveDistance(),
                getDamage(), getStun(), getHitboxSize(), getKnockback(), getOffset(), getHitMoments()));
    }

    public static class Type extends AbstractMultiHitAttack.Type<RageAttack> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<RageAttack>, RageAttack> buildCodec(RecordCodecBuilder.Instance<RageAttack> instance) {
            return multiHitDefault(instance, RageAttack::new);
        }
    }
}
