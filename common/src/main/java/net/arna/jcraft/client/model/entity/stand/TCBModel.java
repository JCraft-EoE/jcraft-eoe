package net.arna.jcraft.client.model.entity.stand;

import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.entity.stand.TCBEntity;

public class TCBModel extends StandEntityModel<TCBEntity> {
    public TCBModel() {
        super(JStandTypeRegistry.TCB.get());
    }
}