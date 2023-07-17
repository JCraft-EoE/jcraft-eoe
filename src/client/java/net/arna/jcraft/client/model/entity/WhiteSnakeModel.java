package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.common.entity.StandType;
import net.arna.jcraft.common.entity.WhiteSnakeEntity;

public class WhiteSnakeModel extends StandEntityModel<WhiteSnakeEntity> {

    public WhiteSnakeModel() {
        super(StandType.WHITE_SNAKE, -0.10f, -0.10f);
    }
}
