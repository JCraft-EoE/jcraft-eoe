package net.arna.jcraft.common.attack.moves.shared;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import lombok.NonNull;
import net.arna.jcraft.api.attack.IAttacker;
import net.arna.jcraft.api.attack.MoveType;
import net.arna.jcraft.api.attack.moves.AbstractMove;
import net.arna.jcraft.api.registry.JPacketRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

public class GoldenRatioMove<A extends IAttacker<? extends A, ?>> extends AbstractMove<GoldenRatioMove<A>, A> {

    public GoldenRatioMove(final int cooldown, final int windup) {
        super(cooldown, windup, 0, 0);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(final A attacker, final LivingEntity user) {
        if (user instanceof ServerPlayer player) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            NetworkManager.sendToPlayer(player, JPacketRegistry.S2C_SPIRAL_CALL, buf);
        }
        else {
            // TODO Arna handle effects for NPCs
        }
        return Set.of();
    }

    @Override
    protected @NonNull GoldenRatioMove<A> getThis() {
        return this;
    }

    @Override
    public @NonNull GoldenRatioMove<A> copy() {
        return copyExtras(new GoldenRatioMove<>(getCooldown(), getWindup()));
    }

    @Override
    public @NonNull MoveType<GoldenRatioMove<A>> getMoveType() {
        return Type.INSTANCE.cast();
    }

    public static class Type extends AbstractMove.Type<GoldenRatioMove<?>> {

        public static final Type INSTANCE = new Type();

        @Override
        protected @NonNull App<RecordCodecBuilder.Mu<GoldenRatioMove<?>>, GoldenRatioMove<?>> buildCodec(final RecordCodecBuilder.Instance<GoldenRatioMove<?>> instance) {
            return instance.group(extras(), cooldown(), windup()).apply(instance, applyExtras(GoldenRatioMove::new));
        }

    }

}
