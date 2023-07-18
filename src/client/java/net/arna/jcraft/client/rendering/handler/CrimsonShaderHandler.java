package net.arna.jcraft.client.rendering.handler;

import ladysnake.satin.api.event.PostWorldRenderCallbackV2;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.arna.jcraft.client.rendering.skybox.CrimsonSkyBoxCool;
import net.arna.jcraft.client.rendering.skybox.SkyBoxManager;
import net.arna.jcraft.common.util.BlockInfo;
import net.arna.jcraft.common.util.JUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CrimsonShaderHandler extends StandShaderHandler {
    public static final CrimsonShaderHandler INSTANCE = new CrimsonShaderHandler();

    public long effectLength = 0;
    public List<BlockInfo> list = new ArrayList<>();

    @Override
    public void onWorldRendered(@NotNull MatrixStack matrices, @NotNull Camera camera, float tickDelta, long nanoTime) {
        if (renderingEffect) {
            World world = camera.getFocusedEntity().getWorld();
            if(list.isEmpty()){
                list = JUtils.collectBlockInfo(world, camera.getBlockPos(), 8);
            }
            BlockRenderManager manager = MinecraftClient.getInstance().getBlockRenderManager();


            VertexConsumerProvider.Immediate consumer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            for (BlockInfo info : list){


                matrices.push();
                matrices.translate(info.pos().getX() - camera.getPos().x, info.pos().getY() - camera.getPos().y, info.pos().getZ() - camera.getPos().z);

                manager.getModelRenderer().render(
                        world,
                        manager.getModel(info.state()),
                        info.state(),
                        info.pos(),
                        matrices,
                        consumer.getBuffer(RenderLayers.getBlockLayer(info.state())),
                        true,
                        Random.create(),
                        info.state().getRenderingSeed(info.pos()),
                        OverlayTexture.DEFAULT_UV
                );
                matrices.pop();
            }

        }
    }

    @Override
    public void onEndTick(MinecraftClient client) {
        SkyBoxManager skyboxManager = SkyBoxManager.getInstance();

        if (shouldRender) {
            if (!renderingEffect) {
                ticks = 0;
                renderingEffect = true;
                skyboxManager.setEnabled(true);
                skyboxManager.setCurrentSkyBox(new CrimsonSkyBoxCool());
            }
            ticks++;

            if (hasFinishedAnimation()) {
                renderingEffect = false;
                shouldRender = false;
                skyboxManager.setCurrentSkyBox(null);
                skyboxManager.setEnabled(false);
                list.clear();
            }
        } else {
            renderingEffect = false;
            skyboxManager.setCurrentSkyBox(null);
            skyboxManager.setEnabled(false);
            list.clear();
        }
    }

    private boolean hasFinishedAnimation() {
        return ticks > effectLength;
    }

    @Override
    public void renderShaderEffects(float tickDelta) {

    }

    public void init() {
        PostWorldRenderCallbackV2.EVENT.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
        ShaderEffectRenderCallback.EVENT.register(this);
    }
}
