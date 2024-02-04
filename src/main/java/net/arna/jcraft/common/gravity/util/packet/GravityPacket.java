package net.arna.jcraft.common.gravity.util.packet;

import net.arna.jcraft.common.gravity.api.RotationParameters;
import net.arna.jcraft.common.component.entity.GravityComponent;
import net.minecraft.network.PacketByteBuf;

public abstract class GravityPacket {
    public abstract void write(PacketByteBuf buf);

    public abstract void run(GravityComponent gc);

    public abstract RotationParameters getRotationParameters();
}
