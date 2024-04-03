package net.arna.jcraft.common.attack.moves.hierophantgreen;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.projectile.HGNetEntity;
import net.arna.jcraft.common.entity.stand.HGEntity;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.Gravity;
import net.arna.jcraft.registry.JEntityTypeRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Direction;

import java.util.Set;

public class NetSetMove extends AbstractMove<NetSetMove, HGEntity> {
    public NetSetMove(int cooldown, int windup, int duration, float attackDistance) {
        super(cooldown, windup, duration, attackDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(HGEntity attacker, LivingEntity user, MoveContext ctx) {
        Direction gravity = GravityChangerAPI.getGravityDirection(attacker);

        HGNetEntity net = new HGNetEntity(JEntityTypeRegistry.HG_NET, attacker.getWorld());
        net.setSkin(attacker.getSkin());
        net.refreshPositionAndAngles(
                attacker.getX() + gravity.getOffsetX(),
                attacker.getY() + gravity.getOffsetY(),
                attacker.getZ() + gravity.getOffsetZ(),
                attacker.getRandom().nextFloat() * 360f,
                attacker.getRandom().nextFloat() * 360f);
        net.setMaster(user);

        attacker.getWorld().spawnEntity(net);

        GravityChangerAPI.addGravity(net,
                new Gravity(gravity, 0, 32767, "_spawn")
        );

        return Set.of();
    }

    @Override
    protected @NonNull NetSetMove getThis() {
        return this;
    }

    @Override
    public @NonNull NetSetMove copy() {
        return copyExtras(new NetSetMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }
}