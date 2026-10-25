package net.arna.jcraft.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.architectury.platform.Platform;
import net.arna.jcraft.JCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.spongepowered.asm.mixin.Mixin;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(FallbackResourceManager.class)
public abstract class FallbackResourceManagerMixin {

    // Ensures the /reload command reloads move set changes from the JSON files.
    @WrapMethod(method = "createResource")
    private static Resource createDirectResource(final PackResources source, final ResourceLocation location, final IoSupplier<InputStream> streamSupplier, final IoSupplier<ResourceMetadata> metadataSupplier, final Operation<Resource> original) {
        if (Platform.isDevelopmentEnvironment() && JCraft.MOD_ID.equals(location.getNamespace()) && location.getPath().startsWith("movesets/")) {
            Path p = Path.of("./../../common/src/main/generated/data/" + location.getNamespace() + "/" + location.getPath());
            if (Files.exists(p)) // Just to be sure.
                return new Resource(source, () -> Files.newInputStream(p), metadataSupplier);
        }
        return original.call(source, location, streamSupplier, metadataSupplier);
    }
}
