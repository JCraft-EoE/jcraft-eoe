package net.arna.jcraft.common.component;

import lombok.Getter;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.gravity.api.GravityChangerAPI;
import net.arna.jcraft.common.gravity.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class GrabComponentImpl implements GrabComponent {
    @Getter
    private final Entity grabbed;
    @Getter
    public Entity attacker = null;
    @Getter
    public int duration = 0;
    private double offset;

    public GrabComponentImpl(Entity grabbed) {
        this.grabbed = grabbed;
    }

    @Override
    public void startGrab(Entity attacker, int duration, double offset) {
        if (attacker == null) {
            JCraft.LOGGER.warn("Null attacker tried to grab: " + grabbed);
            return;
        }

        this.attacker = attacker;
        this.duration = duration;
        this.offset = offset;
        sync();
    }

    @Override
    public void endGrab() {
        this.attacker = null;
        this.duration = 0;
        sync();
    }

    @Override
    public void tick() {
        if (attacker != null)
            if (duration-- > 0) {
                Vec3d newPos = attacker.getPos()
                        .add(RotationUtil.vecPlayerToWorld(new Vec3d(0, 0.4, 0), GravityChangerAPI.getGravityDirection(attacker)))
                        .add(attacker.getRotationVector().multiply(offset));
                if (!attacker.getWorld().isTopSolid(new BlockPos(newPos), grabbed))
                    grabbed.setPosition(newPos);
            } else endGrab();
    }

    public void sync() {
        JComponents.GRAB.sync(grabbed);
    }
    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        // It'll be passively synced in a choppy way for those far away
        return player.squaredDistanceTo(grabbed) <= 6400; // 5 chunks
    }
    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        boolean notGrabbing = attacker == null;
        buf.writeBoolean(notGrabbing);
        if (notGrabbing) return;
        buf.writeVarInt(attacker.getId());
        buf.writeVarInt(duration);
        buf.writeDouble(offset);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        if (buf.readBoolean()) return;
        attacker = grabbed.getWorld().getEntityById(buf.readVarInt());
        duration = buf.readVarInt();
        offset = buf.readDouble();
    }

    @Override
    public void readFromNbt(NbtCompound tag) { }
    @Override
    public void writeToNbt(NbtCompound tag) { }
}
