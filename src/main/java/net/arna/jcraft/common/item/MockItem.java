package net.arna.jcraft.common.item;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class MockItem extends Item {
    private static final ItemStack FALLBACK = new ItemStack(Items.DIRT);

    public MockItem() {
        super(new Settings());
    }

    public static boolean isMockItem(ItemStack stack) {
        return stack.getItem() == JObjectRegistry.MOCK_ITEM;
    }

    public static ItemStack getMockedStack(ItemStack mockItemStack) {
        NbtCompound nbt = mockItemStack.getNbt();
        if (nbt == null || !nbt.contains("MockItem", NbtElement.STRING_TYPE)) return FALLBACK;

        String mockItemId = nbt.getString("MockItem");
        Item mockItem = Registries.ITEM.get(new Identifier(mockItemId));

        NbtCompound mockData = nbt.contains("MockData", NbtElement.COMPOUND_TYPE) ? nbt.getCompound("MockData") : null;

        ItemStack mockedStack = new ItemStack(mockItem, mockItemStack.getCount());
        mockedStack.setNbt(mockData);

        return mockedStack;
    }

    public static ItemStack createMockStack(ItemStack stack) {
        // No need to create a mock stack if it already is one
        if (isMockItem(stack)) return stack;

        ItemStack mockStack = new ItemStack(JObjectRegistry.MOCK_ITEM, stack.getCount());
        NbtCompound nbt = mockStack.getOrCreateNbt();
        // Register which item it's mocking and copy all relevant NBT data
        nbt.putString("MockItem", Registries.ITEM.getId(stack.getItem()).toString());
        if (stack.getNbt() != null) nbt.put("MockData", stack.getNbt());

        return mockStack;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return getMockedStack(stack).getTranslationKey();
    }
}
