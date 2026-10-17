package net.arna.jcraft.mixin;

import net.arna.jcraft.api.splatter.JSplatterManager;
import net.arna.jcraft.common.util.IJSplatterManagerHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Level.class)
public abstract class WorldMixin implements IJSplatterManagerHolder {

    private final @Unique JSplatterManager jcraft$splatterManager = new JSplatterManager((Level)(Object)this);

    @Override
    public JSplatterManager jcraft$getSplatterManager() {
        return jcraft$splatterManager;
    }

}
