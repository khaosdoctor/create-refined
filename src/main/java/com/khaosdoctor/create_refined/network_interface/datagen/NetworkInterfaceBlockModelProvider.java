package com.khaosdoctor.create_refined.network_interface.datagen;

import com.khaosdoctor.create_refined.CreateRefined;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class NetworkInterfaceBlockModelProvider extends BlockModelProvider {

  public NetworkInterfaceBlockModelProvider(
      PackOutput output,
      ExistingFileHelper existingFileHelper) {
    super(output, CreateRefined.MODID, existingFileHelper);
  }

  @Override
  protected void registerModels() {
    final String blockName = CreateRefined.NETWORK_INTERFACE.getId().getPath();
    getBuilder(String.format("%s_off", blockName)).parent(new ModelFile.UncheckedModelFile(
        ResourceLocation.fromNamespaceAndPath("refinedstorage", "block/controller/light_blue")))
        .texture("all",
            ResourceLocation.fromNamespaceAndPath("create", "block/brass_casing"))
        .texture("cutout",
            ResourceLocation.fromNamespaceAndPath("refinedstorage", "block/controller/cutouts/off"))
        .texture("particle",
            ResourceLocation.fromNamespaceAndPath("create", "block/brass_casing"));

    getBuilder(String.format("%s_on", blockName)).parent(new ModelFile.UncheckedModelFile(
        ResourceLocation.fromNamespaceAndPath("refinedstorage", "block/controller/light_blue")))
        .texture("all",
            ResourceLocation.fromNamespaceAndPath("create", "block/brass_casing"))
        .texture("cutout",
            ResourceLocation.fromNamespaceAndPath("refinedstorage", "block/controller/cutouts/orange"))
        .texture("particle",
            ResourceLocation.fromNamespaceAndPath("create", "block/brass_casing"));
  }
}
