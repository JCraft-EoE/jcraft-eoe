package net.arna.jcraft.common.util;

import net.arna.jcraft.common.entity.PlayerCloneEntity;
import net.arna.jcraft.common.entity.stand.StandEntity;

public interface IClientEntityHandler {
    void standEntityClientTick(StandEntity<?,?> stand);
    void playerCloneEntityClientTick(PlayerCloneEntity entity);
}
