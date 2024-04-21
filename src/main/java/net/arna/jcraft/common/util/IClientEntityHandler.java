package net.arna.jcraft.common.util;

import net.arna.jcraft.common.component.living.BombTrackerComponent;
import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.arna.jcraft.common.entity.stand.*;
import net.minecraft.entity.Entity;

public interface IClientEntityHandler {
    void bombTrackerParticleTick(Entity entity, BombTrackerComponent.BombData bombData);
    void standEntityClientTick(StandEntity<?,?> stand);

    void whiteSnakeRemoteClientTick(WhiteSnakeEntity whiteSnakeEntity);
    void hierophantGreenRemoteClientTick(HGEntity hgEntity);
    void purpleHazeRemoteClientTick(AbstractPurpleHazeEntity<?,?> purpleHazeEntity);
    void sheerHeartAttackEntityTick(SheerHeartAttackEntity sHAEntity);
}
