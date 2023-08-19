package net.arna.jcraft.common.attack.moves.killerqueen;

import net.arna.jcraft.common.attack.moves.base.AbstractSimpleAttack;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.entity.stand.AbstractKillerQueenEntity;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BombPlantAttack extends AbstractSimpleAttack<BombPlantAttack, AbstractKillerQueenEntity<?, ?>> {
    public static final MoveVariable<Entity> BOMB_ENTITY = new MoveVariable<>(Entity.class);
    public static final MoveVariable<Vec3d> BOMB_POS = new MoveVariable<>(Vec3d.class);

    public BombPlantAttack(int cooldown, int windup, int duration, float attackDistance, int stun, float hitboxSize, float offset) {
        super(cooldown, windup, duration, attackDistance, 0f, stun, hitboxSize, 0f, offset);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(AbstractKillerQueenEntity<?, ?> stand, LivingEntity user, MoveContext ctx) {
        Set<LivingEntity> targets = super.perform(stand, user, ctx);
        Entity target = targets.stream()
                .findFirst()
                .<Entity>map(JUtils::getUserIfStand)
                .or(() -> {
                    // If none are found, re-do an optimized hitbox check for any entity type
                    Vec3d rotVec = getRotVec(stand);
                    Vec3d boxCenter = stand.getPos().add(0, user.getHeight() / 2, 0).add(rotVec);
                    Vec3d halfBox = new Vec3d(0.5, 0.5, 0.5);
                    List<Entity> hit = stand.world.getEntitiesByClass(Entity.class,
                            new Box(boxCenter.subtract(halfBox), boxCenter.add(halfBox)),
                            EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != stand && e != user));
                    return !hit.isEmpty() ? Optional.of(hit.get(0)) : Optional.empty();
                })
                .orElse(null);

        if (target != null) {
            ctx.set(BOMB_ENTITY, target);
            ctx.set(BOMB_POS, null);
        }

        return targets;
    }

    public void tickBomb(AbstractKillerQueenEntity<?, ?> stand) {
        // TODO might as well make this only happen on the client so you don't have to send a packet.
        if (!stand.world.isClient && stand.getUser() instanceof ServerPlayerEntity player)
            displayBombParticles(stand, player);
    }

    private void displayBombParticles(AbstractKillerQueenEntity<?, ?> stand, ServerPlayerEntity playerEntity) {
        Entity bombEntity = stand.getMoveContext().get(BOMB_ENTITY);
        Vec3d bombPos = stand.getMoveContext().get(BOMB_POS);

        boolean bombIsBlock = bombPos != null;
        boolean bombExists = bombEntity != null || bombIsBlock;

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
        } else if (bombIsBlock) { // If the bomb is a block
            dX1 = bombPos.getX();
            dY1 = bombPos.getY();
            dZ1 = bombPos.getZ();

            dX2 = dY2 = dZ2 = 1.41;
        }

        if (bombExists) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(4);

            buf.writeDouble(dX1);
            buf.writeDouble(dY1);
            buf.writeDouble(dZ1);

            buf.writeDouble(dX2);
            buf.writeDouble(dY2);
            buf.writeDouble(dZ2);

            boolean anyInRange = false;
            Vec3d bPos = stand.getBombPos();
            Vec3d v1 = bPos.add(3, 3, 3);
            Vec3d v2 = bPos.add(-3, -3, -3);
            List<LivingEntity> list = stand.world.getEntitiesByClass(LivingEntity.class, new Box(v1, v2), EntityPredicates.VALID_LIVING_ENTITY);
            if (bombEntity instanceof LivingEntity) list.remove(bombEntity);
            for (LivingEntity l : list)
                if (l.squaredDistanceTo(bPos) < 9) {
                    anyInRange = true;
                    break;
                }

            buf.writeBoolean(anyInRange);

            if ((bBox != null && bBox.getAverageSideLength() > 0) || bombIsBlock)
                ServerChannelFeedbackPacket.send(playerEntity, buf);
        }
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(BOMB_ENTITY);
        ctx.register(BOMB_POS);
    }

    @Override
    protected BombPlantAttack getThis() {
        return this;
    }

    @Override
    public BombPlantAttack copy() {
        return new BombPlantAttack(getCooldown(), getWindup(), getDuration(), getMoveDistance(), getStun(), getHitboxSize(),
                getOffset());
    }
}
