package net.arna.jcraft.client.rendering.post;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.PostProcessor;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class TimestopShaderPostProcessor extends PostProcessor {

    @Override
    public void init() {

    }

    @Override
    public Identifier getShaderEffectId() {
        return JCraft.id("za_warudo");
    }

    @Override
    public void beforeProcess(MatrixStack viewModelStack) {

    }

    @Override
    public void afterProcess() {

    }

    public boolean playEffect(Vec3d center, float time, Runnable omComplete){
        return !isActive();
    }
}
