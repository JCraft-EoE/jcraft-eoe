package net.arna.jcraft.common.util;

import net.arna.jcraft.common.component.living.BombTrackerComponent;
import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.arna.jcraft.common.entity.stand.HGEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.WhiteSnakeEntity;
import net.minecraft.entity.Entity;

public interface IClientEntityHandler {
    void bombTrackerParticleTick(Entity entity, BombTrackerComponent.BombData bombData);
    void standEntityClientTick(StandEntity<?,?> stand);

    void whiteSnakeRemoteClientTick(WhiteSnakeEntity whiteSnakeEntity);
    void hierophantGreenRemoteClientTick(HGEntity hgEntity);
    void sheerHeartAttackEntityTick(SheerHeartAttackEntity sHAEntity);
}
