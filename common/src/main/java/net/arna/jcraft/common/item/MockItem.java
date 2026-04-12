package net.arna.jcraft.common.item;

import dev.architectury.registry.registries.RegistrySupplier;
import net.arna.jcraft.api.registry.JItemRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MockItem extends Item {
    private static final ItemStack FALLBACK = new ItemStack(Items.DIRT);

    protected MockItem() {
        super(new Properties());
    }

    public static ItemStack getMockedStack(ItemStack mockItemStack) {
        CompoundTag nbt = mockItemStack.getTag();
        if (nbt == null || !nbt.contains("MockItem", Tag.TAG_STRING)) {
            return FALLBACK;
        }

        String mockItemId = nbt.getString("MockItem");
        Item mockItem = BuiltInRegistries.ITEM.get(new ResourceLocation(mockItemId));

        CompoundTag mockData = nbt.contains("MockData", Tag.TAG_COMPOUND) ? nbt.getCompound("MockData") : null;

        ItemStack mockedStack = new ItemStack(mockItem, mockItemStack.getCount());
        mockedStack.setTag(mockData);

        return mockedStack;
    }

    protected static ItemStack createMockStack(ItemStack stack, RegistrySupplier<Item> mockItem) {
        ItemStack mockStack = new ItemStack(mockItem.get(), stack.getCount());
        CompoundTag nbt = mockStack.getOrCreateTag();
        // Register which item it's mocking and copy all relevant NBT data
        nbt.putString("MockItem", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        if (stack.getTag() != null) {
            nbt.put("MockData", stack.getTag());
        }
        return mockStack;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return getMockedStack(stack).getDescriptionId();
    }
}
