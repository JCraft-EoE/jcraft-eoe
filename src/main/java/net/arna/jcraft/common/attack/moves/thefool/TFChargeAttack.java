package net.arna.jcraft.common.attack.moves.thefool;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractChargeAttack;
import net.arna.jcraft.common.entity.stand.TheFoolEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class TFChargeAttack extends AbstractChargeAttack<TFChargeAttack, TheFoolEntity, TheFoolEntity.State> {
    public TFChargeAttack(int cooldown, int windup, int duration, float moveDistance, float damage, int stun,
                          float hitboxSize, float knockback, float offset, TheFoolEntity.State hitAnimState) {
        super(cooldown, windup, duration, moveDistance, damage, stun, hitboxSize, knockback, offset, hitAnimState);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(TheFoolEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        if (targets.isEmpty()) return targets;

        attacker.setSand(true);
        Vec3d pos = attacker.getEyePos();

        // Display sand effect
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeShort(11);
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeDouble(0.5);

        for (ServerPlayerEntity sendPlayer : PlayerLookup.around((ServerWorld) attacker.getWorld(), pos, 96))
            ServerChannelFeedbackPacket.send(sendPlayer, buf);

        return targets;
    }

    @Override
    protected @NonNull TFChargeAttack getThis() {
        return this;
    }

    @Override
    public @NonNull TFChargeAttack copy() {
        return copyExtras(new TFChargeAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getDamage(),
                getStun(), getHitboxSize(), getKnockback(), getOffset(), getHitAnimState()));
    }
}
