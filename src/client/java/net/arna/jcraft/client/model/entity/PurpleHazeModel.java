package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.stand.AbstractPurpleHazeEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import net.minecraft.util.Identifier;

public class PurpleHazeModel extends StandEntityModel<AbstractPurpleHazeEntity<?, ?>> {
    private static final Identifier MODEL = JCraft.id("geo/purple_haze.geo.json");

    public PurpleHazeModel(boolean distortion) {
        super(distortion ? StandType.PURPLE_HAZE_DISTORTION : StandType.PURPLE_HAZE);
    }

    @Override
    public Identifier getModelResource(AbstractPurpleHazeEntity<?, ?> object) {
        return MODEL;
    }
}
