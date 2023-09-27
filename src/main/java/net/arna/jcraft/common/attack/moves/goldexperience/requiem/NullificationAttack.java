package net.arna.jcraft.common.attack.moves.goldexperience.requiem;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.attack.moves.base.AbstractCounterAttack;
import net.arna.jcraft.common.attack.moves.shared.CounterMissMove;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.entity.stand.GEREntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.JParticleType;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public class NullificationAttack extends AbstractCounterAttack<NullificationAttack, GEREntity> {
    private static final int counterStopTime = 20; // Convenience
    private final CounterMissMove<GEREntity> counterMiss = new CounterMissMove<>(20);

    public NullificationAttack(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public void whiff(@NonNull GEREntity attacker, @NonNull LivingEntity user) {
        attacker.setMove(counterMiss, GEREntity.State.COUNTER_MISS);
        StandEntity.stun(attacker.getUser(), counterMiss.getDuration(), 0);
    }

    @Override
    public void counter(@NonNull GEREntity attacker, Entity countered, DamageSource counteredDamageSource) {
        super.counter(attacker, countered, counteredDamageSource);

        if (countered == null || !attacker.hasUser()) return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(14);
        buf.writeInt(countered.getId());
        buf.writeInt(counterStopTime);
        for (PlayerEntity sendPlayer : attacker.getWorld().getPlayers())
            if (sendPlayer instanceof ServerPlayerEntity serverPlayerEntity)
                ServerChannelFeedbackPacket.send(serverPlayerEntity, buf);
        JComponents.getTimeStopData(countered).setTicks(counterStopTime);

        if (countered instanceof LivingEntity living) {
            StandEntity.stun(living, 10, 0);
            JUtils.cancelMoves(living);
        }

        Vec3d eP = attacker.getEyePos();
        JCraft.createParticle((ServerWorld) attacker.getWorld(), eP.x, eP.y, eP.z, JParticleType.FLASH);
    }

    @Override
    protected @NonNull NullificationAttack getThis() {
        return this;
    }

    @Override
    public @NonNull NullificationAttack copy() {
        return copyExtras(new NullificationAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}
