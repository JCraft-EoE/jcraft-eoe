package net.arna.jcraft.common.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.arna.jcraft.common.entity.stand.StandEntity;
import net.arna.jcraft.common.entity.stand.StandType;
import org.jetbrains.annotations.Nullable;

public interface StandComponent extends Component, AutoSyncedComponent {
    @Nullable StandType getType();
    void setType(@Nullable StandType type);

    int getSkin();
    void setSkin(int skin);

    @Nullable StandEntity<?, ?> getStand();
    void setStand(@Nullable StandEntity<?, ?> stand);
}
