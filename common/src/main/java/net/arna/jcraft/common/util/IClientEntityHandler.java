package net.arna.jcraft.common.util;

import net.arna.jcraft.api.component.living.CommonBombTrackerComponent;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.arna.jcraft.common.entity.stand.*;
import net.arna.jcraft.common.entity.vehicle.AbstractGroundVehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;

public interface IClientEntityHandler {
    void bombTrackerParticleTick(Entity entity, CommonBombTrackerComponent.BombData bombData);

    void standEntityClientTick(StandEntity<?, ?> stand);

    void whiteSnakeRemoteClientTick(WhiteSnakeEntity whiteSnakeEntity);

    void hierophantGreenRemoteClientTick(HGEntity hgEntity);

    void purpleHazeRemoteClientTick(AbstractPurpleHazeEntity<?, ?> purpleHazeEntity);

    void sheerHeartAttackEntityTick(SheerHeartAttackEntity sHAEntity);

    void displayMetallicaAura(MetallicaEntity metallica);

    void vehicleMovementTick(AbstractGroundVehicleEntity vehicle);

    void spawnGroundedMoshParticles(AbstractArrow projectile);
}
