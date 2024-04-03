package net.arna.jcraft.client.renderer.block;

import net.arna.jcraft.client.registry.JRenderLayerRegistry;
import net.arna.jcraft.common.block.entity.ShaderTestBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class ShaderTestBlockEntityRenderer implements BlockEntityRenderer<ShaderTestBlockEntity> {

    public ShaderTestBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(ShaderTestBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity.getWorld() == null) {
            return;
        }

        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        this.renderSides(matrix4f, vertexConsumers.getBuffer(this.getLayer()));
    }

    private RenderLayer getLayer() {
        return JRenderLayerRegistry.TRANSPARENT_BLOCK;
    }

    private void renderSides(Matrix4f matrix, VertexConsumer vertexConsumer) {
        float f = this.getBottomYOffset();
        float g = this.getTopYOffset();
        this.renderSide(matrix, vertexConsumer, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
        this.renderSide(matrix, vertexConsumer, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        this.renderSide(matrix, vertexConsumer, 1.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F);
        this.renderSide(matrix, vertexConsumer, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F, 1.0F, 0.0F);
        this.renderSide(matrix, vertexConsumer, 0.0F, 1.0F, f, f, 0.0F, 0.0F, 1.0F, 1.0F);
        this.renderSide(matrix, vertexConsumer, 0.0F, 1.0F, g, g, 1.0F, 1.0F, 0.0F, 0.0F);
    }

    private void renderSide(Matrix4f model, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4) {
        vertices.vertex(model, x1, y1, z1).next();
        vertices.vertex(model, x2, y1, z2).next();
        vertices.vertex(model, x2, y2, z3).next();
        vertices.vertex(model, x1, y2, z4).next();
    }

    protected float getTopYOffset() {
        return 1.0F;
    }

    protected float getBottomYOffset() {
        return 0.0F;
    }

    @Override
    public boolean rendersOutsideBoundingBox(ShaderTestBlockEntity blockEntity) {
        return true;
    }
}
