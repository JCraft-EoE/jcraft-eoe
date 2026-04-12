package net.arna.jcraft.datagen.providers.assets;

import dev.architectury.registry.registries.RegistrySupplier;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.arna.jcraft.JCraft;
import net.arna.jcraft.api.JRegistries;
import net.arna.jcraft.api.registry.JBlockRegistry;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.arna.jcraft.api.registry.JSpecTypeRegistry;
import net.arna.jcraft.api.registry.JStandTypeRegistry;
import net.arna.jcraft.common.item.CosplayItem;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;

import java.util.Locale;
import java.util.Optional;

public class JModelProvider extends FabricModelProvider {

    private static final ModelTemplate SPAWN_EGG_MODEL = new ModelTemplate(Optional.of(new ResourceLocation("minecraft", "item/template_spawn_egg")), Optional.empty());

    public JModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(final @NonNull BlockModelGenerators generator) {
        generator.createTrivialCube(JBlockRegistry.FOOLISH_SAND_BLOCK.get());
        generator.createTrivialCube(JBlockRegistry.METEORITE_BLOCK.get());
        generator.createTrivialCube(JBlockRegistry.POLISHED_METEORITE_BLOCK.get());
        generator.createTrivialCube(JBlockRegistry.METEORITE_IRON_ORE_BLOCK.get());
        generator.createTrivialCube(JBlockRegistry.SOUL_BLOCK.get());
        generator.createRotatedVariantBlock(JBlockRegistry.HOT_SAND_BLOCK.get());
        generator.createTrivialCube(JBlockRegistry.STELLAR_IRON_BLOCK.get());
        generator.createTrivialCube(JBlockRegistry.CINDERELLA_GREEN_BLOCK.get());
    }

    @Override
    public void generateItemModels(final @NonNull ItemModelGenerators generator) {
        generator.generateFlatItem(JItemRegistry.BOXING_GLOVES.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.BULLET.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.CINDERELLA_MASK.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.DIARY_PAGE.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.DIOS_DIARY.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.GREEN_BABY.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.KNIFE.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        generator.generateFlatItem(JItemRegistry.SCALPEL.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        generator.generateFlatItem(JItemRegistry.KQ_COIN.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.LIVING_ARROW.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        generator.generateFlatItem(JItemRegistry.REQUIEM_ARROW.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.REQUIEM_RUBY.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        generator.generateFlatItem(JItemRegistry.SINNERS_SOUL.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.STAND_ARROW.get(), ModelTemplates.FLAT_HANDHELD_ITEM);
        generator.generateFlatItem(JItemRegistry.STAND_ARROWHEAD.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.ROAD_ROLLER.get(), ModelTemplates.FLAT_ITEM);

        generateCosplayModels(generator);

        generator.generateFlatItem(JItemRegistry.DISC.get(), ModelTemplates.FLAT_ITEM);
        generateSpecDiscModels(generator);
        generateStandDiscModels(generator);

        generator.generateFlatItem(JItemRegistry.STELLAR_IRON_INGOT.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.STONE_MASK.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.PLANKTON_VIAL.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.PRISON_KEY.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.STEEL_BALL.get(), ModelTemplates.FLAT_ITEM);
        generator.generateFlatItem(JItemRegistry.TRAINING_DUMMY.get(), ModelTemplates.FLAT_ITEM);

        generator.generateFlatItem(JItemRegistry.BRAWLER_SPAWN_EGG.get(), SPAWN_EGG_MODEL);
        generator.generateFlatItem(JItemRegistry.HAMON_SPAWN_EGG.get(), SPAWN_EGG_MODEL);
        generator.generateFlatItem(JItemRegistry.TONPETTY_SPAWN_EGG.get(), SPAWN_EGG_MODEL);
        generator.generateFlatItem(JItemRegistry.VAMPIRE_SPAWN_EGG.get(), SPAWN_EGG_MODEL);
        generator.generateFlatItem(JItemRegistry.ANUBIS_USER_SPAWN_EGG.get(), SPAWN_EGG_MODEL);
        generator.generateFlatItem(JItemRegistry.PETSHOP_SPAWN_EGG.get(), SPAWN_EGG_MODEL);
        generator.generateFlatItem(JItemRegistry.AYA_TSUJI_SPAWN_EGG.get(), SPAWN_EGG_MODEL);
        generator.generateFlatItem(JItemRegistry.DARBY_OLDER_SPAWN_EGG.get(), SPAWN_EGG_MODEL);
        generator.generateFlatItem(JItemRegistry.DARBY_YOUNGER_SPAWN_EGG.get(), SPAWN_EGG_MODEL);
    }

    @SneakyThrows
    private void generateCosplayModels(final @NonNull ItemModelGenerators generator) {
        for (final CosplayItem<?> cosplayItem : CosplayItem.all()) {
            for (final RegistrySupplier<? extends ArmorItem> item : cosplayItem) {
                generator.generateLayeredItem(item.getId().withPath(p -> "item/" + p),
                        JCraft.id("item/" + cosplayItem.getName()), JCraft.id("item/cosplay/" + item.get().getMaterial().getName().toLowerCase(Locale.ROOT)));
            }
        }
    }

    private void generateSpecDiscModels(final @NonNull ItemModelGenerators generator) {
        // Generate a model for each spec.
        for (ResourceLocation id : JRegistries.SPEC_TYPE_REGISTRY.getIds()) {
            if (id.equals(JSpecTypeRegistry.NONE.getId())) {
                continue;
            }
            generator.generateLayeredItem(id.withPath(p -> "item/spec_disc_" + p),
                    JCraft.id("item/spec_disc"), id.withPath(p -> "item/specs/" + p));
        }

        // Generate the default spec disc model.
        generator.generateFlatItem(JItemRegistry.SPEC_DISC.get(), ModelTemplates.FLAT_ITEM);
    }

    private void generateStandDiscModels(ItemModelGenerators generator) {
        // Generate a model for each stand and each skin.
        for (ResourceLocation id : JRegistries.STAND_TYPE_REGISTRY.getIds()) {
            if (id.equals(JStandTypeRegistry.NONE.getId())) {
                continue;
            }

            // For now, we assume each stand has 3 skins.
            // This will have to be revamped later when we redo the skin system anyway.
            // If there are fewer than 3 skins, the model will never be used regardless.
            for (int i = 0; i < 4; i++) {
                int fi = i;
                generator.generateLayeredItem(id.withPath(p -> "item/stand_disc_" + p + "_" + fi),
                        JCraft.id("item/stand_disc"), id.withPath(p -> "item/stands/" + p + "_" + fi));
            }
        }

        // Generate the default stand disc model.
        generator.generateFlatItem(JItemRegistry.STAND_DISC.get(), ModelTemplates.FLAT_ITEM);
    }
}
