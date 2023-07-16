package net.arna.jcraft.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import net.arna.jcraft.common.recipes.StandSkinSmithingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Shadow private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;

    @Shadow private Map<Identifier, Recipe<?>> recipesById;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("RETURN"))
    private void addStandSkinSmithingRecipe(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
        recipes = new HashMap<>(recipes);
        Map<Identifier, Recipe<?>> smithingRecipes = new HashMap<>(recipes.getOrDefault(RecipeType.SMITHING, Map.of()));
        smithingRecipes.put(StandSkinSmithingRecipe.ID, StandSkinSmithingRecipe.INSTANCE);
        recipes.put(RecipeType.SMITHING, ImmutableMap.copyOf(smithingRecipes));
        recipes = ImmutableMap.copyOf(recipes);
        
        recipesById = new HashMap<>(recipesById);
        recipesById.put(StandSkinSmithingRecipe.ID, StandSkinSmithingRecipe.INSTANCE);
        recipesById = ImmutableMap.copyOf(recipesById);
    }
}
