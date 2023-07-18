package net.arna.jcraft.client.util;

import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.util.IClientEntityHandler;

public class ClientEntityHandlerImpl implements IClientEntityHandler {
    public static final ClientEntityHandlerImpl INSTANCE = new ClientEntityHandlerImpl();

    private ClientEntityHandlerImpl() {}

    @Override
    public void playerCloneEntityClientTick(PlayerCloneEntity entity) {
    }
}
