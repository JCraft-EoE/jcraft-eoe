package net.arna.jcraft.common.gravity.util.packet;

import net.arna.jcraft.common.gravity.api.RotationParameters;
import net.arna.jcraft.common.gravity.util.Gravity;
import net.arna.jcraft.common.component.entity.GravityComponent;
import net.arna.jcraft.common.gravity.util.NetworkUtil;
import net.minecraft.network.PacketByteBuf;

public class UpdateGravityPacket extends GravityPacket {
    public final Gravity gravity;
    public final boolean initialGravity;

    public UpdateGravityPacket(Gravity _gravity, boolean _initialGravity) {
        gravity = _gravity;
        initialGravity = _initialGravity;
    }

    public UpdateGravityPacket(PacketByteBuf buf) {
        this(
                NetworkUtil.readGravity(buf),
                buf.readBoolean()
        );
    }

    @Override
    public void write(PacketByteBuf buf) {
        NetworkUtil.writeGravity(buf, gravity);
        buf.writeBoolean(initialGravity);
    }

    @Override
    public void run(GravityComponent gc) {
        gc.addGravity(gravity, initialGravity);
    }

    @Override
    public RotationParameters getRotationParameters() {
        return gravity.rotationParameters();
    }
}
