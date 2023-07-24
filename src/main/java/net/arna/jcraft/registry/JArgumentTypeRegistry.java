package net.arna.jcraft.registry;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.argumenttype.SpecArgumentType;
import net.arna.jcraft.common.argumenttype.StandArgumentType;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;

public interface JArgumentTypeRegistry {
    static void registerArgumentTypes() {
        ArgumentTypeRegistry.registerArgumentType(JCraft.id("stand"), StandArgumentType.class, ConstantArgumentSerializer.of(StandArgumentType::stand));
        ArgumentTypeRegistry.registerArgumentType(JCraft.id("spec"), SpecArgumentType.class, ConstantArgumentSerializer.of(SpecArgumentType::spec));
    }
}
