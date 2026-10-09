package net.arna.jcraft.common.component.impl.world;

import lombok.Getter;
import lombok.NonNull;
import net.arna.jcraft.api.component.world.CommonShockwaveHandlerComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class CommonShockwaveHandlerComponentImpl implements CommonShockwaveHandlerComponent {
    @Getter
    private final List<Shockwave> shockwaves = new ArrayList<>();
    protected final Level world;

    protected CommonShockwaveHandlerComponentImpl(final Level world) {
        this.world = world;
    }

    @Override
    public void addShockwave(final double x, final double y, final double z, final float pitch, final float yaw, final float scale, final Shockwave.Type type) {
        Shockwave shockwave = new Shockwave(x, y, z, pitch, yaw, scale, type);
        shockwaves.add(shockwave);
        sync(shockwave);
    }

    public void sync(final Shockwave shockwave) {
        // JComponentPlatformUtils.SHOCKWAVE_HANDLER.sync(world, (buf, player) -> writeSyncPacket(buf, shockwave));
    }

    public void readFromNbt(final @NonNull CompoundTag tag) {
        for (Tag element : tag.getList("shockwaves", Tag.TAG_COMPOUND)) {
            CompoundTag compound = (CompoundTag) element;
            shockwaves.add(new Shockwave(
                    compound.getDouble("x"),
                    compound.getDouble("y"),
                    compound.getDouble("z"),
                    compound.getFloat("pitch"),
                    compound.getFloat("yaw"),
                    compound.getFloat("scale"),
                    compound.getInt("age"),
                    Shockwave.Type.of(compound.getInt("type"))
            ));
        }
    }

    public void writeToNbt(final @NonNull CompoundTag tag) {
        ListTag list = new ListTag();
        for (Shockwave shockwave : shockwaves) {
            CompoundTag compound = new CompoundTag();
            compound.putDouble("x", shockwave.getX());
            compound.putDouble("y", shockwave.getY());
            compound.putDouble("z", shockwave.getZ());
            compound.putFloat("pitch", shockwave.getPitch());
            compound.putFloat("yaw", shockwave.getYaw());
            compound.putFloat("scale", shockwave.getScale());
            compound.putInt("age", shockwave.getAge());
            compound.putInt("type", shockwave.getType().getId());
            list.add(compound);
        }
        tag.put("shockwaves", list);
    }

    public void writeSyncPacket(final FriendlyByteBuf buf, final ServerPlayer recipient) {
        writeSyncPacket(buf, (Shockwave) null);
    }

    public void writeSyncPacket(final FriendlyByteBuf buf, final @Nullable Shockwave shockwave) {
        List<Shockwave> shockwaves = shockwave == null ? this.shockwaves : List.of(shockwave);

        buf.writeInt(shockwaves.size());
        for (Shockwave sw : shockwaves) {
            buf.writeDouble(sw.getX());
            buf.writeDouble(sw.getY());
            buf.writeDouble(sw.getZ());
            buf.writeFloat(sw.getPitch());
            buf.writeFloat(sw.getYaw());
            buf.writeFloat(sw.getScale());
            buf.writeInt(sw.getAge());
            buf.writeInt(sw.getType().getId());
        }
    }

    public void applySyncPacket(FriendlyByteBuf buf) {
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            shockwaves.add(new Shockwave(
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readInt(),
                    Shockwave.Type.of(buf.readInt())
            ));
        }
    }

    public void tick() {
        for (int i = 0; i < shockwaves.size(); i++) {
            Shockwave shockwave = shockwaves.get(i);
            shockwave.tick();
            if (shockwave.getAge() >= Shockwave.MAX_AGE) {
                shockwaves.remove(i);
                i--;
            }
        }
    }
}
