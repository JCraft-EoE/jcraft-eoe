package net.arna.jcraft.common.component.impl.living;

import lombok.NonNull;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.component.living.CommonBombTrackerComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public abstract class CommonBombTrackerComponentImpl implements CommonBombTrackerComponent {
    private final Entity entity;
    public final BombData main = new BombData();
    public final BombData btd = new BombData();

    protected CommonBombTrackerComponentImpl(final @NonNull Entity entity) {
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

    public void tick() {
        final Level world = entity.level();

        if (world.isClientSide) {
            int id = getMainBomb().bombEntityID;
            if (getMainBomb().bombEntity == null && id != -1) {
                Entity entity = world.getEntity(id);
                // Direct assignment due to other values having been set before
                // All other cases should use .setBomb(Entity entity)
                if (entity != null) getMainBomb().bombEntity = entity;
            }
            JCraft.getClientEntityHandler().bombTrackerParticleTick(entity, main);
        } else {
            if (main.dirty) {
                sync(entity);
            }
            /*
            if (btd.dirty)
                sync();
             */
        }
    }

    public void sync(final Entity entity) {
        //JComponentPlatformUtils.BOMB_TRACKER.sync(entity);
        main.dirty = false;
        btd.dirty = false;
    }

    public boolean shouldSyncWith(final ServerPlayer player) {
        return player == entity;
    }

    public void syncBombData(final FriendlyByteBuf buf, final BombData bombData) {
        buf.writeBoolean(bombData.isBlock);
        buf.writeBoolean(bombData.isEntity);
        buf.writeBoolean(bombData.isItem);
        if (bombData.isEntity) {
            buf.writeVarInt(bombData.bombEntity.getId());
        }
        if (bombData.isBlock) {
            buf.writeBlockPos(bombData.bombBlock);
        }
    }

    public void writeSyncPacket(final FriendlyByteBuf buf, final ServerPlayer recipient) {
        syncBombData(buf, main);
        //syncBombData(buf, btd);
    }

    public void readBombData(final FriendlyByteBuf buf, final BombData bombData) {
        bombData.isBlock = buf.readBoolean();
        bombData.isEntity = buf.readBoolean();
        bombData.isItem = buf.readBoolean();
        if (bombData.isEntity) {
            int id = buf.readVarInt();
            bombData.bombEntityID = id;
            bombData.bombEntity = entity.level().getEntity(id);
        }
        if (bombData.isBlock) {
            bombData.bombBlock = buf.readBlockPos();
        }
    }

    public void applySyncPacket(final FriendlyByteBuf buf) {
        readBombData(buf, main);
        //readBombData(buf, btd);
    }


    public void readFromNbt(final @NonNull CompoundTag tag) {
    }

    public void writeToNbt(final @NonNull CompoundTag tag) {
    }
}
