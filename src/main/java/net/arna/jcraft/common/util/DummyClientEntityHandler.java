package net.arna.jcraft.common.util;

import net.arna.jcraft.common.component.BombTrackerComponent;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.minecraft.entity.Entity;

// Dummy implementation of IClientEntityHandler used on the server.
public class DummyClientEntityHandler implements IClientEntityHandler {
    public static final DummyClientEntityHandler INSTANCE = new DummyClientEntityHandler();

    private DummyClientEntityHandler() {}

    @Override
    public void playerCloneEntityClientTick(PlayerCloneEntity entity) {}

    @Override
    public void bombTrackerParticleTick(Entity entity, BombTrackerComponent.BombData bombData) {}

    @Override
    public void standEntityClientTick(StandEntity<?,?> stand) {}
}
