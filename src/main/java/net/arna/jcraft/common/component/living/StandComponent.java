package net.arna.jcraft.common.component.living;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import org.jetbrains.annotations.Nullable;

public interface StandComponent extends Component, AutoSyncedComponent {
    @Nullable StandType getType();
    default void setType(@Nullable StandType type) {
        setTypeAndSkin(type, 0);
    }

    void setTypeAndSkin(@Nullable StandType type, int skin);

    int getSkin();
    void setSkin(int skin);

    @Nullable StandEntity<?, ?> getStand();
    void setStand(@Nullable StandEntity<?, ?> stand);
}
