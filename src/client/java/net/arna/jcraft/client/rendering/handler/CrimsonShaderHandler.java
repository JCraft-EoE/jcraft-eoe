package net.arna.jcraft.client.rendering.handler;

import ladysnake.satin.api.event.PostWorldRenderCallbackV2;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import ladysnake.satin.api.managed.ManagedCoreShader;
import ladysnake.satin.api.managed.ShaderEffectManager;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.skybox.CrimsonSkyBox;
import net.arna.jcraft.common.util.BlockInfo;
import net.arna.jcraft.client.rendering.skybox.SkyBoxManager;
import net.arna.jcraft.common.util.RenderUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.SideShapeType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class CrimsonShaderHandler extends StandShaderHandler {
    public static CrimsonShaderHandler INSTANCE = new CrimsonShaderHandler();
    public static final Identifier SHADER_ID = JCraft.id("space");

    public static final ManagedCoreShader SHADER = ShaderEffectManager.getInstance().manageCoreShader(SHADER_ID, VertexFormats.POSITION_TEXTURE);

    public long effectLength = 0;
    public List<BlockInfo> list = new ArrayList<>();

    @Override
    public void onWorldRendered(MatrixStack matrices, Camera camera, float tickDelta, long nanoTime) {
        if (renderingEffect) {
            World world = camera.getFocusedEntity().getWorld();
            if(list.isEmpty()){
                list = collectBlockInfo(world, camera.getBlockPos());
            }
            BlockRenderManager manager = MinecraftClient.getInstance().getBlockRenderManager();
            var en = MinecraftClient.getInstance().getBlockEntityRenderDispatcher();

            VertexConsumerProvider.Immediate consumer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            for (BlockInfo info : list){
                matrices.push();
                RenderUtils.renderBlockAtLocation(matrices, camera, new Vec3d(info.pos().getX(), info.pos().getY(), info.pos().getZ()), new Identifier("textures/block/stone.png"), 1);
                matrices.pop();

                matrices.push();
                matrices.translate((double)(info.pos().getX() & 15), (double)(info.pos().getY() & 15), (double)(info.pos().getZ() & 15));
                manager.renderBlock(info.state(), info.pos(), world, matrices, consumer.getBuffer(RenderLayers.getBlockLayer(info.state())), true, world.getRandom());
                matrices.pop();

                //manager.renderBlock(info.state(), info.pos(), world, matrices, consumer.getBuffer(RenderLayers.getBlockLayer(info.state())), true, world.getRandom());
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
                skyboxManager.setCurrentSkyBox(new CrimsonSkyBox());
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
        }
    }

    private boolean hasFinishedAnimation() {
        return ticks > effectLength;
    }

    @Override
    public void renderShaderEffects(float tickDelta) {

    }

    public static List<BlockInfo> collectBlockInfo(World world, BlockPos origin) {
        List<BlockInfo> infoList = new ArrayList<>();
        int radius = 8;

        int[][] array = new int[radius * 2 + 1][radius * 2 + 1];

        for (int i = 0; i < radius; i++) {
            for (int j = 0; j < radius; j++) {
                array[i][j] = 0;
            }
        }

        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        for (int y = originY + radius; y >= originY - radius; y--) {
            for (int x = originX - radius; x <= originX + radius; x++) {
                for (int z = originZ - radius; z <= originZ + radius; z++) {
                    double distance = Math.sqrt(Math.pow(x - originX, 2) + Math.pow(y - originY, 2) + Math.pow(z - originZ, 2));
                    if (distance <= radius) {
                        double skipProbability = (distance / radius);
                        if (world.getRandom().nextDouble() > skipProbability / 2) {
                            BlockPos pos = new BlockPos(x, y, z);
                            BlockState state = world.getBlockState(pos);
                            int x0 = x - originX + radius;
                            int z0 = z - originZ + radius;
                            if(state.isSideSolid(world, pos, Direction.UP, SideShapeType.RIGID) && array[x0][z0] == 0){
                                array[x0][z0] = 1;

                                BlockInfo info = new BlockInfo(state, pos);
                                infoList.add(info);
                            }
                        }
                    }
                }
            }
        }

        return infoList;
    }

    public void init() {
        PostWorldRenderCallbackV2.EVENT.register(this);
        ClientTickEvents.END_CLIENT_TICK.register(this);
        ShaderEffectRenderCallback.EVENT.register(this);
    }
}
