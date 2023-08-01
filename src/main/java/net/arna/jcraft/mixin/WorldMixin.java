package net.arna.jcraft.mixin;

import net.arna.jcraft.common.splatter.JSplatterManager;
import net.arna.jcraft.common.util.IJSplatterManagerHolder;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(World.class)
public class WorldMixin implements IJSplatterManagerHolder {
    private final @Unique JSplatterManager splatterManager = new JSplatterManager((World) (Object) this);

    @Override
    public JSplatterManager jcraft$getSplatterManager() {
        return splatterManager;
    }
}
