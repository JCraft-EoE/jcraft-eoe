package net.arna.jcraft.registry;

import com.mojang.serialization.Lifecycle;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.client.rendering.skybox.AbstractSkyBox;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;


public class JSkyBoxRegistry<T extends AbstractSkyBox> {

    public static final Registry<JSkyBoxRegistry<? extends AbstractSkyBox>> REGISTRY =
            FabricRegistryBuilder.<JSkyBoxRegistry<? extends AbstractSkyBox>, SimpleRegistry<JSkyBoxRegistry<? extends AbstractSkyBox>>>from(
                    new SimpleRegistry<>(RegistryKey.ofRegistry(new Identifier(JCraft.MOD_ID, "skybox_type")), Lifecycle.stable(), null)).buildAndRegister();


    public static void init() {

    }

}
