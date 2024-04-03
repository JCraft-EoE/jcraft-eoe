package net.arna.jcraft.client.renderer.block;

import net.arna.jcraft.client.model.tile.CoffinModel;
import net.arna.jcraft.common.block.tile.CoffinTileEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CoffinTileRenderer extends GeoBlockRenderer<CoffinTileEntity> {
    public CoffinTileRenderer(BlockEntityRendererFactory.Context context) { super(new CoffinModel()); }
}
