package net.arna.jcraft.common.attack.moves.shadowtheworld;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.api.attack.moves.AbstractCounterAttack;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.common.attack.moves.shared.CounterMissMove;
import net.arna.jcraft.common.entity.stand.ShadowTheWorldEntity;
import net.arna.jcraft.common.network.s2c.PlayerAnimPacket;
import net.arna.jcraft.api.spec.JSpec;
import net.arna.jcraft.common.util.JUtils;
import net.arna.jcraft.platform.JComponentPlatformUtils;
import net.arna.jcraft.api.registry.JSoundRegistry;
import net.arna.jcraft.api.registry.JStatusRegistry;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class STWCounterAttack<A extends StandEntity<? extends A, ?>> extends AbstractCounterAttack<STWCounterAttack<A>, A> {
    private static final CounterMissMove<ShadowTheWorldEntity> missAttack = new CounterMissMove<>(20);

    public STWCounterAttack(final int cooldown, final int windup, final int duration, final float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull MoveType<STWCounterAttack<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    @Override
    public void onInitiate(final A attacker) {
        super.onInitiate(attacker);
        final LivingEntity user = attacker.getUserOrThrow();
        if (user instanceof ServerPlayer player) {
            final JSpec<?,?> spec = JComponentPlatformUtils.getSpecData(player).getSpec();
            if (spec != null) spec.cancelMove();

            JUtils.around((ServerLevel) player.level(), player.position(), 96).forEach(
                    serverPlayer -> PlayerAnimPacket.send(player, serverPlayer, "stw.cntr"));
        }
        JCraft.stun(user, 20, 0);
    }

    @Override
    public void whiff(final @NonNull A attacker, final @NonNull LivingEntity user) {
        attacker.cancelMove();
        attacker.desummon(false);
        JCraft.stun(user, missAttack.getDuration(), 0);
    }

    @Override
    public void counter(final @NonNull A attacker, final Entity countered, final DamageSource counteredDamageSource) {
        super.counter(attacker, countered, counteredDamageSource);

        if (countered == null || !attacker.hasUser()) {
            return;
        }
        // Teleports behind countered
        final LivingEntity user = attacker.getUserOrThrow();
        final Vec3 behind = countered.position().subtract(countered.getLookAngle());
        final BlockPos behindBlockPos = new BlockPos((int) behind.x, (int) behind.y, (int) behind.z);
        JUtils.setVelocity(user, 0, 0, 0);
        if (!user.level().getBlockState(behindBlockPos).canOcclude()) {
            user.teleportToWithTicket(behind.x, behind.y, behind.z);
        }
        user.lookAt(EntityAnchorArgument.Anchor.EYES, countered.getEyePosition());

        if (countered instanceof LivingEntity livingEntity) {
            livingEntity.removeEffect(JStatusRegistry.DAZED.get());
            JCraft.stun(livingEntity, 20, 0);

            JUtils.cancelMoves(livingEntity);
        }

        attacker.playSound(JSoundRegistry.STW_LAUGH.get(), 1, 1);
        attacker.playSound(JSoundRegistry.STW_ZAP.get(), 1, 1);

        attacker.cancelMove();
        attacker.desummon();
    }

    @Override
    protected @NonNull STWCounterAttack<A> getThis() {
        return this;
    }

    @Override
    public @NonNull STWCounterAttack<A> copy() {
        return copyExtras(new STWCounterAttack<>(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    public static class Type extends AbstractMove.Type<STWCounterAttack<?>> {
        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<STWCounterAttack<?>>, STWCounterAttack<?>> buildCodec(RecordCodecBuilder.Instance<STWCounterAttack<?>> instance) {
            return baseDefault(instance, STWCounterAttack::new);
        }
    }
}
