package net.arna.jcraft.client.rendering.shader.api;

import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public class ShaderSourceProvider {
    private final ResourceManager resourceManager;

    public ShaderSourceProvider(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public @Nullable UnbakedShader loadSource(ShaderSourceRef sourceRef) {
        Optional<Resource> resourceGetter = resourceManager.getResource(sourceRef.path());
        if (resourceGetter.isEmpty())
            return null;

        Resource resource = resourceGetter.get();

        try (BufferedReader reader = resource.openAsReader())
        {
            return new UnbakedShader(reader.lines()
                                            .collect(Collectors.joining(System.lineSeparator())),
                                    sourceRef.type());
        } catch (IOException e) {
            return null;
        }
    }
}