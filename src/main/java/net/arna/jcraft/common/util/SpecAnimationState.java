package net.arna.jcraft.common.util;

import net.arna.jcraft.common.spec.JSpec;

public interface SpecAnimationState<S extends JSpec<S, ?>> {
    String getKey(S spec);
}
