package net.arna.jcraft.client.model.entity;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.entity.projectile.HGNetEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

import java.util.List;
import java.util.stream.IntStream;

public class HGNetModel extends GeoModel<HGNetEntity> {
    private static final List<Identifier> skins = IntStream.range(0, 4).mapToObj(
            i -> JCraft.id("textures/entity/hg_nets/" + i + ".png")).toList();

    @Override
    public Identifier getModelResource(HGNetEntity object) {
        return JCraft.id("geo/hg_nets.geo.json");
    }

    @Override
    public Identifier getTextureResource(HGNetEntity object) {
        return skins.get(object.getSkin());
    }

    @Override
    public Identifier getAnimationResource(HGNetEntity animatable) {
        return JCraft.id("animations/hg_nets.animation.json");
    }

}
