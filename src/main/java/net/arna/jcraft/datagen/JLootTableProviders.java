package net.arna.jcraft.datagen;

import com.google.common.collect.Maps;
import net.arna.jcraft.registry.JObjectRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.BiConsumer;

public class JLootTableProviders {

    public static class BlockLoot extends FabricBlockLootTableProvider {

        protected BlockLoot(FabricDataGenerator dataGenerator) {
            super(dataGenerator);
        }

        @Override
        protected void generateBlockLootTables() {
            addDrop(JObjectRegistry.METEORITE_BLOCK);
            addDrop(JObjectRegistry.METEORITE_IRON_ORE_BLOCK);
            addDrop(JObjectRegistry.SOUL_BLOCK);
        }
    }

    public static class EntityLoot extends SimpleFabricLootTableProvider {
        private final Map<Identifier, LootTable.Builder> loot = Maps.newHashMap();

        public EntityLoot(FabricDataGenerator dataGenerator) {
            super(dataGenerator, LootContextTypes.ENTITY);
        }

        @Override
        public void accept(BiConsumer<Identifier, LootTable.Builder> consumer) {
            this.generateLoot();
            for (Map.Entry<Identifier, LootTable.Builder> entry : loot.entrySet()) {
                consumer.accept(entry.getKey(), entry.getValue());
            }
        }

        private void generateLoot() {

        }
    }
}
