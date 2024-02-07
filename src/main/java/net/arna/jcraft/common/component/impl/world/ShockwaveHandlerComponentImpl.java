package net.arna.jcraft.common.component.impl.world;

import lombok.Getter;
import net.arna.jcraft.common.component.JComponents;
import net.arna.jcraft.common.component.world.ShockwaveHandlerComponent;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShockwaveHandlerComponentImpl implements ShockwaveHandlerComponent {
    @Getter
    private final List<Shockwave> shockwaves = new ArrayList<>();
    private final World world;

    public ShockwaveHandlerComponentImpl(World world) {
        this.world = world;
    }

    @Override
    public void addShockwave(double x, double y, double z, float pitch, float yaw, float scale) {
        Shockwave shockwave = new Shockwave(x, y, z, pitch, yaw, scale);
        shockwaves.add(shockwave);
        JComponents.SHOCKWAVE_HANDLER.sync(world, (buf, player) -> writeSyncPacket(buf, shockwave));
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound tag) {
        for (NbtElement element : tag.getList("shockwaves", NbtElement.COMPOUND_TYPE)) {
            NbtCompound compound = (NbtCompound) element;
            shockwaves.add(new Shockwave(
                    compound.getDouble("x"),
                    compound.getDouble("y"),
                    compound.getDouble("z"),
                    compound.getFloat("pitch"),
                    compound.getFloat("yaw"),
                    compound.getFloat("scale"),
                    compound.getInt("age")
            ));
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound tag) {
        NbtList list = new NbtList();
        for (Shockwave shockwave : shockwaves) {
            NbtCompound compound = new NbtCompound();
            compound.putDouble("x", shockwave.getX());
            compound.putDouble("y", shockwave.getY());
            compound.putDouble("z", shockwave.getZ());
            compound.putFloat("pitch", shockwave.getPitch());
            compound.putFloat("yaw", shockwave.getYaw());
            compound.putFloat("scale", shockwave.getScale());
            compound.putInt("age", shockwave.getAge());
            list.add(compound);
        }
        tag.put("shockwaves", list);
    }

    @Override
    public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
        writeSyncPacket(buf, (Shockwave) null);
    }

    private void writeSyncPacket(PacketByteBuf buf, @Nullable Shockwave shockwave) {
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
        }
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        int count = buf.readInt();
        for (int i = 0; i < count; i++)
            shockwaves.add(new Shockwave(
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readFloat(),
                    buf.readInt()
            ));
    }

    @Override
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
