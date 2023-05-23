package net.arna.jcraft.mixin.client;

import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    /*
    @Unique
    private final HashMap<Identifier, ShaderEffect> shaderEffects = new HashMap<Identifier, ShaderEffect>();

    @Inject(method = "reload", at = @At("HEAD"))
    private void jcraft$reload(ResourceManager manager, CallbackInfo ci) {
        shaderEffects.forEach((id, shaderEffect) -> shaderEffect.close());
        shaderEffects.clear();
    }

    @Inject(method = "loadPrograms", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 53, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void jcraft$loadPrograms(ResourceFactory factory) {
        try {
            list2.add(Pair.of(new Shader(factory, "rendertype_time_erase", VertexFormats.POSITION), (shader) -> {
                JCraftClient.timeeraseShader = shader;
            }));
        } catch (IOException e) {
            list2.forEach((pair) -> pair.getFirst().close());
            throw new RuntimeException("could not reload shaders", e);
        }
    }
     */
}
