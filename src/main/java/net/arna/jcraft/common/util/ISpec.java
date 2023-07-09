package net.arna.jcraft.common.util;

import net.arna.jcraft.common.spec.JCraftSpec;

public interface ISpec {
    JCraftSpec getSpec();
    void setClientSpec(JCraftSpec spec);
    void setSpec(JCraftSpec spec);
}
