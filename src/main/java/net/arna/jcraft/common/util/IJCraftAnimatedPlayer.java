package net.arna.jcraft.common.util;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;

public interface IJCraftAnimatedPlayer {
    ModifierLayer<IAnimation> jcraft_getModAnimation();
}
