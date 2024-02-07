package net.arna.jcraft.common.component.entity;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.arna.jcraft.common.gravity.RotationAnimation;
import net.arna.jcraft.common.gravity.api.RotationParameters;
import net.arna.jcraft.common.gravity.util.Gravity;
import net.minecraft.util.math.Direction;

import java.util.List;

public interface GravityComponent extends Component, CommonTickingComponent {
    //Internal

    void onGravityChanged(Direction oldGravity, Direction newGravity, RotationParameters rotationParameters, boolean initialGravity);

    void updateGravity(RotationParameters rotationParameters, boolean initialGravity);

    //Get

    Direction getGravityDirection();

    Direction getPrevGravityDirection();

    Direction getDefaultGravityDirection();

    Direction getActualGravityDirection();

    List<Gravity> getGravity();

    boolean getInvertGravity();

    RotationAnimation getGravityAnimation();

    //Set

    void setGravity(List<Gravity> gravityList, boolean initialGravity);

    void invertGravity(boolean isInverted, RotationParameters rotationParameters, boolean initialGravity);

    void setDefaultGravityDirection(Direction gravityDirection, RotationParameters rotationParameters, boolean initialGravity);

    void addGravity(Gravity gravity, boolean initialGravity);

    void clearGravity(RotationParameters rotationParameters, boolean initialGravity);
}
