package net.arna.jcraft.common.util;

import net.arna.jcraft.common.component.living.BombTrackerComponent;
import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.arna.jcraft.common.entity.stand.*;
import net.minecraft.entity.Entity;

// Dummy implementation of IClientEntityHandler used on the server.
public class DummyClientEntityHandler implements IClientEntityHandler {
    public static final DummyClientEntityHandler INSTANCE = new DummyClientEntityHandler();

    private DummyClientEntityHandler() {}

    @Override
    public void whiteSnakeRemoteClientTick(WhiteSnakeEntity whiteSnakeEntity) {}

    @Override
    public void hierophantGreenRemoteClientTick(HGEntity hgEntity) {}

    @Override
    public void purpleHazeRemoteClientTick(AbstractPurpleHazeEntity<?,?> purpleHazeEntity) {}

    @Override
    public void sheerHeartAttackEntityTick(SheerHeartAttackEntity sHAEntity) {}

    @Override
    public void bombTrackerParticleTick(Entity entity, BombTrackerComponent.BombData bombData) {}

    @Override
    public void standEntityClientTick(StandEntity<?,?> stand) {}
}
