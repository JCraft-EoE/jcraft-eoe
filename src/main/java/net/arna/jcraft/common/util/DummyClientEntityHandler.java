package net.arna.jcraft.common.util;

import net.arna.jcraft.common.component.living.BombTrackerComponent;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.WhiteSnakeEntity;
import net.minecraft.entity.Entity;

// Dummy implementation of IClientEntityHandler used on the server.
public class DummyClientEntityHandler implements IClientEntityHandler {
    public static final DummyClientEntityHandler INSTANCE = new DummyClientEntityHandler();

    private DummyClientEntityHandler() {}

    @Override
    public void playerCloneEntityClientTick(PlayerCloneEntity entity) {}

    @Override
    public void whiteSnakeRemoteClientTick(WhiteSnakeEntity whiteSnakeEntity) {}

    @Override
    public void bombTrackerParticleTick(Entity entity, BombTrackerComponent.BombData bombData) {}

    @Override
    public void standEntityClientTick(StandEntity<?,?> stand) {}
}
