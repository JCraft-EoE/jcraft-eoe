package net.arna.jcraft.datagen.providers.data;

import com.google.common.collect.Maps;
import net.arna.jcraft.api.registry.JBlockRegistry;
import net.arna.jcraft.api.registry.JEntityTypeRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class JLootTableProviders {

    public static class BlockLoot extends FabricBlockLootTableProvider {

        public BlockLoot(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generate() {
            dropSelf(JBlockRegistry.METEORITE_BLOCK.get());
            dropSelf(JBlockRegistry.POLISHED_METEORITE_BLOCK.get());
            dropSelf(JBlockRegistry.METEORITE_IRON_ORE_BLOCK.get());
            dropSelf(JBlockRegistry.SOUL_BLOCK.get());
            dropSelf(JBlockRegistry.HOT_SAND_BLOCK.get());
            dropSelf(JBlockRegistry.STELLAR_IRON_BLOCK.get());
            dropSelf(JBlockRegistry.CINDERELLA_GREEN_BLOCK.get());
        }
    }

    public static class EntityLoot extends SimpleFabricLootTableProvider {

        private final Map<ResourceLocation, LootTable.Builder> loot = Maps.newHashMap();

        public EntityLoot(FabricDataOutput output) {
            super(output, LootContextParamSets.ENTITY);
        }

        // add new entries here
        private void generateLoot() {
            addDrop(JEntityTypeRegistry.PETSHOP.get(), this::petshopLoot);
            addDrop(JEntityTypeRegistry.AYA_TSUJI.get(), this::ayaTsujiLoot);
            addDrop(JEntityTypeRegistry.DARBY_OLDER.get(), this::darbyOlderLoot);
            addDrop(JEntityTypeRegistry.ANUBIS_SPEC_USER.get(), this::anubisSpecUserLoot);
        }

        // loot builder for Petshop
        private LootTable.Builder petshopLoot(EntityType<?> type) {
            return LootTable.lootTable()
                    .withPool(constantPool(1f).add(LootItem.lootTableItem(Items.FEATHER).apply(uniformAmount(1f, 2f))))
                    .withPool(constantPool(1f).add(LootItem.lootTableItem(Items.CHAIN).apply(constantAmount(1f))));
        }

        // loot builder for Aya Tsuji
        private LootTable.Builder ayaTsujiLoot(EntityType<?> type) {
            return LootTable.lootTable()
                    .withPool(constantPool(1f)
                            .add(LootItem.lootTableItem(Items.AIR).setWeight(3).apply(constantAmount(1f)))
                            .add(LootItem.lootTableItem(JItemRegistry.CINDERELLA_MASK.get()).setWeight(1).apply(constantAmount(1f))));
        }

        // loot builder for D'Arby Older
        private LootTable.Builder darbyOlderLoot(EntityType<?> type) {
            return LootTable.lootTable()
                    .withPool(constantPool(1f).add(LootItem.lootTableItem(Items.FEATHER).apply(uniformAmount(2f, 5f))));
        }

        // loot builder for Anubis
        private LootTable.Builder anubisSpecUserLoot(EntityType<?> type) {
            return LootTable.lootTable()
                    .withPool(constantPool(1f)
                            .add(LootItem.lootTableItem(JItemRegistry.ANUBIS_SHEATHED.get()).setWeight(1).apply(constantAmount(1f))));
        }

        public <T extends Entity> void addDrop(EntityType<T> type, Function<EntityType<T>, LootTable.Builder> function) {
            loot.put(type.getDefaultLootTable(), function.apply(type));
        }

        @Override
        public void generate(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
            this.generateLoot();
            for (Map.Entry<ResourceLocation, LootTable.Builder> entry : loot.entrySet()) {
                consumer.accept(entry.getKey(), entry.getValue());
            }
        }
    }

    private static LootPool.Builder constantPool(final float rolls) {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(rolls));
    }

    private static LootPool.Builder uniformPool(final float min, final float max) {
        return LootPool.lootPool().setRolls(UniformGenerator.between(min, max));
    }

    private static LootItemFunction.Builder constantAmount(final float amount) {
        return SetItemCountFunction.setCount(ConstantValue.exactly(amount));
    }

    private static LootItemFunction.Builder uniformAmount(final float min, final float max) {
        return SetItemCountFunction.setCount(UniformGenerator.between(min, max));
    }
}
