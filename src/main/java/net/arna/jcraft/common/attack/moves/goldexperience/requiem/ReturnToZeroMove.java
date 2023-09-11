package net.arna.jcraft.common.attack.moves.goldexperience.requiem;

import com.google.common.reflect.TypeToken;
import lombok.Data;
import lombok.NonNull;
import net.arna.jcraft.common.attack.core.ctx.MoveContext;
import net.arna.jcraft.common.attack.core.ctx.MoveVariable;
import net.arna.jcraft.common.attack.moves.base.AbstractMove;
import net.arna.jcraft.common.entity.stand.GEREntity;
import net.arna.jcraft.common.network.s2c.ServerChannelFeedbackPacket;
import net.arna.jcraft.registry.JSoundRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class ReturnToZeroMove extends AbstractMove<ReturnToZeroMove, GEREntity> {
    public static final MoveVariable<Map<Entity, NbtCompound>> ENTITY_DATA = new MoveVariable<>(new TypeToken<>() {});
    public static final MoveVariable<List<ReturnData>> RETURN_INFO = new MoveVariable<>(new TypeToken<>() {});

    public ReturnToZeroMove(int cooldown, int windup, int duration, float moveDistance) {
        super(cooldown, windup, duration, moveDistance);
    }

    @Override
    public @NonNull Set<LivingEntity> perform(GEREntity attacker, LivingEntity user, MoveContext ctx) {
        List<Entity> toReturn = attacker.getWorld().getEntitiesByClass(Entity.class, attacker.getBoundingBox().expand(64),
                EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.and(e -> e != attacker && e != user));

        Map<Entity, NbtCompound> entityData = ctx.get(ENTITY_DATA);
        List<ReturnData> returnInfo = ctx.get(RETURN_INFO);

        for (Entity e : toReturn) {
            NbtCompound data = new NbtCompound();
            e.writeNbt(data);
            entityData.put(e, data);
            returnInfo.add(new ReturnData(e.getEyePos(), e));
        }

        return Set.of();
    }

    public void returnToZero(GEREntity attacker) {
        MoveContext ctx = attacker.getMoveContext();
        Map<Entity, NbtCompound> entityData = ctx.get(ENTITY_DATA);
        List<ReturnData> returnInfo = ctx.get(RETURN_INFO);

        for (Map.Entry<Entity, NbtCompound> data : entityData.entrySet()) {
            Entity ent = data.getKey();
            if (!ent.isAlive()) continue;
            NbtCompound nbt = data.getValue();

            if (ent instanceof PlayerEntity playerEntity) {
                nbt.put("Inventory", playerEntity.getInventory().writeNbt(new NbtList()));
                nbt.put("EnderItems", playerEntity.getEnderChestInventory().toNbtList());

                ServerPlayerEntity serverPlayer = ((ServerPlayerEntity) playerEntity);
                serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(ent));
                NbtList list = nbt.getList("Pos", 6);
                serverPlayer.teleport(list.getDouble(0), list.getDouble(1), list.getDouble(2));
            }

            ent.readNbt(nbt);
        }

        entityData.clear();
        returnInfo.clear();

        attacker.playSound(JSoundRegistry.GER_RTZ, 1, 1);
    }

    public void tickReturnInfo(GEREntity attacker) {
        if (!(attacker.getUser() instanceof ServerPlayerEntity serverPlayer)) return;
        for (ReturnToZeroMove.ReturnData data : attacker.getMoveContext().get(RETURN_INFO)) {
            Entity entity = data.getEntity();
            if (entity == null || !entity.isAlive()) continue;
            Vec3d position = data.getOriginalPos();
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeShort(7);

            buf.writeInt(entity.getId());

            buf.writeDouble(position.getX());
            buf.writeDouble(position.getY());
            buf.writeDouble(position.getZ());

            ServerChannelFeedbackPacket.send(serverPlayer, buf);
        }
    }

    @Override
    public void registerContextEntries(MoveContext ctx) {
        ctx.register(ENTITY_DATA, new WeakHashMap<>());
        ctx.register(RETURN_INFO, new ArrayList<>());
    }

    @Override
    protected @NonNull ReturnToZeroMove getThis() {
        return this;
    }

    @Override
    public @NonNull ReturnToZeroMove copy() {
        return copyExtras(new ReturnToZeroMove(getCooldown(), getWindup(), getDuration(), getMoveDistance()));
    }

    @Data
    public static class ReturnData {
        Vec3d originalPos;
        Entity entity;

        public ReturnData(Vec3d originalPos, Entity entity) {
            this.originalPos = originalPos;
            this.entity = entity;
        }
    }
}
