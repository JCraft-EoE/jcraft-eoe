package net.arna.jcraft.client.renderer.effects;

import com.google.common.collect.EvictingQueue;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.LongLongPair;
import lombok.Synchronized;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.*;

@UtilityClass
public class AttackHitboxEffectRenderer {
    // Use an evicting queue to limit the amount of hit boxes rendered at a time to 8.
    // If there are already 8 hit boxes, and we wish to add more, old ones will be removed.
    @SuppressWarnings("UnstableApiUsage") // I do not care. (based)
    private static final Queue<Pair<LongLongPair, Box>> hitboxes = EvictingQueue.create(8);
    private static final List<Pair<LongLongPair, Box>> highPriorityBoxes = new ArrayList<>(); // These do not get evicted.

    public static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(AttackHitboxEffectRenderer::render);
    }

    public static void addHitboxes(Iterable<Box> boxes) {
        boxes.forEach(AttackHitboxEffectRenderer::addHitbox);
    }

    public static void addHitbox(Box box) {
        addHitbox(box, 2500, false);
    }

    @Synchronized
    public static void addHitbox(Box box, long duration, boolean highPriority) {
        (highPriority ? highPriorityBoxes : hitboxes).add(Pair.of(LongLongPair.of(Util.getEpochTimeMs(), duration), box));
    }

    @Synchronized
    private static void render(WorldRenderContext ctx) {
        if (!MinecraftClient.getInstance().getEntityRenderDispatcher().shouldRenderHitboxes()) return;

        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

        Vec3d camPos = ctx.camera().getPos();
        MatrixStack matrices = ctx.matrixStack();
        matrices.push();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        renderBoxes(ctx, matrices, hitboxes);
        renderBoxes(ctx, matrices, highPriorityBoxes);

        matrices.pop();
    }

    private static void renderBoxes(WorldRenderContext ctx, MatrixStack matrices, Collection<Pair<LongLongPair, Box>> boxes) {
        for (Iterator<Pair<LongLongPair, Box>> iterator = boxes.iterator(); iterator.hasNext();) {
            Pair<LongLongPair, Box> pair = iterator.next();
            renderBox(ctx, pair.right(), matrices);
            if (Util.getEpochTimeMs() - pair.left().leftLong() > pair.left().rightLong()) iterator.remove();
        }
    }

    @SuppressWarnings("DuplicatedCode") // Don't care
    private static void renderBox(WorldRenderContext ctx, Box box, MatrixStack matrices) {
        // Draw faces
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder quadsVc = tess.getBuffer();
        quadsVc.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false); // Don't write to the depth buffer.

        int c = 0x54FF0000;

        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        Matrix4f m = matrices.peek().getPositionMatrix();

        // Up
        quadsVc.vertex(m, minX, maxY, minZ).color(c).next();
        quadsVc.vertex(m, minX, maxY, maxZ).color(c).next();
        quadsVc.vertex(m, maxX, maxY, maxZ).color(c).next();
        quadsVc.vertex(m, maxX, maxY, minZ).color(c).next();

        // Down
        quadsVc.vertex(m, minX, minY, minZ).color(c).next();
        quadsVc.vertex(m, maxX, minY, minZ).color(c).next();
        quadsVc.vertex(m, maxX, minY, maxZ).color(c).next();
        quadsVc.vertex(m, minX, minY, maxZ).color(c).next();

        // North
        quadsVc.vertex(m, minX, minY, minZ).color(c).next();
        quadsVc.vertex(m, minX, maxY, minZ).color(c).next();
        quadsVc.vertex(m, maxX, maxY, minZ).color(c).next();
        quadsVc.vertex(m, maxX, minY, minZ).color(c).next();

        // East
        quadsVc.vertex(m, maxX, minY, minZ).color(c).next();
        quadsVc.vertex(m, maxX, maxY, minZ).color(c).next();
        quadsVc.vertex(m, maxX, maxY, maxZ).color(c).next();
        quadsVc.vertex(m, maxX, minY, maxZ).color(c).next();

        // South
        quadsVc.vertex(m, minX, minY, maxZ).color(c).next();
        quadsVc.vertex(m, maxX, minY, maxZ).color(c).next();
        quadsVc.vertex(m, maxX, maxY, maxZ).color(c).next();
        quadsVc.vertex(m, minX, maxY, maxZ).color(c).next();

        // West
        quadsVc.vertex(m, minX, minY, minZ).color(c).next();
        quadsVc.vertex(m, minX, minY, maxZ).color(c).next();
        quadsVc.vertex(m, minX, maxY, maxZ).color(c).next();
        quadsVc.vertex(m, minX, maxY, minZ).color(c).next();

        tess.draw();

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.disableDepthTest();

        // Draw lines
        VertexConsumer linesVc = Objects.requireNonNull(ctx.consumers()).getBuffer(RenderLayer.LINES);
        WorldRenderer.drawBox(matrices, linesVc, box, 1f, 0f, 0f, 1f);
    }
}
