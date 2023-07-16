package net.arna.jcraft.common.recipes;

import net.arna.jcraft.JCraft;
import net.arna.jcraft.common.enchantments.CinderellasKissEnchantment;
import net.arna.jcraft.common.item.StandDiscItem;
import net.arna.jcraft.registry.JObjectRegistry;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class StandSkinSmithingRecipe extends SmithingRecipe {
    public static final Identifier ID = JCraft.id("stand_skin");
    public static final StandSkinSmithingRecipe INSTANCE = new StandSkinSmithingRecipe();
    
    private StandSkinSmithingRecipe() {
        super(ID, Ingredient.EMPTY, Ingredient.EMPTY, ItemStack.EMPTY);
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        ItemStack discStack = inventory.getStack(0);
        ItemStack maskStack = inventory.getStack(1);
        
        // TODO check if the stand of this disc has enough skins for this mask.
        // I.e. if this mask has cinderellas kiss 3, but the stand of this disc
        // only has 2 alternate skins, this is invalid.
        return discStack.getItem() == JObjectRegistry.STANDDISC && 
                maskStack.getItem() == JObjectRegistry.CINDERELLA_MASK;
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        ItemStack discStack = inventory.getStack(0);
        ItemStack maskStack = inventory.getStack(1);
        
        ItemStack res = discStack.copy();
        res.setCount(1);
        int skin = CinderellasKissEnchantment.getCKLevel(maskStack); // 0 is default skin
        StandDiscItem.setSkin(res, skin);
        return res;
    }
}
