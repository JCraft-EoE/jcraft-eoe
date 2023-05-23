package net.arna.jcraft.util;

import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;

public interface IJCraftAnimatedPlayer {
    ModifierLayer<IAnimation> jcraft_getModAnimation();
}
