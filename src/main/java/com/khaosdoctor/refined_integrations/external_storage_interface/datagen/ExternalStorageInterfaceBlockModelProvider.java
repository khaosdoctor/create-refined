package com.khaosdoctor.refined_integrations.external_storage_interface.datagen;

import com.khaosdoctor.refined_integrations.RefinedIntegrations;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ExternalStorageInterfaceBlockModelProvider extends BlockModelProvider {

  public ExternalStorageInterfaceBlockModelProvider(
      PackOutput output,
      ExistingFileHelper existingFileHelper) {
    super(output, RefinedIntegrations.MODID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    final String blockName = RefinedIntegrations.NETWORK_INTERFACE.getId().getPath();
    getBuilder(String.format("%s_off", blockName)).parent(new ModelFile.UncheckedModelFile(
        ResourceLocation.fromNamespaceAndPath("refinedstorage", "block/controller/light_blue")))
        .texture("all",
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/barrel_side"))
        .texture("cutout",
            ResourceLocation.fromNamespaceAndPath("refinedstorage", "block/controller/cutouts/off"))
        .texture("particle",
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/barrel_side"));

    getBuilder(String.format("%s_on", blockName)).parent(new ModelFile.UncheckedModelFile(
        ResourceLocation.fromNamespaceAndPath("refinedstorage", "block/controller/light_blue")))
        .texture("all",
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/barrel_side"))
        .texture("cutout",
            ResourceLocation.fromNamespaceAndPath("refinedstorage", "block/controller/cutouts/orange"))
        .texture("particle",
            ResourceLocation.fromNamespaceAndPath("minecraft", "block/barrel_side"));
  }
}
