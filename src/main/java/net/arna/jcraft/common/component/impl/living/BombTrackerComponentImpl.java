package net.arna.jcraft.common.component.impl.living;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.component.living.BombTrackerComponent;
import net.arna.jcraft.common.component.JComponents;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BombTrackerComponentImpl implements BombTrackerComponent {
    private final Entity entity;
    private final BombData main = new BombData();
    private final BombData btd = new BombData();

    public BombTrackerComponentImpl(@NotNull Entity entity) {
        this.entity = entity;
    }

    @Override
    public BombData getMainBomb() {
        return main;
    }

    @Deprecated
    @Override
    public BombData getBTD() {
        return btd;
    }

    @Override
    public void tick() {
        World world = entity.getWorld();

        if (world.isClient) {
            JCraft.getClientEntityHandler().bombTrackerParticleTick(entity, main);
        } else {
            if (main.dirty)
                sync();
            /*
            if (btd.dirty)
                sync();
             */
        }
    }

    private void sync() {
        JComponents.BOMB_TRACKER.sync(entity);
        main.dirty = false;
        btd.dirty = false;
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == entity;
    }

    private void syncBombData(PacketByteBuf buf, BombData bombData) {
        buf.writeBoolean(bombData.isBlock);
        buf.writeBoolean(bombData.isEntity);
        buf.writeBoolean(bombData.isItem);
        if (bombData.isEntity)
            buf.writeVarInt(bombData.bombEntity.getId());
        if (bombData.isBlock)
            buf.writeBlockPos(bombData.bombBlock);
    }
    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        syncBombData(buf, main);
        //syncBombData(buf, btd);
    }

    private void readBombData(PacketByteBuf buf, BombData bombData) {
        bombData.isBlock = buf.readBoolean();
        bombData.isEntity = buf.readBoolean();
        bombData.isItem = buf.readBoolean();
        if (bombData.isEntity)
            bombData.bombEntity = entity.getWorld().getEntityById(buf.readVarInt());
        if (bombData.isBlock)
            bombData.bombBlock = buf.readBlockPos();
    }
    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        readBombData(buf, main);
        //readBombData(buf, btd);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) { }
    @Override
    public void writeToNbt(@NotNull NbtCompound tag) { }
}
