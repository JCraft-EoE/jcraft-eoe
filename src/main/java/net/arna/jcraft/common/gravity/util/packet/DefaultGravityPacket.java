package net.arna.jcraft.common.gravity.util.packet;

import net.arna.jcraft.common.gravity.api.RotationParameters;
import net.arna.jcraft.common.component.entity.GravityComponent;
import net.arna.jcraft.common.gravity.util.NetworkUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Direction;

public class DefaultGravityPacket extends GravityPacket {
    public final Direction direction;
    public final RotationParameters rotationParameters;
    public final boolean initialGravity;

    public DefaultGravityPacket(Direction _direction, RotationParameters _rotationParameters, boolean _initialGravity) {
        direction = _direction;
        rotationParameters = _rotationParameters;
        initialGravity = _initialGravity;
    }

    public DefaultGravityPacket(PacketByteBuf buf) {
        this(NetworkUtil.readDirection(buf), NetworkUtil.readRotationParameters(buf), buf.readBoolean());
    }

    @Override
    public void write(PacketByteBuf buf) {
        NetworkUtil.writeDirection(buf, direction);
        NetworkUtil.writeRotationParameters(buf, rotationParameters);
        buf.writeBoolean(initialGravity);
    }

    @Override
    public void run(GravityComponent gc) {
        gc.setDefaultGravityDirection(direction, rotationParameters, initialGravity);
    }

    @Override
    public RotationParameters getRotationParameters() {
        return rotationParameters;
    }
}
