package net.arna.jcraft.client.renderer.effects.splatter;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.*;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class SplatterEffectRenderer {
    private static final Set<Splatter> SPLATTERS = new HashSet<>();

    public static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(SplatterEffectRenderer::render);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            synchronized (SPLATTERS) {
                SPLATTERS.forEach(Splatter::tick);
                SPLATTERS.removeIf(Splatter::isRemoved);
            }
        });
    }

    public static void addSplatter(World world, Vec3d pos, SplatterType type) {
        synchronized (SPLATTERS) {
            SPLATTERS.add(new Splatter(world, new Vec3d(pos.getX(), Math.floor(pos.getY()), pos.getZ()), type, .8f));
        }
    }

    private static void render(WorldRenderContext ctx) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);

        MatrixStack matrices = ctx.matrixStack();
        Vec3d camPos = ctx.camera().getPos();

        synchronized (SPLATTERS) {
            for (Splatter splatter : SPLATTERS) {
                if (splatter.isRemoved()) continue;

                RenderSystem.setShaderTexture(0, splatter.getType().getTexture());

                matrices.push();
                matrices.translate(-camPos.x, -camPos.y, -camPos.z);

                Tessellator tess = Tessellator.getInstance();
                BufferBuilder buf = tess.getBuffer();
                buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);

                int blockLight = splatter.getWorld().getLightLevel(LightType.BLOCK, new BlockPos(splatter.getPos()));
                int skyLight = splatter.getWorld().getLightLevel(LightType.SKY, new BlockPos(splatter.getPos()));
                int light = LightmapTextureManager.pack(blockLight, skyLight);
                float alpha = splatter.getStrength(ctx.tickDelta());

                for (SplatterSection section : splatter.getSections())
                    if (!section.isRemoved())
                        renderSection(section, buf, matrices, alpha, light, splatter.getOffset());

                tess.draw();
                matrices.pop();
            }
        }

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    @SuppressWarnings("DuplicatedCode") // I do not care how similar the different directions' code are.
    private static void renderSection(SplatterSection section, BufferBuilder buf, MatrixStack matrices, float alpha, int light, float offset) {
        matrices.push();
        Vec3f offsetVec = section.getDirection().getUnitVector();
        offsetVec.multiplyComponentwise(offset, offset, offset); // Prevent z-fighting with anchor block and other splatters.
        matrices.translate(offsetVec.getX(), offsetVec.getY(), offsetVec.getZ());
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        Vec3f min = section.getMinPos();
        Vec3f max = section.getMaxPos();
        Vec2f minUv = section.getMinUv();
        Vec2f maxUv = section.getMaxUv();

        float minX = min.getX();
        float minY = min.getY();
        float minZ = min.getZ();
        float maxX = max.getX();
        float maxY = max.getY();
        float maxZ = max.getZ();

        switch (section.getDirection()) {
            case UP -> {
                vertex(buf, matrix, minX, minY, minZ, minUv.x, minUv.y, alpha, light);
                vertex(buf, matrix, maxX, minY, minZ, maxUv.x, minUv.y, alpha, light);
                vertex(buf, matrix, maxX, minY, maxZ, maxUv.x, maxUv.y, alpha, light);
                vertex(buf, matrix, minX, minY, maxZ, minUv.x, maxUv.y, alpha, light);
            }
            case NORTH -> {
                vertex(buf, matrix, minX, minY, minZ, minUv.x, minUv.y, alpha, light);
                vertex(buf, matrix, minX, maxY, minZ, minUv.x, maxUv.y, alpha, light);
                vertex(buf, matrix, maxX, maxY, minZ, maxUv.x, maxUv.y, alpha, light);
                vertex(buf, matrix, maxX, minY, minZ, maxUv.x, minUv.y, alpha, light);
            }
            case EAST -> {
                vertex(buf, matrix, maxX, minY, minZ, maxUv.x, minUv.y, alpha, light);
                vertex(buf, matrix, maxX, maxY, minZ, minUv.x, minUv.y, alpha, light);
                vertex(buf, matrix, maxX, maxY, maxZ, minUv.x, maxUv.y, alpha, light);
                vertex(buf, matrix, maxX, minY, maxZ, maxUv.x, maxUv.y, alpha, light);
            }
            case SOUTH -> {
                vertex(buf, matrix, minX, minY, maxZ, minUv.x, maxUv.y, alpha, light);
                vertex(buf, matrix, maxX, minY, maxZ, maxUv.x, maxUv.y, alpha, light);
                vertex(buf, matrix, maxX, maxY, maxZ, maxUv.x, minUv.y, alpha, light);
                vertex(buf, matrix, minX, maxY, maxZ, minUv.x, minUv.y, alpha, light);
            }
            case WEST -> {
                vertex(buf, matrix, minX, minY, minZ, minUv.x, minUv.y, alpha, light);
                vertex(buf, matrix, minX, minY, maxZ, minUv.x, maxUv.y, alpha, light);
                vertex(buf, matrix, minX, maxY, maxZ, maxUv.x, maxUv.y, alpha, light);
                vertex(buf, matrix, minX, maxY, minZ, maxUv.x, minUv.y, alpha, light);
            }
            // Down should be impossible
            default -> throw new IllegalStateException("Unexpected value: " + section.getDirection());
        }

        matrices.pop();
    }

    private static void vertex(BufferBuilder buf, Matrix4f matrix, float x, float y, float z, float u, float v, float alpha, int light) {
        buf
                .vertex(matrix, x, y, z)
                .color(1f, 1f, 1f, alpha)
                .texture(u, v)
                .light(light)
                .next();
    }
}
