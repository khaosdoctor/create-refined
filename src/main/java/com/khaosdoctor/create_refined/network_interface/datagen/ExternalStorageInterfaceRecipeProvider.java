package com.khaosdoctor.create_refined.network_interface.datagen;

import java.util.concurrent.CompletableFuture;

import com.khaosdoctor.create_refined.CreateRefined;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import com.refinedmods.refinedstorage.common.content.Tags;
import com.refinedmods.refinedstorage.common.misc.ProcessorItem.Type;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.content.Items;

public class ExternalStorageInterfaceRecipeProvider extends RecipeProvider {

  public ExternalStorageInterfaceRecipeProvider(PackOutput output, CompletableFuture<Provider> registries) {
    super(output, registries);
  }

  @Override
  protected void buildRecipes(RecipeOutput recipeOutput) {
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, CreateRefined.NETWORK_INTERFACE.get())
        .pattern("QIQ")
        .pattern("MBX")
        .pattern("QPQ")
        .define('Q',
            Items.INSTANCE.getQuartzEnrichedIron())
        .define('I', Blocks.INSTANCE.getInterface())
        .define('M', Tags.IMPORTERS)
        .define('B', net.minecraft.world.item.Items.BARREL)
        .define('X', Tags.EXPORTERS)
        .define('P',
            Items.INSTANCE.getProcessor(Type.IMPROVED))
        .unlockedBy("refined_storage_controller_available",
            inventoryTrigger(
                ItemPredicate.Builder.item()
                    .of(Tags.CONTROLLERS)
                    .build()))
        .save(recipeOutput);
  }
}
