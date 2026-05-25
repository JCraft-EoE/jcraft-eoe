package net.arna.jcraft.common.util;

import net.arna.jcraft.api.component.living.CommonBombTrackerComponent;
import net.arna.jcraft.api.stand.StandEntity;
import net.arna.jcraft.common.entity.SheerHeartAttackEntity;
import net.arna.jcraft.common.entity.stand.*;
import net.arna.jcraft.common.entity.vehicle.AbstractGroundVehicleEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;

// Dummy implementation of IClientEntityHandler used on the server.
public class DummyClientEntityHandler implements IClientEntityHandler {
    public static final DummyClientEntityHandler INSTANCE = new DummyClientEntityHandler();

    private DummyClientEntityHandler() {
    }

    @Override
    public void whiteSnakeRemoteClientTick(WhiteSnakeEntity whiteSnakeEntity) {
    }

    @Override
    public void hierophantGreenRemoteClientTick(HGEntity hgEntity) {
    }

    @Override
    public void purpleHazeRemoteClientTick(AbstractPurpleHazeEntity<?, ?> purpleHazeEntity) {
    }

    @Override
    public void sheerHeartAttackEntityTick(SheerHeartAttackEntity sHAEntity) {
    }

    @Override
    public void displayMetallicaAura(MetallicaEntity metallica) {
    }

    @Override
    public void bombTrackerParticleTick(Entity entity, CommonBombTrackerComponent.BombData bombData) {
    }

    @Override
    public void standEntityClientTick(StandEntity<?, ?> stand) {
    }

    @Override
    public void vehicleMovementTick(AbstractGroundVehicleEntity vehicle) {
    }

    @Override
    public void spawnGroundedMoshParticles(AbstractArrow projectile) {
    }
}
