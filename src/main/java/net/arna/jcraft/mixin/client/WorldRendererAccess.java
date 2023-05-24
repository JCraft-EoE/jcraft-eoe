package net.arna.jcraft.mixin.client;


import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccess {

    @Accessor("END_SKY")
    static Identifier getEndSky() {
        throw new AssertionError();
    }

    @Accessor
    VertexBuffer getLightSkyBuffer();

    @Accessor
    VertexBuffer getStarsBuffer();
}