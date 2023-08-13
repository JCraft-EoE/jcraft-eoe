package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.common.entity.stand.StandType;
import net.arna.jcraft.common.entity.stand.WhiteSnakeEntity;

public class WhiteSnakeModel extends StandEntityModel<WhiteSnakeEntity> {

    public WhiteSnakeModel() {
        super(StandType.WHITE_SNAKE, -0.10f, -0.10f);
    }
}
