package net.arna.jcraft.client.mixin;

import net.arna.jcraft.client.util.JClientUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {

    /**
     * @author Arna
     * @reason MixinExtras was causing the mod to consider Sodium a dependency, the change itself is minor enough.
     */
    @Overwrite
    private static <T extends BlockEntity> void render(BlockEntityRenderer<T> renderer, T blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        World world = blockEntity.getWorld();
        int i;
        if (world != null) {
            i = WorldRenderer.getLightmapCoordinates(world, blockEntity.getPos());
        } else {
            i = 15728880;
        }

        renderer.render(
                blockEntity,
                JClientUtils.getTicksIfInTSRange(blockEntity.getPos()) > 0 ? 0 : tickDelta,
                matrices,
                vertexConsumers,
                i,
                OverlayTexture.DEFAULT_UV);
    }
}
