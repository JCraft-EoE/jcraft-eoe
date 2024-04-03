package net.arna.jcraft.common.attack.moves.killerqueen.bitesthedust;

import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.entity.stand.KQBTDEntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Set;

public class BTDPlantAttack extends AbstractSimpleAttack<BTDPlantAttack, KQBTDEntity> {
    public static final MoveVariable<LivingEntity> BTD_ENTITY = new MoveVariable<>(LivingEntity.class);
    public static final MoveVariable<Vec3d> BTD_POS = new MoveVariable<>(Vec3d.class);

    public BTDPlantAttack(int cooldown, int windup, int duration, float attackDistance, int stun, float hitboxSize, float offset) {
        super(cooldown, windup, duration, attackDistance, 0f, stun, hitboxSize, 0f, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(KQBTDEntity attacker, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(attacker, user, ctx);
        if (targets.isEmpty()) return Set.of();

        Entity btdEntity = JUtils.getUserIfStand(targets.stream().findFirst().orElseThrow());
        if (btdEntity instanceof LivingEntity living) {
            ctx.set(BTD_ENTITY, living);
            ctx.set(BTD_POS, btdEntity.getPos());
        }
        return targets;
    }

    public void tickBomb(KQBTDEntity stand) {
        if (stand.getUser() instanceof ServerPlayerEntity player)
            displayBTDParticles(stand, player);
    }

    private void displayBTDParticles(KQBTDEntity stand, ServerPlayerEntity playerEntity) {
        Entity bombEntity = stand.getMoveContext().get(BTD_ENTITY);
        Vec3d bombPos = stand.getMoveContext().get(BTD_POS);
        boolean bombExists = bombEntity != null;

        double dX1 = 0;
        double dY1 = 0;
        double dZ1 = 0;
        double dX2 = 0;
        double dY2 = 0;
        double dZ2 = 0;

        Box bBox = null;

        if (bombEntity != null) { // If the bomb isn't a block
            dX1 = bombEntity.getX();
            dY1 = bombEntity.getY();
            dZ1 = bombEntity.getZ();

            bBox = bombEntity.getBoundingBox();

            dX2 = bBox.getXLength();
            dY2 = bBox.getYLength();
            dZ2 = bBox.getZLength();
        }

        if (!bombExists) return;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(9);

        buf.writeDouble(dX1);
        buf.writeDouble(dY1);
        buf.writeDouble(dZ1);
        buf.writeDouble(dX2);
        buf.writeDouble(dY2);
        buf.writeDouble(dZ2);

        buf.writeDouble(bombPos.x);
        buf.writeDouble(bombPos.y);
        buf.writeDouble(bombPos.z);

        boolean anyInRange = false;
        Vec3d pos = bombEntity.getPos();
        Vec3d v1 = pos.add(3, 3, 3);
        Vec3d v2 = pos.add(-3, -3, -3);
        List<LivingEntity> list = stand.getWorld().getEntitiesByClass(LivingEntity.class, new Box(v1, v2),
                EntityPredicates.VALID_LIVING_ENTITY.and(e -> e != bombEntity));
        for (LivingEntity l : list)
            if (l.squaredDistanceTo(pos) < 9) {
                anyInRange = true;
                break;
            }

        buf.writeBoolean(anyInRange);

        if (bBox.getAverageSideLength() > 0)
            ServerChannelFeedbackPacket.send(playerEntity, buf);
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(BTD_ENTITY);
        ctx.register(BTD_POS);
    }

    @Override
    protected @NonNull BTDPlantAttack getThis() {
        return this;
    }

    @Override
    public @NonNull BTDPlantAttack copy() {
        return copyExtras(new BTDPlantAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getStun(), getHitboxSize(),
                getOffset()));
    }
}
