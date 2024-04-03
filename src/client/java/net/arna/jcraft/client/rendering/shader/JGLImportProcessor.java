package net.arna.jcraft.client.rendering.shader;

import net.arna.jcraft.JCraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlImportProcessor;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Make sure we can have the CORE shaders in our own namespace
 */
public class JGLImportProcessor extends GlImportProcessor {
    @Nullable
    @Override
    public String loadImport(boolean inline, String name) {
        JCraft.LOGGER.debug("Loading moj_import in EffectShader: " + name);

        Identifier id = new Identifier(name);
        Identifier id1 = new Identifier(id.getNamespace(), "shaders/include/" + id.getPath() + ".glsl");

        Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(id1).orElseThrow();

        try {
            return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException ioexception) {
            JCraft.LOGGER.error("Could not open GLSL import {}: {}", name, ioexception.getMessage());
            return "#error " + ioexception.getMessage();
        }
    }
}
